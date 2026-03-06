const TENANT_API = 'http://openk9-tenant-manager:8080';
const DATASOURCE_API = 'http://openk9-datasource:8080';

const VIRTUAL_HOST = 'demo.openk9.localhost';
const TENANT_NAME = 'demo';

const MAX_RETRIES = 3;
const RETRY_DELAY_MS = 5000;

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

const authHeader = 'Basic YWRtaW46YWRtaW4='; // admin:admin

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

async function ensureTenant() {
  console.log('1/6 Creating Tenant...');

  const response = await fetchWithRetry(
    `${TENANT_API}/api/tenant-manager/tenant-manager/tenant`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': authHeader
      },
      body: JSON.stringify({
        virtualHost: VIRTUAL_HOST,
        securityConfiguration: 'LEGACY',
        tenantName: TENANT_NAME,
        skipOAuth2: true
      })
    },
    'Create Tenant'
  );

  if (response.ok) {
    const data = await response.json();
    console.log(`1/6 Tenant created. Schema: ${data.schemaName}`);
    return data.schemaName;
  }

  if (response.status === 409) {
    console.log('1/6 Tenant already exists, skipping.');
    return `${TENANT_NAME}`;
  }

  const text = await response.text();
  throw new Error(`Failed to create tenant (${response.status}): ${text}`);
}

async function initTenant(schemaName) {
  console.log('2/6 Initializing Tenant Data...');

  const response = await fetchWithRetry(
    `${TENANT_API}/api/tenant-manager/provisioning/initTenant`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': authHeader
      },
      body: JSON.stringify({ tenantName: schemaName })
    },
    'Init Tenant'
  );

  if (response.ok) {
    console.log('2/6 Tenant initialized.');
    return;
  }

  const text = await response.text();
  throw new Error(`Failed to initialize tenant (${response.status}): ${text}`);
}

async function graphqlQuery(schemaName, query, label) {
  for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
    const response = await fetchWithRetry(
      `${DATASOURCE_API}/api/datasource/graphql`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': authHeader,
          'X-TENANT-ID': schemaName
        },
        body: JSON.stringify({ query })
      },
      label
    );

    if (!response.ok) {
      return null;
    }

    const body = await response.json();

    if (!body.errors) {
      return body.data;
    }

    const isTransient = body.errors.some(e =>
      e.extensions?.exception?.includes('VertxException') ||
      e.message === 'System error'
    );

    if (!isTransient || attempt === MAX_RETRIES) {
      console.warn(`${label}: GraphQL error: ${body.errors.map(e => e.message).join('; ')}`);
      return null;
    }

    console.log(`${label}: transient error, retrying in ${RETRY_DELAY_MS / 1000}s... (${attempt}/${MAX_RETRIES})`);
    await sleep(RETRY_DELAY_MS);
  }
  return null;
}

async function getExistingPluginDriverNames(schemaName) {
  const data = await graphqlQuery(
    schemaName,
    `{ pluginDrivers(first: 100) { edges { node { name } } } }`,
    'Query Plugin Drivers'
  );

  if (!data) {
    console.warn('Could not query existing plugin drivers, will attempt creation anyway.');
    return new Set();
  }

  const edges = data.pluginDrivers?.edges ?? [];
  return new Set(edges.map(e => e.node.name));
}

