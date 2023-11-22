---
id: kubernetes-openk9-core
title: Openk9 Core
---

In this section is described how to install Openk9 core components. To install components helm charts are used.


### Preparing the installation

OpenK9 is formed by multiples core components to install.
Currently installing through [Helm Charts](https://helm.sh/docs/topics/charts/) is the best choice.

Inside the [openk9-helm-charts repository](https://github.com/smclab/openk9-helm-charts) there is the
`kubernetes/ 01-core-charts` folder where, for each component, there are configuration files for the different installation scenarios.

So clone this repository before start to install.

## Ingestion

See architecture [Ingestion](ingestion) documentation to go into detail.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install ingestion 01-core-charts/openk9-ingestion \
  -n openk9 \
  -f 01-core-charts/openk9-ingestion/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-ingestion" -o name)
```

Port forward the pod or service and request [http://localhost:8080/q/openapi](http://localhost:8080/q/openapi) to get the api's yaml.

## Datasource

See architecture [Datasource](datasource) documentation to go into detail.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install datasource 01-core-charts/openk9-datasource \
  -n openk9 \
  -f 01-core-charts/openk9-datasource/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors.

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-datasource" -o name)
```

Datasource component self-registers within Consul.
By accessing the [dashboard](#consul-dashboard) we should see our component among the services.

## Tenant Manager

See architecture [Tenant Manager](tenant-manager) documentation to go into detail.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install entity-manager 01-core-charts/openk9-entity-manager \
  -n openk9 \
  -f 01-core-charts/openk9-entity-manager/scenarios/local-runtime.yaml
```


### Verify installation

Check the pod startup logs for the absence of serious errors.

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-entity-manager" -o name)
```

## Searcher

See architecture [Searcher](seacher) documentation to go into detail.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install searcher 01-core-charts/openk9-searcher \
  -n openk9 \
  -f 01-core-charts/openk9-searcher/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-searcher" -o name)
```
