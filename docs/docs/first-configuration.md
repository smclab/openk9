---
id: first-configuration
title: First configuration
---


This is the procedure to perform first configuration of Openk9.

When Openk9 is installed, you need to:

1. Configure realm on Keycloak for Tenant Manager Admin UI
2. Create your first tenant
3. Configure your first datasource

For Advanced configuration go to [related section](./configuration/overview.md)

### Tenant Manager Keycloak Configuration

Acces Keycloak Admin console and create new realm. Call it *tenant-manager*.

![tenant-manager-create-realm](../static/img/tenant-manager-create-realm.png)

Then create a new client and call it *tenant-manager*. If you named differently when installed Openk9, call it with same name.

![create-client-tenant-manager](../static/img/create-client-tenant-manager.png)

Mantains default choices in section *Capability config* and proceed to *login settings* section.

Set *Valid redirect URIs* with url configured for tenant manager.

![login-settings-tenant-manager](../static/img/login-settings-tenant-manager.png)

Create then a new role *admin*.

![tenant-manager-new-role-admin](../static/img/tenant-manager-new-role-admin.png)

After this, create a new user *tenant-manager-admin* and set password for this user.

![tenant-manager-new-user](../static/img/tenant-manager-new-user.png)

When create, add *admin* role to *tenant-manager-admin* user.

![add-admin-role-to-tenant-manager-user](../static/img/add-admin-role-to-tenant-manager-user.png)

Keycloak configuration is completed and now you can access to Tenant Manager Admin UI to create your first tenant.

### First tenant creation

Access to Tenant Manager Admin UI with user previously created. It is accessible to url https:://tenant-manager-url/admin. use

When accessed, go to Tenants section to create your first tenant and click on *add new* button.

Insert virtualhost for tenant and click on create.

![new-tenant](../static/img/new-tenant.png)

Wait until tenant is created. When created you view it on tenant list recap.

![tenant-list](../static/img/tenant-list.png)

Now you can proceed to Admin Ui to connect your first datasource.


### Configure and start your first datasource

