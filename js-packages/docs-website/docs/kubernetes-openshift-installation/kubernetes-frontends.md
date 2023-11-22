---
id: kubernetes-frontends
title: Openk9 UIs
---

### Preparing the installation

Is required to configure Keycloak before install Admin e access through credentials.
So, if not already done go to the appropriate [section](keycloak-configuration) and configure keycloak.

OpenK9 has a standalone search frontend and an admin frontend.
Currently installing through [Helm Charts](https://helm.sh/docs/topics/charts/) is the best choice.

Inside the [openk9-helm-charts repository](https://github.com/smclab/openk9-helm-charts) there is the
`kubernetes/01-core-charts` folder where there are helm charts to install these components.

So clone this repository before start to install.

## Tenant UI

Tenant v component represents an admin ui to handle configuration aspects of Openk9.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install tenant-ui 01-core-charts/openk9-tenant-ui \
  -n openk9 \
  -f 01-core-charts/openk9-tenant-ui/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-tenant-ui" -o name)
```

### Access Console

Access to Openk9 tenant ui console using url [http://demo.openk9.local/admin](http://demo.openk9.local/admin)

![image-20220303214347128](../../static/img/installation/image-20220303214347128.png)

Log in with credentials ("test:test")

![image-20220303214849436](../../static/img/installation/image-20220303214849436.png)

## Admin UI

Admin UI component represents an admin ui to handle configuration aspects of Openk9.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install admin-ui 01-core-charts/openk9-admin-ui \
  -n openk9 \
  -f 01-core-charts/openk9-admin-ui/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-admin-ui" -o name)
```

### Access Console

Access to Openk9 admin console using url [http://demo.openk9.local/admin](http://demo.openk9.local/admin)

![image-20220303214347128](../../static/img/installation/image-20220303214347128.png)

Log in with credentials ("test:test")

![image-20220303214849436](../../static/img/installation/image-20220303214849436.png)


## Search Frontend

Search FrontEnd represents search standalone frontend for Openk9.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario

```bash
helm install search-frontend 01-core-charts/openk9-search-frontend \
  -n openk9 \
  -f 01-core-charts/openk9-search-frontend/scenarios/local-runtime.yaml
```


### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-query-frontend" -o name)
```

### Access UI

Access to Openk9 frontend Ui using url [http://demo.openk9.local/](http://demo.openk9.local/)

![image-20220304180312005](../../static/img/installation/image-20220304180312005.png)
