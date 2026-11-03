# Helm package for Openk9 Email Connector

## Introduction

This chart bootstraps Openk9 Email Connector deployment on a [Kubernetes](https://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

## Installing the Chart

Add in your local environment Openk9 Helm repo:

```bash
helm repo add openk9 https://registry.smc.it/repository/helm-private/
```

Then install using following command:

```bash
helm upgrade -i email-connector openk9/openk9-email-connector
```

## Parameters

### Startup, readiness and liveness probes

Openk9 Email Connector supports Readiness, Liveness and Startup Probes.

Each probe is an HTTP GET on `/docs`, the API documentation page that FastAPI serves as soon as the application is up. The whole probe, check included, is read from `values.yaml`, so it can be changed without touching the template. If the connector is ever started with the FastAPI docs disabled (`docs_url=None`), point the probes to another path.

To configure probes appropriately you can use following parameters:

| Name                                 | Description                                       | Value   |
| ------------------------------------ | ------------------------------------------------- | ------- |
| `livenessProbe.enabled`              | Enable livenessProbe                              | `true`  |
| `livenessProbe.httpGet.path`         | Path requested by livenessProbe                   | `/docs` |
| `livenessProbe.httpGet.port`         | Port (name or number) requested by livenessProbe  | `http`  |
| `livenessProbe.initialDelaySeconds`  | Initial delay seconds for livenessProbe           | `0`     |
| `livenessProbe.periodSeconds`        | Period seconds for livenessProbe                  | `10`    |
| `livenessProbe.timeoutSeconds`       | Timeout seconds for livenessProbe                 | `5`     |
| `livenessProbe.successThreshold`     | Success threshold for livenessProbe               | `1`     |
| `livenessProbe.failureThreshold`     | Failure threshold for livenessProbe               | `3`     |
| `readinessProbe.enabled`             | Enable readinessProbe                             | `true`  |
| `readinessProbe.httpGet.path`        | Path requested by readinessProbe                  | `/docs` |
| `readinessProbe.httpGet.port`        | Port (name or number) requested by readinessProbe | `http`  |
| `readinessProbe.initialDelaySeconds` | Initial delay seconds for readinessProbe          | `0`     |
| `readinessProbe.periodSeconds`       | Period seconds for readinessProbe                 | `10`    |
| `readinessProbe.timeoutSeconds`      | Timeout seconds for readinessProbe                | `5`     |
| `readinessProbe.successThreshold`    | Success threshold for readinessProbe              | `1`     |
| `readinessProbe.failureThreshold`    | Failure threshold for readinessProbe              | `3`     |
| `startupProbe.enabled`               | Enable startupProbe                               | `true`  |
| `startupProbe.httpGet.path`          | Path requested by startupProbe                    | `/docs` |
| `startupProbe.httpGet.port`          | Port (name or number) requested by startupProbe   | `http`  |
| `startupProbe.initialDelaySeconds`   | Initial delay seconds for startupProbe            | `0`     |
| `startupProbe.periodSeconds`         | Period seconds for startupProbe                   | `5`     |
| `startupProbe.timeoutSeconds`        | Timeout seconds for startupProbe                  | `3`     |
| `startupProbe.successThreshold`      | Success threshold for startupProbe                | `1`     |
| `startupProbe.failureThreshold`      | Failure threshold for startupProbe                | `30`    |

To use another kind of check, set `httpGet` to `null` and add the new handler. Helm merges your values with the chart defaults, and Kubernetes rejects a probe with two handlers:

```yaml
livenessProbe:
  httpGet: null
  tcpSocket:
    port: http
```
