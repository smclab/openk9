---
id: kubernetes-openk9-core
title: Install Openk9 Core
---

In this section is described how to install Openk9 core components. To install components helm charts are used.


### Preparing the installation

OpenK9 is formed by multiples core components to install.
Currently installing through [Helm Charts](https://helm.sh/docs/topics/charts/) is the best choice.

Inside the [openk9-kubernetes repository](https://github.com/smclab/openk9-kubernetes) there is the
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


## Entity Manager

See architecture [Entity Manager](entity-manager) documentation to go into detail.

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

## Index writer

See architecture [Index writer](index-writer) documentation to go into detail.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install index-writer 01-core-charts/openk9-index-writer \
  -n openk9 \
  -f 01-core-charts/openk9-index-writer/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors.

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-index-writer" -o name)
```

## Plugin Driver Manager

See architecture [Plugin Driver Manager](plugin-driver-manager) documentation to go into detail.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install plugin-driver-manager 01-core-charts/openk9-plugin-driver-manager \
  -n openk9 \
  -f 01-core-charts/openk9-plugin-driver-manager/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-plugin-driver-manager" -o name)
```

Plugin Driver Manager component self-registers within Consul.
By accessing the [dashboard](#consul-dashboard) we should see our component among the services.


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

Searcher component self-registers within Consul.
Access the [dashboard](http://consul.demo.openk9.local) we should see our component among the services.

## Api Aggregator

See architecture [Api Aggregator](api-aggregator) documentation to go into detail.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install api-aggregator 01-core-charts/openk9-api-aggregator \
  -n openk9 \
  -f 01-core-charts/openk9-api-aggregator/scenarios/local-runtime.yaml
```

In the `local-runtime` context the component exposes an Ingress on "http://demo.openk9.local".


### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-api-aggregator" -o name)
```

Go to [http: //demo.openk9.local/q/swagger-ui](http://demo.openk9.local/q/swagger-ui)
to access the Swagger UI with the description of the exposed APIs

