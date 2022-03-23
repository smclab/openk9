---
id: kubernetes-openk9-core
title: Install Openk9 Core
---

## Ingestion

The [Ingestion](ingestion) component exposes the Rest API through which the various "datasource" configured provide the information to be indexed.

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

Port forward the pod or service and request "http://localhost:8080/q/openapi" to get the api's yaml

## Datasource

The [Datasource](datasource) component represents the manager of the various indexed data sources.

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

"Dashboard" component self-registers within Consul.
By accessing the [dashboard](#consul-dashboard) we should see our component among the services.


## Entity Manager

[Entity Manager](entity-manager) component represents <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install entitymanager 01-core-charts/openk9-entity-manager \
  -n openk9 \
  -f 01-core-charts/openk9-entity-manager/scenarios/local-runtime.yaml
```



### Verify installation

Check the pod startup logs for the absence of serious errors.

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-entity-manager" -o name)
```

## Index writer
[Index Writer](index-writer) component represents <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install indexwriter 01-core-charts/openk9-index-writer \
  -n openk9 \
  -f 01-core-charts/openk9-index-writer/scenarios/local-runtime.yaml
```



### Verify installation

Check the pod startup logs for the absence of serious errors.

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-index-writer" -o name)
```

## Plugin Driver Manager

[Plugin Driver Manager](plugin-driver-manager) represents <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install pdm 01-core-charts/openk9-plugin-driver-manager \
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

[Searcher"](searcher) component rappresents <mark>TODO</mark>

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
I access the [dashboard] (# consul-dashboard) we should see our component among the services.

## Api Aggregator

Api Aggregator component represents <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install api-aggregator 01-core-charts/openk9-api-aggregator \
  -n openk9 \
  -f 01-core-charts/openk9-api-aggregator/scenarios/local-runtime.yaml
```

In the `local-runtime` context the component exposes an Ingress on "http: //demo.openk9.local".


### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-api-aggregator" -o name)
```

Go to "[http: //demo.openk9.local/q/swagger-ui](http://demo.openk9.local/q/swagger-ui)"
to access the Swagger UI with the description of the exposed APIs

## Search Admin

Search Admin component represents <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that * adapts * it to the chosen scenario.

```bash
helm install search-admin 01-core-charts/openk9-search-admin \
  -n openk9 \
  -f 01-core-charts/openk9-search-admin/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-search-admin" -o name)
```



### Accesso Console

Accedo alla console di amministrazione di OpenK9 sfruttando la url "[http://demo.openk9.local/admin](http://demo.openk9.local/admin)"

![image-20220303214347128](../static/img/installation/image-20220303214347128.png)

dove accedendo con le credenziali dell'unico utente censito ("test:test")

![image-20220303214849436](../static/img/installation/image-20220303214849436.png)

attero nella console per il momento povera di contenuti.


## Searc Frontend

La componente "Query FrontEnd" rappresenta <mark>TODO</mark>

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install query-frontend 01-core-charts/openk9-query-frontend \
  -n openk9 \
  -f 01-core-charts/openk9-query-frontend/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-query-frontend" -o name)
```

