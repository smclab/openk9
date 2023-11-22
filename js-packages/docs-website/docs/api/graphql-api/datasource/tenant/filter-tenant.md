---
id: filter-tenant
title: Filter Tenant API
slug: /api/filter-tenant

---

Filter tenant

```bash
POST /v2/tenant/filter
{
  "name": "test.openk9.io"
}
```

### Description

Get tenants that match filter information passed in request body

### Request Body

`name`: (string) Name of tenant

`virtualHost`: (string) VirtualHost associated with tenant

`jsonConfig`: (string) Json configuration for tenant (i.e. configuration of shortcuts for search standalone frontend)
