---
id: create-enrichitem
title: Create enrichitem API
slug: /api/create-enrichitem

---

Create new enrichitem

```bash
POST /v2/enrichitem
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

Allows you to create a new datasource.

### Request Body

`active`: (boolean) Specific if datasource is enabled or not

`description`: (string) Description about the datasource

`jsonConfig`: (dict) Configuration of the datasource. Normally includes the information to be sent to the external connector.

`name`: (date) Name of datasource

`tenantId`: (int) Id of related tenant

`scheduling`: (cron expression) Quartz cron expression to adjust the scheduler

`driverServiceName`: (string) Symbolic name of related plugin


