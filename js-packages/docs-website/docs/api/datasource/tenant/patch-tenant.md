---
id: patch-tenant
title: patch Tenant API
slug: /api/patch-tenant

---

Patch tenant

```bash
PATCH /v2/tenant
{
    "name": "test.openk9.io"
}
```

### Description

Allows you to update individual information in tenant

### Request Body

`name`: (string) Name of tenant

`virtualHost`: (string) VirtualHost associated with tenant

`jsonConfig`: (string) Json configuration for tenant (i.e. configuration of shortcuts for search standalone frontend)
