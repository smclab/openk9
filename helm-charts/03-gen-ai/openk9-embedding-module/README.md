# Helm package for Openk9 Embedding Module

## Introduction

This chart bootstraps Openk9 Embedding Module deployment on a [Kubernetes](https://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

## Prerequisites

- Kubernetes 1.23+
- Helm 3.8.0+
- PV provisioner support in the underlying infrastructure

## Installing the Chart

Add in your local environment Openk9 Helm repo:

```bash
helm repo add openk9 https://registry.smc.it/repository/helm-private/
```

Then install using following command:

```bash
helm upgrade -i embedding-module openk9/openk9-embedding-module
```

The command deploys Openk9 Embedding Module on the Kubernetes cluster in the default configuration. The [Parameters](#parameters) section lists the parameters that can be configured during installation.

## Parameters

### Configure Image

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `image.registry`    | Openk9 Embedding Module image registry                                                                                  | `REGISTRY_NAME`            |
| `image.repository`  | Openk9 Embedding Module image repository                                                                                | `REPOSITORY_NAME/openk9` |
| `image.digest`      | Openk9 Embedding Module image digest in the way sha256:aa.... Please note this parameter, if set, will override the tag | `""`                       |
| `image.pullPolicy`  | Openk9 Embedding Module image pull policy                                                                               | `IfNotPresent`             |
| `image.pullSecrets` | Specify docker-registry secret names as an array                                                         | `[]`                       |
| `image.debug`       | Set to true if you would like to see extra information on logs                                           | `false`                    |

### Common parameters

| Name                                         | Description                                                                                                                                                             | Value                                             |
| -------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| `nameOverride`                               | String to partially override ingestion.fullname template (will maintain the release name)                                                                                | `""`                                              |
| `fullnameOverride`                           | String to fully override ingestion.fullname template                                                                                                                     | `""`                                              |
| `commonAnnotations`                          | Annotations to add to all deployed objects                                                                                                                              | `{}`                                              |
| `hostAliases`                                | Deployment pod host aliases                                                                                                                                             | `[]`                                              |


### Configure connections to other Openk9 services

Openk9 Datasource needs to communicate with other components to work. 
In particular Openk9 Embedding Module need to communicate with Datasource to perform tenant resolving.

To configure connection to Datasource following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `openk9.datasource.host`    | Datasource host                         | `openk9-datasource`            |


### Service account and rbac

Openk9 Embedding Module doesn't need particular Service Account or rbac. 

But if you want for some reasons associate a pre-created Service Account to Openk9 Embedding Module you can do using following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `serviceAccount.create`    | If create a default Service Account     | `false`            |
| `serviceAccount.annotations`  | Service Account annotations                                    | `{}` |
| `serviceAccount.name`  | Name of pre created Service Account                                    | `` |


### RBAC parameters

| Name                                          | Description                                                                                | Value   |
| --------------------------------------------- | ------------------------------------------------------------------------------------------ | ------- |
| `serviceAccount.create`                       | Enable creation of ServiceAccount for Ingestion pods                                        | `true`  |
| `serviceAccount.name`                         | Name of the created serviceAccount                                                         | `""`    |
| `serviceAccount.automountServiceAccountToken` | Auto-mount the service account token in the pod                                            | `false` |
| `serviceAccount.annotations`                  | Annotations for service account. Evaluated as a template. Only used if `create` is `true`. | `{}`    |
| `rbac.create`                                 | Whether RBAC rules should be created                                                       | `true`  |
| `rbac.rules`                                  | Custom RBAC rules                                                                          | `[]`    |                              |

### Configure ingress or route

Openk9 Embedding Module doesn't need to openk9 Apis externally.

For Ingress creation presente of an Ingress Controller is mandatory on your cluster. Check [Kubernets Documentation](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/) for availables Ingress Controllers.

But if you want for some reasons you can modifying following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `ingress.enabled`    | If enable Ingress creation     | `false`            |
| `ingress.host`  | Ingress host                                    | `{}` |
| `ingress.annotations`  | Ingress annotations                                    | `{}` |
| `ingress.tls.enabled`  | If enable tls on Ingress                                    | `` |
| `ingress.tls.secretName`  | Secret with tls certificate to associate to Ingress                                    | `` |

To configure Route for Openshift use:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `route.enabled`    | If enable Route creation     | `false`            |
| `route.host`  | Route host                                    | `{}` |
| `route.annotations`  | Route annotations                                    | `{}` |
| `route.tls.enabled`  | If enable tls on Route                                     | `` |
| `route.tls.secretName`  | Secret with tls certificate to associate to Route                                       | `` |

### Startup, readiness and liveness probes

Openk9 Embedding Module supports Readiness, Liveness and Startup Probes.

It uses [Quarkus health check endpoint](https://quarkus.io/guides/smallrye-health#running-the-health-check) to perform healht check actions. 

To configure probes appropriately you can use following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `livenessProbe.enabled`                             | Enable livenessProbe                                                                                                                                                                                              | `true`           |
| `livenessProbe.initialDelaySeconds`                 | Initial delay seconds for livenessProbe                                                                                                                                                                           | `120`            |
| `livenessProbe.periodSeconds`                       | Period seconds for livenessProbe                                                                                                                                                                                  | `30`             |
| `livenessProbe.timeoutSeconds`                      | Timeout seconds for livenessProbe                                                                                                                                                                                 | `20`             |
| `livenessProbe.failureThreshold`                    | Failure threshold for livenessProbe                                                                                                                                                                               | `6`              |
| `livenessProbe.successThreshold`                    | Success threshold for livenessProbe                                                                                                                                                                               | `1`              |
| `readinessProbe.enabled`                            | Enable readinessProbe                                                                                                                                                                                             | `true`           |
| `readinessProbe.initialDelaySeconds`                | Initial delay seconds for readinessProbe                                                                                                                                                                          | `10`             |
| `readinessProbe.periodSeconds`                      | Period seconds for readinessProbe                                                                                                                                                                                 | `30`             |
| `readinessProbe.timeoutSeconds`                     | Timeout seconds for readinessProbe                                                                                                                                                                                | `20`             |
| `readinessProbe.failureThreshold`                   | Failure threshold for readinessProbe                                                                                                                                                                              | `3`              |
| `readinessProbe.successThreshold`                   | Success threshold for readinessProbe                                                                                                                                                                              | `1`              |
| `startupProbe.enabled`                              | Enable startupProbe                                                                                                                                                                                               | `false`          |
| `startupProbe.initialDelaySeconds`                  | Initial delay seconds for startupProbe                                                                                                                                                                            | `10`             |
| `startupProbe.periodSeconds`                        | Period seconds for startupProbe                                                                                                                                                                                   | `30`             |
| `startupProbe.timeoutSeconds`                       | Timeout seconds for startupProbe                                                                                                                                                                                  | `20`             |
| `startupProbe.failureThreshold`                     | Failure threshold for startupProbe                                                                                                                                                                                | `3`              |
| `startupProbe.successThreshold`                     | Success threshold for startupProbe                                                                                                                                                                                | `1`              |

### Resource requests and limits

Openk9 Embedding Module chart allow setting resource requests and limits for all containers inside the chart deployment. These are inside the `resources` value (check parameter table). Setting requests is essential for production workloads and these should be adapted to your specific use case.

| `resources`                                         | Set container requests and limits for different resources like CPU or memory (essential for production workloads)                                                                                                 | `{}`             |

### Security

You can use security context settings when you need to handle permissions on your cluster.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `podSecurityContext.enabled`                        | Enable Admin Ui pods' Security Context                                                                                                                                                                            | `true`           |
| `podSecurityContext.fsGroupChangePolicy`            | Set filesystem group change policy                                                                                                                                                                                | `Always`         |
| `podSecurityContext.sysctls`                        | Set kernel settings using the sysctl interface                                                                                                                                                                    | `[]`             |
| `podSecurityContext.supplementalGroups`             | Set filesystem extra groups                                                                                                                                                                                       | `[]`             |
| `podSecurityContext.fsGroup`                        | Set Admin Ui pod's Security Context fsGroup                                                                                                                                                                       | `1001`           |
| `podSecurityContext.enabled`                  | Enabled Admin Ui containers' Security Context                                                                                                                                                                     | `true`           |
| `podSecurityContext.runAsUser`                | Set Admin Ui containers' Security Context runAsUser                                                                                                                                                               | `1001`           |
| `podSecurityContext.runAsGroup`               | Set Admin Ui containers' Security Context runAsGroup                                                                                                                                                              | `1001`           |
| `podSecurityContext.runAsNonRoot`             | Set Admin Ui container's Security Context runAsNonRoot                                                                                                                                                            | `true`           |
| `podSecurityContext.allowPrivilegeEscalation` | Set container's privilege escalation                                                                                                                                                                              | `false`          |
| `podSecurityContext.readOnlyRootFilesystem`   | Set container's Security Context readOnlyRootFilesystem                                                                                                                                                           | `true`           |
| `podSecurityContext.capabilities.drop`        | Set container's Security Context runAsNonRoot                                                                                                                                                                     | `["ALL"]`        |

### Prometheus metrics

This chart can be integrated with Prometheus by setting `metrics.enabled` to `true`.

#### Prometheus requirements

It is necessary to have a working installation of Prometheus for the integration to work.

| Name                                                 | Description                                                                                        | Value                 |
| ---------------------------------------------------- | -------------------------------------------------------------------------------------------------- | --------------------- |
| `metrics.enabled`                                    | Enable exposing Datasource metrics to be gathered by Prometheus                                      | `false`               |
| `metrics.serviceMonitor.namespace`                   | Specify the namespace in which the serviceMonitor resource will be created                         | `""`                  |
| `metrics.serviceMonitor.jobLabel`                    | The name of the label on the target service to use as the job name in prometheus.                  | `""`                  |
| `metrics.serviceMonitor.targetLabels`                | Used to keep given service's labels in target                                                      | `{}`                  |
| `metrics.serviceMonitor.podTargetLabels`             | Used to keep given pod's labels in target                                                          | `{}`                  |
| `metrics.serviceMonitor.selector`                    | ServiceMonitor selector labels                                                                     | `{}`                  |
| `metrics.serviceMonitor.labels`                      | Extra labels for the ServiceMonitor                                                                | `{}`                  |
| `metrics.serviceMonitor.annotations`                 | Extra annotations for the ServiceMonitor                                                           | `{}`                  |
| `metrics.serviceMonitor.enabled`                     | Deprecated. Please use `metrics.serviceMonitor.{default/perObject/detailed}` instead.              | `false`               |
| `metrics.serviceMonitor.interval`                    | Deprecated. Please use `metrics.serviceMonitor.{default/perObject/detailed}` instead.              | `30s`                 |
| `metrics.serviceMonitor.scrapeTimeout`               | Deprecated. Please use `metrics.serviceMonitor.{default/perObject/detailed}` instead.              | `""`                  |
| `metrics.serviceMonitor.relabelings`                 | Deprecated. Please use `metrics.serviceMonitor.{default/perObject/detailed}` instead.              | `[]`                  |
| `metrics.serviceMonitor.metricRelabelings`           | Deprecated. Please use `metrics.serviceMonitor.{default/perObject/detailed}` instead.              | `[]`                  |
| `metrics.serviceMonitor.honorLabels`                 | Deprecated. Please use `metrics.serviceMonitor.{default/perObject/detailed}` instead.              | `false`               |
| `metrics.serviceMonitor.path`                        | Deprecated. Please use `metrics.serviceMonitor.{default/perObject/detailed}` instead.              | `""`                  |
| `metrics.serviceMonitor.params`                      | Deprecated. Please use `metrics.serviceMonitor.{default/perObject/detailed}` instead.              | `{}`                  |
| `metrics.prometheusRule.enabled`                     | Set this to true to create prometheusRules for Prometheus operator                                 | `false`               |
| `metrics.prometheusRule.additionalLabels`            | Additional labels that can be used so prometheusRules will be discovered by Prometheus             | `{}`                  |
| `metrics.prometheusRule.namespace`                   | namespace where prometheusRules resource should be created                                         | `""`                  |
| `metrics.prometheusRule.rules`                       | List of rules, used as template by Helm.                                                           | `[]`                  |

### Scale horizontally

To horizontally scale this chart once it has been deployed, two options are available:

- Use the `kubectl scale` command.
- Upgrade the chart modifying the `replicaCount` parameter.

```text
    replicaCount=3
```

When scaling down the solution, unnecessary Ingestion nodes are automatically stopped. 

Is possible also to set autoscaling using following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `autoscaling.enabled`                             | Enable livenessProbe                                                                                                                                                                                              | `true`           |
| `autoscaling.minReplicas`                 | Initial delay seconds for livenessProbe                                                                                                                                                                           | `120`            |
| `autoscaling.maxReplicas`                       | Period seconds for livenessProbe                                                                                                                                                                                  | `30`             |
| `autoscaling.averageCpuUtilizationPercentage`                      | Timeout seconds for livenessProbe                                                                                                                                                                                 | `20`             |
| `autoscaling.averageMemoryUtilizationPercentage`                    | Failure threshold for livenessProbe                                                                                                                                                                               | `6`              |

## Advanced logging

No settings are available to set up advanced configuration for logging.

## Known issues

No Known issues

## Troubleshooting

Find more information about how to deal with common errors related to Openk9's Helm charts in [this troubleshooting guide](https://staging-site.openk9.io/docs/monitoring/troubleshooting).


## Upgrading

### To 3.1.0

No action necessary

### Previous versions

No details present


## License


Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
