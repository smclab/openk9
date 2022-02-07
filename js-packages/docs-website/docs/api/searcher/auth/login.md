---
id: login
title: Login API
slug: /api/login

---

Login on Openk9

```bash
POST v1/auth/login
{
	"username": "demo",
	"password": "demo"
}
```

### Description

Allows you to log into Openk9. You log in specifying username and password.

### Request Body

`username`: (string)

`password`: (string)