async function ensurePluginDriver(schemaName, name, description, resourceUri) {
  const resourceUriFields = resourceUri.path
    ? `resourceUri: { baseUri: "${resourceUri.baseUri}", path: "${resourceUri.path}" }`
    : `resourceUri: { baseUri: "${resourceUri.baseUri}" }`;

  const payload = {
    query: `mutation {
      pluginDriverWithDocType(pluginWithDocTypeDTO: {
        name: "${name}"
        description: "${description}"
        type: HTTP
        provisioning: USER
        ${resourceUriFields}
        docTypeUserDTOSet: []
      }) {
        entity { id name }
      }
    }`
  };

  for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
    const response = await fetchWithRetry(
      `${DATASOURCE_API}/api/datasource/graphql`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': authHeader,
          'X-TENANT-ID': schemaName
        },
        body: JSON.stringify(payload)
      },
      `Create ${name}`
    );

    if (!response.ok) {
      const text = await response.text();
      throw new Error(`Failed to create ${name} (${response.status}): ${text}`);
    }

    const body = await response.json();

    if (!body.errors) {
      console.log(`  ${name} configured.`);
      return;
    }

    // Transient errors (VertxException, timeouts) are retryable
    const isTransient = body.errors.some(e =>
      e.extensions?.exception?.includes('VertxException') ||
      e.message === 'System error'
    );

    if (!isTransient || attempt === MAX_RETRIES) {
      const msg = body.errors.map(e => e.message).join('; ');
      throw new Error(`GraphQL error creating ${name}: ${msg}`);
    }

    console.log(`  ${name}: transient error, retrying in ${RETRY_DELAY_MS / 1000}s... (${attempt}/${MAX_RETRIES})`);
    await sleep(RETRY_DELAY_MS);
  }
}

async function getExistingDatasourceNames(schemaName) {
  const data = await graphqlQuery(
    schemaName,
    `{ datasources(first: 100) { edges { node { name } } } }`,
    'Query Datasources'
  );

  if (!data) {
    console.warn('Could not query existing datasources, will attempt creation anyway.');
    return new Set();
  }

  const edges = data.datasources?.edges ?? [];
  return new Set(edges.map(e => e.node.name));
}

async function getPluginDriverIdByName(schemaName, name) {
  const data = await graphqlQuery(
    schemaName,
    `{ pluginDrivers(first: 100, searchText: "${name}") { edges { node { id name } } } }`,
    `Find PluginDriver ${name}`
  );

  if (!data) {
    throw new Error(`Failed to query plugin driver "${name}"`);
  }

  const match = data.pluginDrivers?.edges?.find(e => e.node.name === name);

  if (!match) {
    throw new Error(`Plugin driver "${name}" not found`);
  }

  return match.node.id;
}

async function ensureDatasource(schemaName, name, description, pluginDriverId, jsonConfig, dataIndex) {
  const jsonConfigEscaped = JSON.stringify(jsonConfig).replace(/"/g, '\\"');

  const payload = {
    query: `mutation {
      createDatasourceConnection(datasourceConnection: {
        name: "${name}"
        description: "${description}"
        pluginDriverId: ${pluginDriverId}
        jsonConfig: "${jsonConfigEscaped}"
        schedulable: false
        dataIndex: {
          name: "${dataIndex.name}"
        }
      }) {
        entity { id name }
      }
    }`
  };

  for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
    const response = await fetchWithRetry(
      `${DATASOURCE_API}/api/datasource/graphql`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': authHeader,
          'X-TENANT-ID': schemaName
        },
        body: JSON.stringify(payload)
      },
      `Create Datasource ${name}`
    );

    if (!response.ok) {
      const text = await response.text();
      throw new Error(`Failed to create datasource ${name} (${response.status}): ${text}`);
    }

    const body = await response.json();

    if (!body.errors) {
      console.log(`  ${name} created.`);
      return;
    }

    const isTransient = body.errors.some(e =>
      e.extensions?.exception?.includes('VertxException') ||
      e.message === 'System error'
    );

    if (!isTransient || attempt === MAX_RETRIES) {
      const msg = body.errors.map(e => e.message).join('; ');
      throw new Error(`GraphQL error creating datasource ${name}: ${msg}`);
    }

    console.log(`  ${name}: transient error, retrying in ${RETRY_DELAY_MS / 1000}s... (${attempt}/${MAX_RETRIES})`);
    await sleep(RETRY_DELAY_MS);
  }
}

