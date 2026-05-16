const http = require('http');

// All OpenK9 services on 3.0.x use the shared common resource
// `quarkus.http.root-path=/api/${quarkus.application.name}` defined in
// core/common/resources-common/META-INF/microprofile-config.properties,
// so resource paths from `@Path` annotations are prefixed with
// `/api/<service-name>` at runtime.
const TENANT_API = 'http://openk9-tenant-manager:8080/api/tenant-manager';
const DATASOURCE_HOST = 'openk9-datasource';
const DATASOURCE_PORT = 8080;
const DATASOURCE_GRAPHQL_PATH = '/api/datasource/graphql';
const KEYCLOAK_URL = 'http://keycloak.openk9.localhost:8081';

const VIRTUAL_HOST = 'demo.openk9.localhost';

const KEYCLOAK_MASTER_ADMIN_USER = 'user';
const KEYCLOAK_MASTER_ADMIN_PASSWORD = 'openk9';
const KEYCLOAK_ADMIN_CLIENT = 'admin-cli';

const TM_REALM = 'tenant-manager';
const TM_ADMIN_USER = 'admin';
const TM_ADMIN_PASSWORD = 'admin';
const TM_ADMIN_ROLE = 'admin';

// The per-tenant Keycloak realm provisioned by tenant-manager has a
// `k9admin` user with a server-generated random password. The
// datasource service requires the `k9-admin` realm role on the
// caller — already assigned to k9admin — so we reset the password
// to a known value and use it for datasource GraphQL calls.
const TENANT_ADMIN_USER = 'k9admin';
const TENANT_ADMIN_PASSWORD = 'openk9';

// Connector plugin drivers to register on the demo tenant. Each
// entry is keyed by the compose profile that has to be active for
// the backing service to be running — so the Minio Connector is
// only seeded when the user starts the stack with
// `./k9.sh up --with=file-handling`. The web-connector container
// is in the core compose file, so its plugin driver is always
// registered.
//
// On 3.0.x the `PluginDriverDTO` carries the connection info as a
// JSON string in `jsonConfig`, deserialized server-side into
// `io.openk9.datasource.plugindriver.HttpPluginDriverInfo`:
//   { secure: boolean, baseUri: string, path: string, method: Method, body?: object }
const CONNECTORS_BY_PROFILE = {
  core: [
    {
      name: 'Sitemap Crawler',
      description: 'Docker Compose Web Connector',
      httpInfo: {
        secure: false,
        baseUri: 'openk9-web-connector:5000',
        path: '/startSitemapCrawling',
        method: 'POST',
      },
    },
  ],
  'file-handling': [
    {
      name: 'Minio Connector',
      description: 'Docker Compose Minio Connector',
      httpInfo: {
        secure: false,
        baseUri: 'openk9-minio-connector:5000',
        path: '/execute',
        method: 'POST',
      },
    },
  ],
};

function resolveActiveConnectors() {
  // `core` is always implicit; everything else comes from the
  // OPENK9_PROFILES env exported by k9.sh.
  const raw = process.env.OPENK9_PROFILES || '';
  const explicit = raw.split(/\s+/).map((p) => p.trim()).filter(Boolean);
  const profiles = new Set(['core', ...explicit]);
  const connectors = [];
  for (const profile of profiles) {
    const list = CONNECTORS_BY_PROFILE[profile];
    if (list) connectors.push(...list);
  }
  return { profiles: [...profiles], connectors };
}

const MAX_RETRIES = 3;
const RETRY_DELAY_MS = 5000;

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function fetchWithRetry(url, options, label) {
  for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
    try {
      const response = await fetch(url, options);
      return response;
    }
    catch (err) {
      if (attempt === MAX_RETRIES) {
        throw new Error(`${label}: failed after ${MAX_RETRIES} attempts: ${err.message}`);
      }
      console.log(`${label}: attempt ${attempt}/${MAX_RETRIES} failed (${err.message}), retrying in ${RETRY_DELAY_MS / 1000}s...`);
      await sleep(RETRY_DELAY_MS);
    }
  }
}

