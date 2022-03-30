---
id: kubernetes-openk9-enrichers
title: Install Openk9 Enrichers
---

In this section is described how to install Openk9 enricher components. To install components helm charts are used.

## Named Entity Recognition

Named Entity Recognition enricher <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that * adapts * it to the chosen scenario.

```bash
helm install ner-rabbit 03-enrichers/openk9-ner-it \
  -n openk9 \
  -f 03-enrichers/openk9-ner-it/scenarios/local-runtime.yaml
```



### Verify installation



```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-ner-it" -o name)
```

## Tika

Tika enricher <mark>TODO</mark>

### Tika without Ocr

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that * adapts * it to the chosen scenario.

```bash
helm install openk9-tika 03-enrichers/openk9-tika \
  -n openk9 \
  -f 03-enrichers/openk9-tika/scenarios/local-runtime.yaml
```

### Tika with ocr

Tika with Ocr perform <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that * adapts * it to the chosen scenario.

```bash
helm install openk9-tika-ocr 03-enrichers/openk9-tika-ocr \
  -n openk9 \
  -f 03-enrichers/openk9-tika-ocr/scenarios/local-runtime.yaml
```

### Verify installation

Verifico nei log di avvio del pod l'assenza di errori gravi

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-tika" -o name)
```

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-tika-ocr" -o name)
```

