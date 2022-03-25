---
id: kubernetes-openk9-enrichers
title: Install Openk9 Enrichers
---

In this section <mark>TODO</mark>

## Named Entity Recognition

Named Entity Recognition represents <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that * adapts * it to the chosen scenario.

```bash
helm install ner-rabbit 01-core-charts/openk9-ner-it \
  -n openk9 \
  -f 01-core-charts/openk9-ner-it/scenarios/local-runtime.yaml
```



### Verify installation



```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-ner-it" -o name)
```

## Tika

La componente "Named Entity Recognition" (brevemente NER) rappresenta <mark>TODO</mark>

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install ner-rabbit 01-core-charts/openk9-ner-it \
  -n openk9 \
  -f 01-core-charts/openk9-ner-it/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-ner-it" -o name)
```

## Tika with ocr

La componente "Named Entity Recognition" (brevemente NER) rappresenta <mark>TODO</mark>

Installo usando il chart locale, che è già predisposto per usare l'ultima versione stabile del componente, ed il file di configurazione che la *adegua* allo scenario scelto

```bash
helm install ner-rabbit 01-core-charts/openk9-ner-it \
  -n openk9 \
  -f 01-core-charts/openk9-ner-it/scenarios/local-runtime.yaml
```



### Verifica installazione

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-ner-it" -o name)
```