async function getAccessToken(realm, username, password) {
  const url = `${KEYCLOAK_URL}/realms/${realm}/protocol/openid-connect/token`;
  const body = new URLSearchParams({
    client_id: KEYCLOAK_ADMIN_CLIENT,
    grant_type: 'password',
    username,
    password,
  });

  const response = await fetchWithRetry(
    url,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body.toString(),
    },
    `Token (${realm})`
  );

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Failed to get token from ${realm} (${response.status}): ${text}`);
  }
  const data = await response.json();
  return data.access_token;
}

async function masterBearer() {
  return `Bearer ${await getAccessToken('master', KEYCLOAK_MASTER_ADMIN_USER, KEYCLOAK_MASTER_ADMIN_PASSWORD)}`;
}

async function tenantManagerBearer() {
  return `Bearer ${await getAccessToken(TM_REALM, TM_ADMIN_USER, TM_ADMIN_PASSWORD)}`;
}

// Patches the `admin-cli` client of a given realm so that access
// tokens emitted via ROPC are not the lightweight variant Keycloak
// 25+ ships by default. Without this Quarkus OIDC reads an empty
// role list from the token and `@RolesAllowed` fails 403.
async function disableLightweightTokens(authMaster, realmName) {
  const clientsApi = `${KEYCLOAK_URL}/admin/realms/${realmName}/clients`;
  const lookup = await fetchWithRetry(
    `${clientsApi}?clientId=${KEYCLOAK_ADMIN_CLIENT}`,
    { method: 'GET', headers: { 'Authorization': authMaster } },
    `Lookup ${KEYCLOAK_ADMIN_CLIENT} in ${realmName}`
  );
  if (!lookup.ok) {
    throw new Error(`Failed to lookup admin-cli in ${realmName} (${lookup.status}): ${await lookup.text()}`);
  }
  const clients = await lookup.json();
  if (clients.length === 0) {
    throw new Error(`admin-cli client not found in realm ${realmName}`);
  }
  const adminCli = clients[0];
  const updatedAttrs = {
    ...(adminCli.attributes || {}),
    'client.use.lightweight.access.token.enabled': 'false',
  };
  const patch = await fetchWithRetry(
    `${clientsApi}/${adminCli.id}`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': authMaster },
      body: JSON.stringify({
        ...adminCli,
        directAccessGrantsEnabled: true,
        fullScopeAllowed: true,
        attributes: updatedAttrs,
      }),
    },
    `Patch admin-cli in ${realmName}`
  );
  if (!patch.ok) {
    throw new Error(`Failed to patch admin-cli in ${realmName} (${patch.status}): ${await patch.text()}`);
  }
}

// Idempotent bootstrap of the `tenant-manager` realm: create the
// realm, the `admin` role, an `admin`/`admin` user with that role
// (clearing requiredActions so password grants work), and disable
// lightweight tokens on admin-cli.
async function ensureTenantManagerRealm(authMaster) {
  console.log(`0/4 Ensuring Keycloak realm "${TM_REALM}"...`);
  const adminApi = `${KEYCLOAK_URL}/admin/realms`;
  const jsonHeaders = {
    'Content-Type': 'application/json',
    'Authorization': authMaster,
  };

  const realmCheck = await fetchWithRetry(
    `${adminApi}/${TM_REALM}`,
    { method: 'GET', headers: { 'Authorization': authMaster } },
    `Check ${TM_REALM} realm`
  );
  if (realmCheck.status === 404) {
    const createRealm = await fetchWithRetry(
      adminApi,
      { method: 'POST', headers: jsonHeaders, body: JSON.stringify({ realm: TM_REALM, enabled: true }) },
      `Create ${TM_REALM} realm`
    );
    if (!createRealm.ok && createRealm.status !== 409) {
      throw new Error(`Failed to create realm (${createRealm.status}): ${await createRealm.text()}`);
    }
    console.log(`  realm ${TM_REALM} created.`);
  }
  else if (realmCheck.ok) {
    console.log(`  realm ${TM_REALM} already exists.`);
  }
  else {
    throw new Error(`Unexpected status checking realm (${realmCheck.status}): ${await realmCheck.text()}`);
  }

  const rolesApi = `${adminApi}/${TM_REALM}/roles`;
  const createRole = await fetchWithRetry(
    rolesApi,
    { method: 'POST', headers: jsonHeaders, body: JSON.stringify({ name: TM_ADMIN_ROLE }) },
    `Create role ${TM_ADMIN_ROLE}`
  );
  if (!createRole.ok && createRole.status !== 409) {
    throw new Error(`Failed to create role (${createRole.status}): ${await createRole.text()}`);
  }
  const roleLookup = await fetchWithRetry(
    `${rolesApi}/${TM_ADMIN_ROLE}`,
    { method: 'GET', headers: { 'Authorization': authMaster } },
    `Lookup role ${TM_ADMIN_ROLE}`
  );
  if (!roleLookup.ok) {
    throw new Error(`Failed to lookup role (${roleLookup.status}): ${await roleLookup.text()}`);
  }
  const roleRep = await roleLookup.json();
  console.log(`  role ${TM_ADMIN_ROLE} ensured.`);

  const usersApi = `${adminApi}/${TM_REALM}/users`;
  const userSearch = await fetchWithRetry(
    `${usersApi}?username=${encodeURIComponent(TM_ADMIN_USER)}&exact=true`,
    { method: 'GET', headers: { 'Authorization': authMaster } },
    `Lookup user ${TM_ADMIN_USER}`
  );
  if (!userSearch.ok) {
    throw new Error(`Failed to search user (${userSearch.status}): ${await userSearch.text()}`);
  }
  let users = await userSearch.json();
  let userId;
  if (users.length === 0) {
    const createUser = await fetchWithRetry(
      usersApi,
      {
        method: 'POST',
        headers: jsonHeaders,
        body: JSON.stringify({
          username: TM_ADMIN_USER, enabled: true, emailVerified: true,
          firstName: 'Admin', lastName: 'OpenK9', email: 'admin@openk9.local',
          requiredActions: [],
          credentials: [{ type: 'password', value: TM_ADMIN_PASSWORD, temporary: false }],
        }),
      },
      `Create user ${TM_ADMIN_USER}`
    );
    if (!createUser.ok && createUser.status !== 409) {
      throw new Error(`Failed to create user (${createUser.status}): ${await createUser.text()}`);
    }
    const location = createUser.headers.get('location') || '';
    userId = location.substring(location.lastIndexOf('/') + 1);
    console.log(`  user ${TM_ADMIN_USER} created.`);
  }
  else {
    userId = users[0].id;
    console.log(`  user ${TM_ADMIN_USER} already exists.`);
  }

  const updateUser = await fetchWithRetry(
    `${usersApi}/${userId}`,
    { method: 'PUT', headers: jsonHeaders, body: JSON.stringify({ enabled: true, emailVerified: true, requiredActions: [] }) },
    `Update user ${TM_ADMIN_USER}`
  );
  if (!updateUser.ok) {
    throw new Error(`Failed to update user (${updateUser.status}): ${await updateUser.text()}`);
  }
  const resetPassword = await fetchWithRetry(
    `${usersApi}/${userId}/reset-password`,
    { method: 'PUT', headers: jsonHeaders, body: JSON.stringify({ type: 'password', value: TM_ADMIN_PASSWORD, temporary: false }) },
    `Reset password ${TM_ADMIN_USER}`
  );
  if (!resetPassword.ok) {
    throw new Error(`Failed to reset password (${resetPassword.status}): ${await resetPassword.text()}`);
  }
  const mapping = await fetchWithRetry(
    `${usersApi}/${userId}/role-mappings/realm`,
    { method: 'POST', headers: jsonHeaders, body: JSON.stringify([{ id: roleRep.id, name: roleRep.name }]) },
    `Map ${TM_ADMIN_ROLE} role`
  );
  if (!mapping.ok && mapping.status !== 409) {
    throw new Error(`Failed to map role (${mapping.status}): ${await mapping.text()}`);
  }

  await disableLightweightTokens(authMaster, TM_REALM);
  console.log(`  ${KEYCLOAK_ADMIN_CLIENT} configured for full access tokens.`);
}

// After a tenant is created the tenant-manager actor system has
// provisioned a per-tenant realm with k9admin user + k9-admin
// role. Reset the k9admin password to a known value and disable
// lightweight tokens on the realm's admin-cli so we can get a
// usable bearer for datasource GraphQL.
async function prepareTenantRealmAccess(authMaster, realmName) {
  const usersApi = `${KEYCLOAK_URL}/admin/realms/${realmName}/users`;
  const userSearch = await fetchWithRetry(
    `${usersApi}?username=${encodeURIComponent(TENANT_ADMIN_USER)}&exact=true`,
    { method: 'GET', headers: { 'Authorization': authMaster } },
    `Lookup ${TENANT_ADMIN_USER} in ${realmName}`
  );
  if (!userSearch.ok) {
    throw new Error(`Failed to lookup ${TENANT_ADMIN_USER} in ${realmName} (${userSearch.status}): ${await userSearch.text()}`);
  }
  const users = await userSearch.json();
  if (users.length === 0) {
    throw new Error(`${TENANT_ADMIN_USER} not found in realm ${realmName}`);
  }
  const userId = users[0].id;

  const jsonHeaders = {
    'Content-Type': 'application/json',
    'Authorization': authMaster,
  };
  const updateUser = await fetchWithRetry(
    `${usersApi}/${userId}`,
    { method: 'PUT', headers: jsonHeaders, body: JSON.stringify({ enabled: true, emailVerified: true, requiredActions: [] }) },
    `Update ${TENANT_ADMIN_USER}`
  );
  if (!updateUser.ok) {
    throw new Error(`Failed to update ${TENANT_ADMIN_USER} (${updateUser.status}): ${await updateUser.text()}`);
  }
  const reset = await fetchWithRetry(
    `${usersApi}/${userId}/reset-password`,
    { method: 'PUT', headers: jsonHeaders, body: JSON.stringify({ type: 'password', value: TENANT_ADMIN_PASSWORD, temporary: false }) },
    `Reset ${TENANT_ADMIN_USER} password`
  );
  if (!reset.ok) {
    throw new Error(`Failed to reset ${TENANT_ADMIN_USER} password (${reset.status}): ${await reset.text()}`);
  }

  await disableLightweightTokens(authMaster, realmName);
  console.log(`  realm ${realmName}: admin-cli full-token + ${TENANT_ADMIN_USER} pw reset.`);

  const token = await getAccessToken(realmName, TENANT_ADMIN_USER, TENANT_ADMIN_PASSWORD);
  return `Bearer ${token}`;
}

async function ensureTenant(authTenantManager) {
  console.log('1/4 Creating Tenant...');
  const response = await fetchWithRetry(
    `${TENANT_API}/tenant-manager/tenant`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': authTenantManager },
      body: JSON.stringify({ virtualHost: VIRTUAL_HOST }),
    },
    'Create Tenant'
  );

  if (response.ok) {
    const data = await response.json();
    console.log(`1/4 Tenant created. id=${data.id} schema=${data.schemaName} realm=${data.realmName} clientId=${data.clientId}`);
    return data;
  }

  if (response.status === 409) {
    console.log('1/4 Tenant already exists. Initializer is fire-once.');
    console.log('    To re-run from scratch: ./k9.sh down -v && ./k9.sh up');
    process.exit(0);
  }

  const text = await response.text();
  throw new Error(`Failed to create tenant (${response.status}): ${text}`);
}

async function initTenantData(authTenantManager, schemaName) {
  console.log(`2/4 Initializing tenant data (schema=${schemaName})...`);
  const response = await fetchWithRetry(
    `${TENANT_API}/provisioning/initTenant`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': authTenantManager },
      body: JSON.stringify({ tenantName: schemaName }),
    },
    'Init Tenant'
  );
  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Failed to initialize tenant (${response.status}): ${text}`);
  }
  const data = await response.json().catch(() => ({}));
  console.log(`2/4 Tenant data initialized. Default bucket id=${data.bucketId}`);
  return data.bucketId;
}

