---
id: patch-tenant
title: patch tenant API
slug: /api/patch-tenant

---

Patch tenant

```bash
PATCH /v2/tenant
{
    "active": false
}
```

### Description

Allows you to update individual information in tenant

### Request Body

`name`: (string) Name of tenant

`virtualHost`: (string) VirtualHost associated with tenant

`jsonConfig`: (dict) Json configuration for tenant (i.e. configuration of shortcuts for search standalone frontend)
