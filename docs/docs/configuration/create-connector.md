---
id: create-connector
title: Create connector
---

This is the procedure to create and connect new connector to Openk9.

To connect a new connector to Openk9, you must before install connector and make sure this is reachable via http REST call.

Once connector is installed, you can:

1. Go to connectors section under Datasource and Data enrichments in left side menu
2. Click on *create new connector* button in connector listing view
3. Insert into form correct informations, save and continue until connector is correctly created and connected.

In following image there is an example of form correctly compilated.

![create-connector](../static/img/create-connector.png)

To connect new connector you must insert:

- name
- optional description
- baseUri: is the url where preinstalled connector is reachable
- path: is the REST api path used to triggere data extraction
- method: HTTP method associated with path
- secure: set to true if baseUri is under http secure