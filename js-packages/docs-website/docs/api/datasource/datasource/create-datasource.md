---
id: create-datasource
title: Create datasource API
slug: /api/create-datasource

---

Create new datasource

```bash
POST /v2/datasource
{
    "active": true,
    "description": "example",
    "jsonConfig": "{}",
    "lastIngestionDate": 0,
    "name": "example-datasource",
    "tenantId": 1,
    "scheduling": "0 */30 * ? * *",
    "driverServiceName": "io.openk9.plugins.example.driver.ExamplePluginDriver"
}
```

### Description

Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard
dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.

### Request Body
