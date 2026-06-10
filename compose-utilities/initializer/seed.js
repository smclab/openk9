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
  console.log('1/7 Creating Tenant...');

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
        securityConfiguration: 'NO_GATEWAY_AUTH',
        tenantName: TENANT_NAME
      })
    },
    'Create Tenant'
  );

  if (response.ok) {
    const data = await response.json();
    console.log(`1/7 Tenant created. Schema: ${data.tenantName}`);
    return data.tenantName;
  }

  if (response.status === 409) {
    console.log('1/7 Tenant already exists, skipping.');
    return `${TENANT_NAME}`;
  }

  const text = await response.text();
  throw new Error(`Failed to create tenant (${response.status}): ${text}`);
}

async function initTenant(schemaName) {
  console.log('2/7 Initializing Tenant Data...');

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
    console.log('2/7 Tenant initialized.');
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

async function getDocTypeFieldMap(schemaName) {
  const data = await graphqlQuery(
    schemaName,
    `{ docTypeFields(first: 500) { edges { node { id name } } } }`,
    'Query DocTypeFields'
  );

  const map = new Map();
  for (const edge of data?.docTypeFields?.edges ?? []) {
    map.set(edge.node.name, edge.node.id);
  }
  return map;
}

async function findEntityIdByName(schemaName, queryField, name, label) {
  const data = await graphqlQuery(
    schemaName,
    `{ ${queryField}(first: 100) { edges { node { id name } } } }`,
    label
  );

  return data?.[queryField]?.edges?.find(e => e.node.name === name)?.node.id ?? null;
}

async function ensureSubField(schemaName, fieldMap, parentName, leafName) {
  const fullName = `${parentName}.${leafName}`;

  if (fieldMap.has(fullName)) {
    console.log(`  Sub-field "${fullName}": already exists, skipping.`);
    return fieldMap.get(fullName);
  }

  const parentId = fieldMap.get(parentName);

  if (parentId == null) {
    console.warn(`  Parent field "${parentName}" not found, cannot create "${fullName}".`);
    return null;
  }

  const data = await graphqlQuery(
    schemaName,
    `mutation {
      createSubField(parentDocTypeFieldId: ${parentId}, docTypeFieldDTO: {
        name: "${fullName}"
        fieldName: "${leafName}"
        fieldType: SEARCH_AS_YOU_TYPE
        searchable: true
        sortable: false
      }) { entity { id } }
    }`,
    `Create sub-field ${fullName}`
  );

  const id = data?.createSubField?.entity?.id ?? null;
  console.log(id != null
    ? `  Sub-field "${fullName}" created.`
    : `  Sub-field "${fullName}": creation failed.`);
  return id;
}

async function ensureAutocorrection(schemaName, name, docTypeFieldId) {
  const existing = await findEntityIdByName(schemaName, 'autocorrections', name, 'Query Autocorrections');

  if (existing != null) {
    console.log(`  Autocorrection "${name}": already exists, skipping.`);
    return existing;
  }

  const data = await graphqlQuery(
    schemaName,
    `mutation {
      autocorrection(autocorrectionDTO: {
        name: "${name}"
        autocorrectionDocTypeFieldId: ${docTypeFieldId}
      }) { entity { id } }
    }`,
    `Create Autocorrection ${name}`
  );

  const id = data?.autocorrection?.entity?.id ?? null;
  console.log(id != null
    ? `  Autocorrection "${name}" created.`
    : `  Autocorrection "${name}": creation failed.`);
  return id;
}

async function ensureAutocomplete(schemaName, name, fieldId) {
  const existing = await findEntityIdByName(schemaName, 'autocompletes', name, 'Query Autocompletes');

  if (existing != null) {
    console.log(`  Autocomplete "${name}": already exists, skipping.`);
    return existing;
  }

  const data = await graphqlQuery(
    schemaName,
    `mutation {
      autocomplete(autocompleteDTO: {
        name: "${name}"
        fieldIds: [${fieldId}]
      }) { entity { id } }
    }`,
    `Create Autocomplete ${name}`
  );

  const id = data?.autocomplete?.entity?.id ?? null;
  console.log(id != null
    ? `  Autocomplete "${name}" created.`
    : `  Autocomplete "${name}": creation failed.`);
  return id;
}

async function ensureSuggestionCategory(schemaName, name, priority, docTypeFieldId) {
  const existing = await findEntityIdByName(schemaName, 'suggestionCategories', name, 'Query Suggestion Categories');

  if (existing != null) {
    console.log(`  Suggestion Category "${name}": already exists, skipping.`);
    return existing;
  }

  const data = await graphqlQuery(
    schemaName,
    `mutation {
      suggestionCategoryWithDocTypeField(suggestionCategoryWithDocTypeFieldDTO: {
        name: "${name}"
        priority: ${priority}
        multiSelect: true
        docTypeFieldId: ${docTypeFieldId}
      }) { entity { id } }
    }`,
    `Create Suggestion Category ${name}`
  );

  const id = data?.suggestionCategoryWithDocTypeField?.entity?.id ?? null;
  console.log(id != null
    ? `  Suggestion Category "${name}" created.`
    : `  Suggestion Category "${name}": creation failed.`);
  return id;
}

async function bindToBucket(schemaName, mutation, idArg, bucketId, entityId, label) {
  const result = await graphqlQuery(
    schemaName,
    `mutation { ${mutation}(bucketId: ${bucketId}, ${idArg}: ${entityId}) { left { id } } }`,
    `Bind ${label}`
  );

  console.log(result
    ? `  ${label} bound to Default Bucket.`
    : `  ${label}: bind failed.`);
}

async function configureSearchFeatures(schemaName) {
  console.log('4/7 Configuring search features...');

  const bucketData = await graphqlQuery(
    schemaName,
    `{ buckets(first: 10, searchText: "Default Bucket") { edges { node { id name } } } }`,
    'Query Default Bucket'
  );

  const bucketId = bucketData?.buckets?.edges
    ?.find(e => e.node.name === 'Default Bucket')?.node.id;

  if (bucketId == null) {
    console.warn('  Default Bucket not found, skipping search features.');
    return;
  }

  // DocTypeFields are auto-generated when the plugin driver is registered (step 3).
  const fieldMap = await getDocTypeFieldMap(schemaName);

  // Autocorrection on web.content
  const contentFieldId = fieldMap.get('web.content');
  if (contentFieldId == null) {
    console.warn('  web.content not found, skipping Autocorrection.');
  }
  else {
    const id = await ensureAutocorrection(schemaName, 'Default Autocorrection', contentFieldId);
    if (id != null) {
      await bindToBucket(schemaName, 'bindAutocorrectionToBucket', 'autocorrectionId', bucketId, id, 'Autocorrection');
    }
  }

  // Autocomplete on web.title.search_as_you_type (sub-field of web.title)
  const saytFieldId = await ensureSubField(schemaName, fieldMap, 'web.title', 'search_as_you_type');
  if (saytFieldId == null) {
    console.warn('  search_as_you_type field unavailable, skipping Autocomplete.');
  }
  else {
    const id = await ensureAutocomplete(schemaName, 'Default Autocomplete', saytFieldId);
    if (id != null) {
      await bindToBucket(schemaName, 'bindAutocompleteToBucket', 'autocompleteId', bucketId, id, 'Autocomplete');
    }
  }

  // Suggestion Category on web.title.keyword
  const titleKeywordFieldId = fieldMap.get('web.title.keyword');
  if (titleKeywordFieldId == null) {
    console.warn('  web.title.keyword not found, skipping Suggestion Category.');
  }
  else {
    const id = await ensureSuggestionCategory(schemaName, 'Web Title', 1.0, titleKeywordFieldId);
    if (id != null) {
      await bindToBucket(schemaName, 'addSuggestionCategoryToBucket', 'suggestionCategoryId', bucketId, id, 'Suggestion Category');
    }
  }

  console.log('4/7 Search features done.');
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

async function getSelectedDocTypeIds(schemaName, pluginDriverId) {
  const response = await fetchWithRetry(
    `${DATASOURCE_API}/api/datasource/pluginDrivers/documentTypes/${pluginDriverId}`,
    {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Authorization': authHeader,
        'X-TENANT-ID': schemaName
      }
    },
    `Get DocTypes for PluginDriver ${pluginDriverId}`
  );

  if (!response.ok) {
    console.warn(`  Could not fetch document types for plugin driver ${pluginDriverId} (${response.status}).`);
    return [];
  }

  const body = await response.json();
  const docTypes = Array.isArray(body) ? body : (body.docTypes ?? []);
  return docTypes.filter(dt => dt.selected).map(dt => dt.docTypeId);
}

async function ensureDatasource(schemaName, name, description, pluginDriverId, jsonConfig, dataIndex, docTypeIds) {
  const jsonConfigEscaped = JSON.stringify(jsonConfig).replace(/"/g, '\\"');

  const docTypeIdsField = docTypeIds && docTypeIds.length
    ? `\n          docTypeIds: [${docTypeIds.join(', ')}]`
    : '';

  const payload = {
    query: `mutation {
      createDatasourceConnection(datasourceConnection: {
        name: "${name}"
        description: "${description}"
        pluginDriverId: ${pluginDriverId}
        jsonConfig: "${jsonConfigEscaped}"
        schedulable: false
        dataIndex: {
          name: "${dataIndex.name}"${docTypeIdsField}
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
  console.log('3/7 Configuring Connectors...');
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

  console.log(`3/7 Connectors done (${created} created, ${skipped} skipped, ${failed} failed).`);

  // --- Step 4: Search features ---
  await configureSearchFeatures(schemaName);

  // --- Step 5: Datasources ---
  console.log('5/7 Configuring Datasources...');
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
        const docTypeIds = await getSelectedDocTypeIds(schemaName, pluginDriverId);
        await ensureDatasource(
          schemaName,
          ds.name,
          ds.description,
          pluginDriverId,
          ds.jsonConfig,
          ds.dataIndex,
          docTypeIds
        );
        created++;
      }
      catch (err) {
        console.warn(`  ${ds.name}: failed, skipping. (${err.message})`);
        failed++;
      }
    }
  }

  console.log(`5/7 Datasources done (${created} created, ${skipped} skipped, ${failed} failed).`);

  // --- Step 6: Link datasources to Default Bucket ---
  console.log('6/7 Linking Datasources to Default Bucket...');

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
        `mutation { addDatasourceToBucket(bucketId: ${bucketId}, datasourceId: ${dsMatch.node.id}) { left { id } } }`,
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

    console.log(`6/7 Bucket linking done (${linked} linked).`);
  }

  console.log('7/7 Done.');
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
