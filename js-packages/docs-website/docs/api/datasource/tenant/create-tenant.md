---
id: create-tenant
title: Create Tenant API
slug: /api/create-tenant

---

Create new Tenant

```bash
POST /v2/tenant
{
	"name": "openk9.io",
	"virtualHost": "openk9.io",
	"jsonConfig": {
      "querySourceBarShortcuts": [
        { "id": "web", "text": "web" },
        { "id": "document", "text": "document" },
        { "id": "pdf", "text": "pdf" }
      ]
    }
}
```

### Description

Create new tenant and define his configuration.

### Request Body

`name`: (string) Name of tenant

`virtualHost`: (string) VirtualHost associated with tenant

`jsonConfig`: (string) Json configuration for tenant (i.e. configuration of shortcuts for search standalone frontend)
