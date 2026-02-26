const TENANT_API = 'http://openk9-tenant-manager:8080';
const DATASOURCE_API = 'http://openk9-datasource:8080';

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

async function main() {
  console.log('🚀 Starting Data Seeder...');

  const authHeader = 'Basic YWRtaW46YWRtaW4='; // admin:admin

  // ---------------------------------------------------------
  // STEP 1: Create Tenant
  // ---------------------------------------------------------
  console.log('1️⃣  Creating Tenant...');

  const createTenantResponse = await fetch(`${TENANT_API}/api/tenant-manager/tenant-manager/tenant`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': authHeader
    },
    body: JSON.stringify({
      virtualHost: "demo.openk9.localhost",
      securityConfiguration: "LEGACY",
      tenantName: "demo",
      skipOAuth2: true
    })
  });

  if (!createTenantResponse.ok) {
    const text = await createTenantResponse.text();
    console.error(`❌ Failed to create tenant. Status: ${createTenantResponse.status}.`);
    console.error(`Response: ${text}`);
    if (createTenantResponse.status === 409) {
      console.warn('tenant already created');
    } else {
      process.exit(1);
    }
  }

  const tenantData = await createTenantResponse.json();
  const schemaName = tenantData.schemaName;
  console.log(`✅ 1/4 Tenant Created. Schema: ${schemaName}`);

  // ---------------------------------------------------------
  // STEP 2: Initialize Tenant Data
  // ---------------------------------------------------------
  console.log('2️⃣  Initializing Default Data...');

  const initResponse = await fetch(`${TENANT_API}/api/tenant-manager/provisioning/initTenant`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': authHeader
    },
    body: JSON.stringify({ tenantName: schemaName })
  });

  if (!initResponse.ok) {
    console.error(`❌ Failed to initialize tenant. Status: ${initResponse.status}.`);
    console.error(await initResponse.text());
    process.exit(1);
  }
  console.log('✅ 2/4 Tenant Initialized.');

  // ---------------------------------------------------------
  // STEP 3: GraphQL
  // ---------------------------------------------------------
  console.log('3️⃣  Configures Connectors...');

  const webConnectorPayload = {
    operationName: "PluginDriverWithDocType",
    variables: {
      description: "Docker Compose Web Connector",
      name: "Sitemap Crawler",
      type: "HTTP",
      provisioning: "USER",
      jsonConfig: "{\"baseUri\":\"openk9-web-connector:5000\",\"path\":\"/startSitemapCrawling\",\"method\":\"POST\"}",
      docTypeUserDTOSet: []
    },
    query: `mutation PluginDriverWithDocType($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $jsonConfig: String!, $provisioning: Provisioning!, $docTypeUserDTOSet: [DocTypeUserDTOInput]) {
      pluginDriverWithDocType(
        id: $id
        pluginWithDocTypeDTO: {name: $name, description: $description, type: $type, jsonConfig: $jsonConfig, provisioning: $provisioning, docTypeUserDTOSet: $docTypeUserDTOSet}
      ) {
        entity { id }
      }
    }`
  };

  const webConnectorResponse = await fetch(`${DATASOURCE_API}/api/datasource/graphql`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': authHeader,
      'X-TENANT-ID': schemaName
    },
    body: JSON.stringify(webConnectorPayload)
  });

  if (!webConnectorResponse.ok) {
    console.error(`❌ Failed to configure Web Connector. Status: ${webConnectorResponse.status}.`);
    console.error(await webConnectorResponse.text());
    process.exit(1);
  }

  console.log('✅ 3/4 Web Connector configured.');

  const minioConnectorPayload = {
    operationName: "PluginDriverWithDocType",
    variables: {
      description: "Docker Compose Minio Connector",
      name: "Minio Connector",
      type: "HTTP",
      provisioning: "USER",
      jsonConfig: "{\"baseUri\":\"openk9-minio-connector:5000\"}",
      docTypeUserDTOSet: []
    },
    query: `mutation PluginDriverWithDocType($id: ID, $name: String!, $description: String, $type: PluginDriverType!, $jsonConfig: String!, $provisioning: Provisioning!, $docTypeUserDTOSet: [DocTypeUserDTOInput]) {
      pluginDriverWithDocType(
        id: $id
        pluginWithDocTypeDTO: {name: $name, description: $description, type: $type, jsonConfig: $jsonConfig, provisioning: $provisioning, docTypeUserDTOSet: $docTypeUserDTOSet}
      ) {
        entity { id }
      }
    }`
  };

  const minioConnectorResponse = await fetch(`${DATASOURCE_API}/api/datasource/graphql`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': authHeader,
      'X-TENANT-ID': schemaName
    },
    body: JSON.stringify(minioConnectorPayload)
  });

  if (!minioConnectorResponse.ok) {
    console.error(`❌ Failed to configure Minio Connector. Status: ${minioConnectorResponse.status}.`);
    console.error(await minioConnectorResponse.text());
    process.exit(1);
  }

  console.log('✅ 4/4 Minio Connector configured.');

  console.log('🎉 Done.');
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
