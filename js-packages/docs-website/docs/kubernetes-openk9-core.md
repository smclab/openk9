---
id: kubernetes-openk9-core
title: Install Openk9 Core
---

## INGESTION

La componente "Ingestion" espone le API Rest attravero le quali i diversi "datasource" configurati forniscono le informazioni da indicizzare.

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install ingestion 01-core-charts/openk9-ingestion \
  -n openk9 \
  -f 01-core-charts/openk9-ingestion/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-ingestion" -o name)
```



Fare un port-forward del pod o del servizio e richiedere "http://localhost:8080/q/openapi" per ottenere lo yaml delle api





## DATASOURCE

La componente "Datasource" rappresenta il gestore delle diverse fonte dati indicizzate.

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install datasource 01-core-charts/openk9-datasource \
  -n openk9 \
  -f 01-core-charts/openk9-datasource/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-datasource" -o name)
```

La componente "Dashboard" si auto-registra all'interno di Consul. Accedendo alla [dashboard](#consul-dashboard) dovremmo vedere il nostro componente tra i servizi.





## ENTITY-MANAGER

La componente "Entity Manager" rappresenta <mark>TODO</mark>

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install entitymanager 01-core-charts/openk9-entity-manager \
  -n openk9 \
  -f 01-core-charts/openk9-entity-manager/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-entity-manager" -o name)
```





## INDEX-WRITER

La componente "Index Writer" rappresenta <mark>TODO</mark>

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install indexwriter 01-core-charts/openk9-index-writer \
  -n openk9 \
  -f 01-core-charts/openk9-index-writer/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-index-writer" -o name)
```





## PLUGIN-DRIVER-MANAGER

La componente "Plugin Driver Manager" rappresenta <mark>TODO</mark>

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install pdm 01-core-charts/openk9-plugin-driver-manager \
  -n openk9 \
  -f 01-core-charts/openk9-plugin-driver-manager/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-plugin-driver-manager" -o name)
```

La componente "Plugin Driver Manager" si auto-registra all'interno di Consul. Accedendo alla [dashboard](#consul-dashboard) dovremmo vedere il nostro componente tra i servizi.



## SEARCHER

La componente "Searcher" rappresenta <mark>TODO</mark>

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install searcher 01-core-charts/openk9-searcher \
  -n openk9 \
  -f 01-core-charts/openk9-searcher/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-searcher" -o name)
```

La componente "Searcher" si auto-registra all'interno di Consul. Accedo alla [dashboard](#consul-dashboard) dovremmo vedere il nostro componente tra i servizi.





## API-AGGREGATOR

La componente "API Aggregator" rappresenta <mark>TODO</mark>

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install api-aggregator 01-core-charts/openk9-api-aggregator \
  -n openk9 \
  -f 01-core-charts/openk9-api-aggregator/scenarios/local-runtime.yaml
```

Nel contesto `local-runtime` la componente espone un Ingress su "http://demo.openk9.local".



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-api-aggregator" -o name)
```

 Accedere a "[http://demo.openk9.local/q/swagger-ui](http://demo.openk9.local/q/swagger-ui)" per accedere alla UI di Swagger con la descrizione delle API esposte





## SEARCH-ADMIN

La componente "Search Admin" rappresenta <mark>TODO</mark>

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

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


## QUERY-FRONTEND

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

