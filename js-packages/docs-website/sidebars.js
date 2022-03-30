module.exports = {
  sidebar: {
    OpenK9: ["intro", "standalone-app", "headless-api"],
    "Getting Started": ["using-docker",
          {
            "type": "category",
            "label": "Getting Started using Kubernetes",
            "items": [
                            "kubernetes-configuration",
                            "openk9-prerequisites",
                            "kubernetes-openk9-core",
                            "kubernetes-openk9-parsers",
                            "kubernetes-openk9-enrichers"
                    ]
                    },
          "keycloak-configuration", "openk9-configuration", "using-frontend"],
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
        {
          "type": "category",
          "label": "Search Apis",
          "items": [
             "api/searcher/search/search-documents",
             "api/searcher/search/search-entities",
             "api/searcher/search/supported-datasources",
             "api/searcher/search/suggestions"
          ]
        },
        {
           "type": "category",
           "label": "Auth Apis",
           "items": [
              "api/searcher/auth/login",
              "api/searcher/auth/logout",
              "api/searcher/auth/user-info"
           ]
         },
         {
          "type": "category",
          "label": "Query Understanding Apis",
          "items": [
            "api/searcher/query-understanding/query-analysis",
            "api/searcher/query-understanding/query-autocomplete"
          ]
        },
       ]
     },
     {
      "type": "category",
      "label": "Datasource Apis",
      "items": [
        "api/datasource/datasource-api-overwiew",
        {
          "type": "category",
          "label": "Tenant Apis",
          "items": [
            "api/datasource/tenant/tenant-api",
            "api/datasource/tenant/create-tenant",
            "api/datasource/tenant/delete-tenant",
            "api/datasource/tenant/filter-tenant",
            "api/datasource/tenant/get-tenant",
            "api/datasource/tenant/get-tenant-list",
            "api/datasource/tenant/patch-tenant",
            "api/datasource/tenant/update-tenant"
          ]
        },
        {
          "type": "category",
          "label": "Datasource Apis",
          "items": [
            "api/datasource/datasource/datasource-api",
            "api/datasource/datasource/create-datasource",
            "api/datasource/datasource/delete-datasource",
            "api/datasource/datasource/filter-datasource",
            "api/datasource/datasource/get-datasource",
            "api/datasource/datasource/get-datasource-list",
            "api/datasource/datasource/patch-datasource",
            "api/datasource/datasource/update-datasource"
          ]
        },
        {
          "type": "category",
          "label": "Enrich Pipeline Apis",
          "items": [
            "api/datasource/enrichpipeline/enrichpipeline-api",
            "api/datasource/enrichpipeline/create-enrichpipeline",
            "api/datasource/enrichpipeline/delete-enrichpipeline",
            "api/datasource/enrichpipeline/filter-enrichpipeline",
            "api/datasource/enrichpipeline/get-enrichpipeline",
            "api/datasource/enrichpipeline/get-enrichpipeline-list",
            "api/datasource/enrichpipeline/patch-enrichpipeline",
            "api/datasource/enrichpipeline/update-enrichpipeline"
          ]
        },
        {
          "type": "category",
          "label": "Enrich Item Apis",
          "items": [
            "api/datasource/enrichitem/enrichitem-api",
            "api/datasource/enrichitem/create-enrichitem",
            "api/datasource/enrichitem/delete-enrichitem",
            "api/datasource/enrichitem/filter-enrichitem",
            "api/datasource/enrichitem/get-enrichitem",
            "api/datasource/enrichitem/get-enrichitem-list",
            "api/datasource/enrichitem/patch-enrichitem",
            "api/datasource/enrichitem/update-enrichitem"
          ]
        },
        {
          "type": "category",
          "label": "Suggestion Category Apis",
          "items": [
            "api/datasource/suggestion-category/suggestion-category-api",
            "api/datasource/suggestion-category/create-suggestion-category",
            "api/datasource/suggestion-category/delete-suggestion-category",
            "api/datasource/suggestion-category/filter-suggestion-category",
            "api/datasource/suggestion-category/get-suggestion-category",
            "api/datasource/suggestion-category/get-suggestion-category-list",
            "api/datasource/suggestion-category/patch-suggestion-category",
            "api/datasource/suggestion-category/update-suggestion-category"
          ]
        },
        {
          "type": "category",
          "label": "Suggestion Category Field Apis",
          "items": [
            "api/datasource/suggestion-category-field/suggestion-category-field-api",
            "api/datasource/suggestion-category-field/create-suggestion-category-field",
            "api/datasource/suggestion-category-field/delete-suggestion-category-field",
            "api/datasource/suggestion-category-field/filter-suggestion-category-field",
            "api/datasource/suggestion-category-field/get-suggestion-category-field",
            "api/datasource/suggestion-category-field/get-suggestion-category-field-list",
            "api/datasource/suggestion-category-field/patch-suggestion-category-field",
            "api/datasource/suggestion-category-field/update-suggestion-category-field"
          ]
        },
        {
          "type": "category",
          "label": "Reindex Apis",
          "items": [
            "api/datasource/reindex/reindex-api",
            "api/datasource/reindex/reindex",
            "api/datasource/reindex/trigger"
          ]
        }
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
