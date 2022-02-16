module.exports = {
  sidebar: {
    OpenK9: ["intro", "standalone-app", "headless-api"],
    "Getting Started": ["using-docker"],
    "OSGi project": [
    "osgi-requirements",
    "deploy-osgi-project",
    {
     "type": "category",
     "label": "Create Plugin",
     "items": [
       "create-plugin-using-java",
       "create-plugin-using-json",
       "create-external-data-parser"
     ]
   }],
    "Architecture": ["architecture/architecture", "architecture/ingestion", "architecture/datasource",
    "architecture/plugin-driver-manager", "architecture/index-writer", "architecture/entity-manager",
    "architecture/searcher"],
    "Rest Api": ["api/api",
    {
       "type": "category",
       "label": "Searcher Apis",
       "items": [
         "api/searcher/searcher-api",
         "api/searcher/search/search-documents",
         "api/searcher/search/search-entities",
         "api/searcher/search/supported-datasources",
         "api/searcher/search/suggestions",
         "api/searcher/query-analysis/query-analysis",
         "api/searcher/auth/login",
         "api/searcher/auth/logout",
         "api/searcher/auth/user-info"
       ]
     },
     {
      "type": "category",
      "label": "Datasource Apis",
      "items": [
        "api/datasource/datasource-api-overwiew",
        "api/datasource/tenant/tenant-api",
        "api/datasource/datasource/datasource-api",
        "api/datasource/enrichpipeline/enrichpipeline-api",
        "api/datasource/enrichitem/enrichitem-api",
        "api/datasource/suggestion-category/suggestion-category-api",
        "api/datasource/suggestion-category-field/suggestion-category-field-api",
        "api/datasource/reindex/reindex-api"
      ]
    },
    {
      "type": "category",
      "label": "Ingestion Apis",
      "items": [
        "api/ingestion/ingestion-api",
        "api/ingestion/ingestion"
      ]
    },
    {
      "type": "category",
      "label": "Index Writer Apis",
      "items": [
        "api/index-writer/index-writer-api",
        "api/index-writer/delete-data-documents-api",
        "api/index-writer/clean-orphan-entities-api"
      ]
    }
    ]
    // "Data Source Plugins": ["datasource"],
    // "Enrich Plugins": ["enrich"],
    // "UI Components": ["ui"],
  },
};