async function main() {
  console.log('Starting Data Seeder...');

  const schemaName = await ensureTenant();

  await initTenant(schemaName);

  // --- Step 3: Connectors ---
  console.log('3/6 Configuring Connectors...');
  const existingDrivers = await getExistingPluginDriverNames(schemaName);

  const connectors = [
    {
      name: 'Sitemap Crawler',
      description: 'Docker Compose Web Connector',
      resourceUri: { baseUri: 'http://openk9-web-connector:5000', path: '/startSitemapCrawling' }
    },
    {
      name: 'Minio Connector',
      description: 'Docker Compose Minio Connector',
      resourceUri: { baseUri: 'http://openk9-minio-connector:5000' }
    }
  ];

  let created = 0;
  let skipped = 0;

  let failed = 0;

  for (const connector of connectors) {
    if (existingDrivers.has(connector.name)) {
      console.log(`  ${connector.name}: already exists, skipping.`);
      skipped++;
    }
    else {
      try {
        await ensurePluginDriver(
          schemaName,
          connector.name,
          connector.description,
          connector.resourceUri
        );
        created++;
      }
      catch (err) {
        console.warn(`  ${connector.name}: failed, skipping. (${err.message})`);
        failed++;
      }
    }
  }

  console.log(`3/6 Connectors done (${created} created, ${skipped} skipped, ${failed} failed).`);

  // --- Step 4: Datasources ---
  console.log('4/6 Configuring Datasources...');
  const existingDatasources = await getExistingDatasourceNames(schemaName);

  const datasources = [
    {
      name: 'SMC Website',
      description: 'Crawl smc.it via sitemap',
      pluginDriverName: 'Sitemap Crawler',
      jsonConfig: {
        sitemapUrls: ['https://www.smc.it/sitemap.xml'],
        maxSizeBytes: 10485760
      },
      dataIndex: { name: 'smc-web' }
    }
  ];

  created = 0;
  skipped = 0;
  failed = 0;

  for (const ds of datasources) {
    if (existingDatasources.has(ds.name)) {
      console.log(`  ${ds.name}: already exists, skipping.`);
      skipped++;
    }
    else {
      try {
        const pluginDriverId = await getPluginDriverIdByName(schemaName, ds.pluginDriverName);
        await ensureDatasource(
          schemaName,
          ds.name,
          ds.description,
          pluginDriverId,
          ds.jsonConfig,
          ds.dataIndex
        );
        created++;
      }
      catch (err) {
        console.warn(`  ${ds.name}: failed, skipping. (${err.message})`);
        failed++;
      }
    }
  }

  console.log(`4/6 Datasources done (${created} created, ${skipped} skipped, ${failed} failed).`);

  // --- Step 5: Link datasources to Default Bucket ---
  console.log('5/6 Linking Datasources to Default Bucket...');

  const bucketData = await graphqlQuery(
    schemaName,
    `{ buckets(first: 10, searchText: "Default Bucket") {
        edges { node { id name datasources(first: 100) { edges { node { name } } } } }
    } }`,
    'Query Default Bucket'
  );

  const defaultBucket = bucketData?.buckets?.edges?.find(e => e.node.name === 'Default Bucket');

  if (!defaultBucket) {
    console.warn('  Default Bucket not found, skipping linking.');
  }
  else {
    const bucketId = defaultBucket.node.id;
    const linkedNames = new Set(
      (defaultBucket.node.datasources?.edges ?? []).map(e => e.node.name)
    );

    const datasourceNames = datasources.map(ds => ds.name);
    let linked = 0;

    for (const dsName of datasourceNames) {
      if (linkedNames.has(dsName)) {
        console.log(`  ${dsName}: already linked, skipping.`);
        continue;
      }

      // Look up datasource ID by name
      const dsData = await graphqlQuery(
        schemaName,
        `{ datasources(first: 10, searchText: "${dsName}") { edges { node { id name } } } }`,
        `Find Datasource ${dsName}`
      );

      const dsMatch = dsData?.datasources?.edges?.find(e => e.node.name === dsName);

      if (!dsMatch) {
        console.warn(`  ${dsName}: not found, skipping.`);
        continue;
      }

      const result = await graphqlQuery(
        schemaName,
        `mutation { addDatasourceToBucket(bucketId: ${bucketId}, datasourceId: ${dsMatch.node.id}) { item1 { id } } }`,
        `Link ${dsName} to Default Bucket`
      );

      if (result) {
        console.log(`  ${dsName} linked to Default Bucket.`);
        linked++;
      }
      else {
        console.warn(`  ${dsName}: failed to link.`);
      }
    }

    console.log(`5/6 Bucket linking done (${linked} linked).`);
  }

  console.log('6/6 Done.');
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
