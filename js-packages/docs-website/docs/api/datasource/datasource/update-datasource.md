---
id: update-datasource
title: Update Datasource API
slug: /api/update-datasource

---

Update existing datasource

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

Update entire datasource with information specified in request body

### Request Body

`active`: (boolean) Specific if datasource is enabled or not

`description`: (string) Description about the datasource

`jsonConfig`: (string) Configuration of the datasource. Normally includes the information to be sent to the external connector.

`name`: (string) Name of datasource

`tenantId`: (integer:$int64) Id of related tenant

`scheduling`: (string) Quartz cron expression to adjust the scheduler

`driverServiceName`: (string) Symbolic name of related plugin

`lastIngestionDate`: (string:$date-time) Symbolic name of related plugin
