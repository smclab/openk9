---
id: logout
title: Logout API
slug: /api/logout

---

Logout from Openk9

```bash
POST v1/auth/logout
{
	"accessToken": "{{ _.TOKEN }}",
	"refreshToken": ""
}
```

### Description

Allow to log out from Openk9

### Request Body

`accessToken`: (string)

`refreshToken`: (string)


