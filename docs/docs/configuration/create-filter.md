---
id: create-filter
title: Create filter
---

This is the procedure to create and user new filter in Openk9.

To crete a new filter in Openk9:

1. Go to filters section under search config in left side menu
2. Click on *create new filter* button in filter listing view
3. Insert into form correct informations, save and continue until filter is correctly created.

In following image there is an example of form correctly compilated.

![create-filter](../static/img/create-filter.png)

To create new filter you must insert:

- name
- optional description
- priority: is the url where preinstalled connector is reachable
- multiSelect: is the REST api path used to triggere data extraction
- doc type file: HTTP method associated with path
- secure: set to true if baseUri is under http secure

Once filter is created, you can add it to your active bucket in following ways:

- using add shorcut present in filters listing view
- editing your active bucket associating new filter



