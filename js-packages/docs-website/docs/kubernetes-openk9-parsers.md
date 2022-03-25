---
id: kubernetes-openk9-parsers
title: Install Openk9 Parsers
---

In this section <mark>TODO</mark>

## Web Parser

Web Parser represents <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component, and the
configuration that *adapts* it to the chosen scenario.

```bash
helm install web-parser 01-core-charts/openk9-web-parser \
  -n openk9 \
  -f 01-core-charts/openk9-web-parser/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-web-parser" -o name)
```

Activate a port-forward to be able to access the administration interface

```
kubectl -n openk9 port-forward \
 $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-email-parser" -o name) \
 6800:6800
```


## Email Parser

Email Parser represents <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component, and the
configuration that *adapts* it to the chosen scenario.

```bash
helm install web-parser 01-core-charts/openk9-email-parser \
  -n openk9 \
  -f 01-core-charts/openk9-email-parser/scenarios/local-runtime.yaml
```



### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-email-parser" -o name)
```

Activate a port-forward to be able to access the administration interface

```
kubectl -n openk9 port-forward \
 $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-email-parser" -o name) \
 6800:6800
```

