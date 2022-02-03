---
id: patch-datasource
title: patch datasource API
slug: /api/patch-datasource

---

Patch existing datasource

```bash
PATCH /v2/datasource
{
    "active": false
}
```

### Description

Allows you to update individual information in datasource

### Request Body

`active`: (boolean) Specific if datasource is enabled or not

`description`: (string) Description about the datasource

`jsonConfig`: (dict) Configuration of the datasource. Normally includes the information to be sent to the external connector.

`name`: (date) Name of datasource

`tenantId`: (int) Id of related tenant

`scheduling`: (cron expression) Quartz cron expression to adjust the scheduler

`driverServiceName`: (string) Symbolic name of related plugin
