---
id: kubernetes-openk9-enrichers
title: Install Openk9 Enrichers
---

In this section is described how to install Openk9 enricher components. To install components helm charts are used.

### Preparing the installation

OpenK9 uses enrichers to enrich data. You can use some core enrichers developed for Openk9.
Currently installing through [Helm Charts](https://helm.sh/docs/topics/charts/) is the best choice.

Inside the [openk9-kubernetes repository](https://github.com/smclab/openk9-kubernetes) there is the
`kubernetes/03-enrichers` folder where, for each enricher, there are configuration files for the different installation scenarios.

So clone this repository before start to install.

## Named Entity Recognition

Named Entity Recognition enricher <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install openk9-ner 03-enrichers/openk9-ner \
  -n openk9 \
  -f 03-enrichers/openk9-ner/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-ner-rabbit" -o name)
```

## Tika

Tika enricher <mark>TODO</mark>

### Tika without Ocr

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install openk9-tika 03-enrichers/openk9-tika \
  -n openk9 \
  -f 03-enrichers/openk9-tika/scenarios/local-runtime.yaml
```

### Tika with ocr

Tika with Ocr perform <mark>TODO</mark>

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install openk9-tika-ocr 03-enrichers/openk9-tika-ocr \
  -n openk9 \
  -f 03-enrichers/openk9-tika-ocr/scenarios/local-runtime.yaml
```

### Verify installation

Check the pod startup logs for the absence of serious errors

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-tika" -o name)
```

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-tika-ocr" -o name)
```

