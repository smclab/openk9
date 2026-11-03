---
id: tenant-authentication
title: Tenant authentication
---

Every tenant is bound to a Keycloak realm and to an OAuth2 client of that realm.
The tenant stores the realm name, the client id and, optionally, the client secret.

Admin UI, Search Frontend and Talk-to read the realm and the client id from
`/api/datasource/oauth2/settings.js` and log the user in with the
Authorization Code flow and PKCE `S256`.
They use no other grant.

This page explains how the client must be configured.
Only Keycloak is supported: the tenant is always a realm of the Keycloak configured for OpenK9,
and the frontends use keycloak-js.

## Where the client comes from

### Realm created by the Tenant Manager

When you create a tenant from the Tenant Manager Admin UI and fill in only the *Virtual Host*,
the Tenant Manager creates a Keycloak realm for the tenant with a client called `openk9`.

The `openk9` client is created with the recommended configuration described below:
a public client with only the standard flow enabled and PKCE `S256` required.
You don't need to change anything.

Realms created by an older version of OpenK9 don't require PKCE.
To align them, see [Align the realms of existing tenants](#align-the-realms-of-existing-tenants).

### Existing realm

When you enable *Configure all fields* and fill in *Realm Name* and *Client Id*,
the Tenant Manager doesn't create any realm: it binds the tenant to the realm and the client you give.

In this case you create and configure the client yourself,
following the recommended configuration.

## Recommended client configuration

| Setting | Value | Keycloak admin console |
| --- | --- | --- |
| Client type | Public | *Settings* > *Capability config* > *Client authentication* Off |
| Authorization Code flow | Enabled | *Standard flow* checked |
| Password grant | Disabled | *Direct access grants* unchecked |
| Implicit flow | Disabled | *Implicit flow* unchecked |
| Client credentials grant | Disabled | *Service accounts roles* unchecked |
| Device and CIBA grants | Disabled | *OAuth 2.0 Device Authorization Grant* and *OIDC CIBA Grant* unchecked |
| PKCE | Required, method `S256` | Up to Keycloak 26.2: *Advanced* > *Advanced settings* > *Proof Key for Code Exchange Code Challenge Method* `S256`; Keycloak 26.3 and later: *Settings* > *Capability config* > *PKCE Method* `S256` |
| Redirect URIs | Only the tenant virtual host | *Valid redirect URIs* `https://<virtual-host>/*` |
| Web origins | Only the tenant virtual host | *Web origins* `https://<virtual-host>` (or `+`, the origins of the redirect URIs) |

## Confidential client and client secret

The client of the tenant is used by the browser, so keep it public.

If the tenant has a client secret, the OpenK9 services use it only on the server side:
`/api/datasource/oauth2/settings.js` doesn't return it.
Even so, on a client with a secret:

- enable only the standard flow (Authorization Code) with PKCE `S256`;
- never enable client credentials (service accounts) or the password grant (direct access):
  whoever gets the secret could get tokens without any user.

If a service needs machine-to-machine access, give it its own client,
separate from the client of the tenant.

## Align the realms of existing tenants

The `openk9` client gets the recommended configuration only in the realms
created after the upgrade.
The realms of the tenants created before don't change: PKCE is not required.

The OpenK9 frontends already use PKCE `S256`, so requiring it doesn't affect them.
An external application that uses the `openk9` client without PKCE must enable it first.

Repeat the following steps for the realm of each tenant.

### From the Keycloak admin console

1. Select the realm of the tenant.
2. Open *Clients* > `openk9` > *Settings*.
3. In *Capability config*, keep *Client authentication* Off and,
   in *Authentication flow*, keep only *Standard flow* checked.
   Uncheck *Direct access grants*, *Implicit flow*, *Service accounts roles*,
   *OAuth 2.0 Device Authorization Grant* and *OIDC CIBA Grant*.
4. Require PKCE `S256`:
   - up to Keycloak 26.2: save, open the *Advanced* tab and, in *Advanced settings*, set
     *Proof Key for Code Exchange Code Challenge Method* to `S256`. Save;
   - Keycloak 26.3 and later: in the same *Capability config*, set *PKCE Method* to `S256`. Save.

### With kcadm.sh

```bash
# log in to the master realm with an administrator
# (without --password, kcadm.sh asks for the password interactively)
kcadm.sh config credentials --server https://<keycloak-host> \
  --realm master --user <admin-user> --password <admin-password>

# find the internal id of the openk9 client in the realm of the tenant
CID=$(kcadm.sh get clients -r <tenant-realm> -q clientId=openk9 \
  --fields id --format csv --noquotes)

# keep only the standard flow and require PKCE S256
kcadm.sh update clients/$CID -r <tenant-realm> \
  -s directAccessGrantsEnabled=false \
  -s implicitFlowEnabled=false \
  -s serviceAccountsEnabled=false \
  -s 'attributes."pkce.code.challenge.method"=S256'

# check the result
kcadm.sh get clients/$CID -r <tenant-realm>
```

In the output, `directAccessGrantsEnabled`, `implicitFlowEnabled` and `serviceAccountsEnabled`
must be `false` and `attributes` must contain `"pkce.code.challenge.method" : "S256"`.

The same steps apply to the `tenant-manager` client of the `tenant-manager` realm,
used by the Tenant Manager Admin UI (see [First configuration](../first-configuration.md)).

### Check the client

An authorization request without `code_challenge` must end with an error
(`invalid_request`, missing `code_challenge`) instead of the login page:

```bash
curl -s -o /dev/null -w '%{http_code} %{redirect_url}\n' \
  "https://<keycloak-host>/realms/<tenant-realm>/protocol/openid-connect/auth?client_id=openk9&response_type=code&scope=openid&redirect_uri=https%3A%2F%2F<virtual-host>%2F"
```

A token request with the password grant must be refused with `unauthorized_client`:

```bash
curl -s https://<keycloak-host>/realms/<tenant-realm>/protocol/openid-connect/token \
  -d grant_type=password -d client_id=openk9 \
  -d username=<user> -d password=<password>
```

Finally, log in to Admin UI and Search Frontend of the tenant to check that the login still works.
