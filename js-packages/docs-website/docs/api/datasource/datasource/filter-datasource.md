---
id: filter-datasource
title: Filter Datasource API
slug: /api/filter-datasource

---

Filter datasource

```bash
POST /v2/datasource/filter
{
  "name": "test-datasource"
}
```

### Description

Get datasources which match filter information passed in request body

### Request Body

`active`: (boolean) Specific if datasource is enabled or not

`description`: (string) Description about the datasource

`jsonConfig`: (string) Configuration of the datasource. Normally includes the information to be sent to the external connector.

`name`: (string) Name of datasource

`tenantId`: (integer) Id of related tenant

`scheduling`: (string) Quartz cron expression to adjust the scheduler

`driverServiceName`: (string) Symbolic name of related plugin

`lastIngestionDate`: (date) Symbolic name of related plugin
