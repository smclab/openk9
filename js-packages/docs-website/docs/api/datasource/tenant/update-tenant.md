---
id: update-tenant
title: Update Tenant API
slug: /api/update-tenant

---

Update existing Tenant


```bash
PUT /v2/tenant
{
    "tenantId": 2,
    "name": "openk9.io",
    "virtualHost": "openk9.io",
    "jsonConfig": {
          "querySourceBarShortcuts": [
            { "id": "web", "text": "web" },
            { "id": "excel", "text": "excel" }
          ]
        }
  }
```

### Description

Update existing tenant, changing its name, virtualHost or configuration.

### Request Body

`name`: (string) Name of tenant

`virtualHost`: (string) VirtualHost associated with tenant

`jsonConfig`: (string) Json configuration for tenant (i.e. configuration of shortcuts for search standalone frontend)
