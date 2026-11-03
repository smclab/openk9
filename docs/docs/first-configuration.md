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

In section *Capability config* keep *Client authentication* Off and, in *Authentication flow*, check only *Standard flow*.
Uncheck *Direct access grants*, that the console enables by default: the Tenant Manager Admin UI doesn't use the password grant. Proceed to *login settings* section.

Set *Valid redirect URIs* with url configured for tenant manager.

![login-settings-tenant-manager](../static/img/login-settings-tenant-manager.png)

Require PKCE `S256`. Up to Keycloak 26.2, save the client, open its *Advanced* tab and, in *Advanced settings*, set *Proof Key for Code Exchange Code Challenge Method* to `S256`, then save again. From Keycloak 26.3, set *PKCE Method* to `S256` directly in *Capability config*.

The same rules apply to the OAuth2 client of each tenant: see [Tenant authentication](./configuration/tenant-authentication.md).

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