// node:fetch / undici treats `Host` as a forbidden header and
// silently rewrites it back to the TCP destination, which breaks
// the datasource OIDC tenant resolver
// (io.openk9.auth.resolver.OIDCTenantResolver) — it reads the
// tenant from `routingContext.request().authority()` (i.e. the
// HTTP Host header) and looks the tenant up by its virtualHost.
// Use Node's `http` module directly, which lets us put the
// virtualHost in the Host header while still TCP-connecting to
// `openk9-datasource`.
function httpJsonPost(host, port, path, headers, body) {
  return new Promise((resolve, reject) => {
    const payload = Buffer.from(body);
    const req = http.request(
      {
        host,
        port,
        path,
        method: 'POST',
        headers: {
          ...headers,
          'Content-Length': payload.length,
        },
      },
      (res) => {
        const chunks = [];
        res.on('data', (chunk) => chunks.push(chunk));
        res.on('end', () => {
          const text = Buffer.concat(chunks).toString('utf8');
          resolve({ status: res.statusCode, headers: res.headers, text });
        });
      }
    );
    req.on('error', reject);
    req.write(payload);
    req.end();
  });
}

async function graphqlCall(auth, query, label) {
  let lastError;
  for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
    try {
      const result = await httpJsonPost(
        DATASOURCE_HOST,
        DATASOURCE_PORT,
        DATASOURCE_GRAPHQL_PATH,
        {
          'Content-Type': 'application/json',
          'Authorization': auth,
          'Host': VIRTUAL_HOST,
        },
        JSON.stringify({ query })
      );
      if (result.status < 200 || result.status >= 300) {
        throw new Error(`${label}: HTTP ${result.status}: ${result.text}`);
      }
      const body = JSON.parse(result.text);
      if (body.errors) {
        throw new Error(`${label}: GraphQL: ${body.errors.map((e) => e.message).join('; ')}`);
      }
      return body.data;
    }
    catch (err) {
      lastError = err;
      if (attempt === MAX_RETRIES) break;
      console.log(`${label}: attempt ${attempt}/${MAX_RETRIES} failed (${err.message}), retrying in ${RETRY_DELAY_MS / 1000}s...`);
      await sleep(RETRY_DELAY_MS);
    }
  }
  throw lastError;
}

