module.exports = {
  sidebar: {
    OpenK9: ["intro"],
    "Getting Started": [
    		{
                "type": "category",
                "label": "Install Openk9",
    			"items": [
    			"types-of-installation", "using-docker",
          {
            "type": "category",
            "label": "Install on Kubernetes or Openshift",
            "items": [
                            "kubernetes-openshift-installation/kubernetes-configuration",
                            "kubernetes-openshift-installation/kubernetes-openk9-prerequisites",
                            "kubernetes-openshift-installation/kubernetes-openk9-core",
                           	"kubernetes-openshift-installation/kubernetes-frontends",
                            "kubernetes-openshift-installation/kubernetes-openk9-file-manager-pack",
                            "kubernetes-openshift-installation/kubernetes-openk9-graph",
                            "kubernetes-openshift-installation/kubernetes-openk9-connectors"
                    ]
                    }
                    ]
                    },
          "keycloak-configuration"],
//    "OSGi project": [
//    "osgi-requirements",
//    "deploy-osgi-project",
//    {
//     "type": "category",
//     "label": "Create Plugin",
//     "items": [
//       "create-plugin-using-java",
//       "create-plugin-using-json",
//       "create-external-data-parser"
//     ]
//   }],
   "Openk9 Configuration": ["configuration/overview", "configuration/add-data-source",
"configuration/bucket-configuration"],
   "Develop your UI": ["standalone-app", "embeddable-app", "headless-api"],
    "Architecture": ["architecture/architecture", "architecture/ingestion", "architecture/datasource",
    "architecture/tenant-manager", "architecture/entity-manager", "architecture/file-manager",
    "architecture/searcher"],
    "APIs": [
	  {
		"type": "category",
		"label": "Rest APIs",
		"items": ["api/rest-api/rest-api",
		"api/rest-api/consuming-rest-api",
    {
       "type": "category",
       "label": "Searcher Apis",
       "items": [
                "api/rest-api/searcher/searcher-api",
        {
          "type": "category",
          "label": "Search Apis",
          "items": [
             "api/rest-api/searcher/search/search-documents",
             "api/rest-api/searcher/search/suggestions"
          ]
        },
         {
          "type": "category",
          "label": "Query Understanding Apis",
          "items": [
            "api/rest-api/searcher/query-understanding/query-analysis",
            "api/rest-api/searcher/query-understanding/query-autocomplete"
          ]
        },
       ]
     },
     {
      "type": "category",
      "label": "Datasource Apis",
      "items": [
        "api/rest-api/datasource/datasource-api-overwiew",

        {
          "type": "category",
          "label": "Reindex Apis",
          "items": [
            "api/rest-api/datasource/reindex/reindex-api",
            "api/rest-api/datasource/reindex/reindex"
          ]
        },
        {
		  "type": "category",
		  "label": "Trigger Apis",
		  "items": [
			"api/rest-api/datasource/trigger/trigger-api",
			"api/rest-api/datasource/trigger/trigger"
		  ]
		},
		{
		  "type": "category",
		  "label": "Data Index Apis",
		  "items": [
			"api/rest-api/datasource/trigger/trigger-api",
			"api/rest-api/datasource/trigger/trigger"
		  ]
		},
		{
		  "type": "category",
		  "label": "Bucket Apis",
		  "items": [
			"api/rest-api/datasource/trigger/trigger-api",
			"api/rest-api/datasource/trigger/trigger"
		  ]
		}
      ]
    },
    {
      "type": "category",
      "label": "Ingestion Apis",
      "items": [
        "api/rest-api/ingestion/ingestion-api",
        "api/rest-api/ingestion/ingestion"
      ]
    },
    {
	  "type": "category",
	  "label": "Tenant Manager Apis",
	  "items": [
		"api/rest-api/tenant-manager/tenant-manager-api"
	  ]
	}
    ]},
      {
    		"type": "category",
    		"label": "Graphql APIs",
    		"items": ["api/graphql-api/graphql-api"]}
    ]
    // "Data Source Plugins": ["datasource"],
    // "Enrich Plugins": ["enrich"],
    // "UI Components": ["ui"],
  },
};