async function createPluginDriver(auth, connector) {
  // jsonConfig is a serialized HttpPluginDriverInfo; escape the
  // double quotes once it's a string literal inside the GraphQL
  // mutation source.
  const jsonConfigEscaped = JSON.stringify(connector.httpInfo).replace(/"/g, '\\"');

  const mutation = `mutation {
    pluginDriverWithDocType(pluginWithDocTypeDTO: {
      name: "${connector.name}"
      description: "${connector.description}"
      type: HTTP
      provisioning: USER
      jsonConfig: "${jsonConfigEscaped}"
      docTypeUserDTOSet: []
    }) {
      entity { id name }
    }
  }`;

  await graphqlCall(auth, mutation, `Create plugin driver ${connector.name}`);
  console.log(`  ${connector.name} created.`);
}

async function main() {
  console.log('Starting Data Seeder...');

  const authMaster = await masterBearer();
  await ensureTenantManagerRealm(authMaster);
  const authTenantManager = await tenantManagerBearer();

  const tenant = await ensureTenant(authTenantManager);
  await initTenantData(authTenantManager, tenant.schemaName);

  console.log(`Preparing realm "${tenant.realmName}" access for datasource GraphQL...`);
  const authTenant = await prepareTenantRealmAccess(authMaster, tenant.realmName);

  const { profiles, connectors } = resolveActiveConnectors();
  console.log(`3/4 Registering plugin drivers (profiles: ${profiles.join(', ')})...`);
  let created = 0;
  let failed = 0;
  for (const connector of connectors) {
    try {
      await createPluginDriver(authTenant, connector);
      created++;
    }
    catch (err) {
      console.warn(`  ${connector.name}: ${err.message}`);
      failed++;
    }
  }
  console.log(`3/4 Plugin drivers done (${created} created, ${failed} failed).`);

  console.log('4/4 Done.');
  console.log('');
  console.log('==============================================================');
  console.log(`Demo tenant ready at https://${VIRTUAL_HOST}`);
  console.log('');
  console.log('Keycloak realms:');
  console.log(`  master            user: ${KEYCLOAK_MASTER_ADMIN_USER}     / ${KEYCLOAK_MASTER_ADMIN_PASSWORD}      (Keycloak admin)`);
  console.log(`  ${TM_REALM}    user: ${TM_ADMIN_USER}     / ${TM_ADMIN_PASSWORD}      (tenant-manager admin role)`);
  console.log(`  ${tenant.realmName}            user: ${TENANT_ADMIN_USER}   / ${TENANT_ADMIN_PASSWORD}     (demo tenant admin: k9-admin/k9-read/k9-write)`);
  console.log('==============================================================');
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
