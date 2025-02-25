# Helm package for Openk9 File Manager

## Introduction

This chart bootstraps Openk9 File Manager deployment on a [Kubernetes](https://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

## Prerequisites

- Kubernetes 1.23+
- Helm 3.8.0+

## Installing the Chart

Add in your local environment Openk9 Helm repo:

```bash
helm repo add openk9 https://registry.smc.it/repository/helm-private/
```

Then install using following command:

```bash
helm upgrade -i file-manager openk9/openk9-file-manager
```

The command deploys Openk9 File Manager on the Kubernetes cluster in the default configuration. The [Parameters](#parameters) section lists the parameters that can be configured during installation.

# Parameters

### Configure Image

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `image.registry`    | Openk9 File Manager image registry                                                                                  | `REGISTRY_NAME`            |
| `image.repository`  | Openk9 File Manager image repository                                                                                | `REPOSITORY_NAME/openk9` |
| `image.digest`      | Openk9 File Manager image digest in the way sha256:aa.... Please note this parameter, if set, will override the tag | `""`                       |
| `image.pullPolicy`  | Openk9 v image pull policy                                                                               | `IfNotPresent`             |
| `image.pullSecrets` | Specify docker-registry secret names as an array                                                         | `[]`                       |
| `image.debug`       | Set to true if you would like to see extra information on logs                                           | `false`                    |

### Common parameters

| Name                                         | Description                                                                                                                                                             | Value                                             |
| -------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| `nameOverride`                               | String to partially override file-manager.fullname template (will maintain the release name)                                                                                | `""`                                              |
| `fullnameOverride`                           | String to fully override rabbifile-managertmq.fullname template                                                                                                                     | `""`                                              |
| `commonAnnotations`                          | Annotations to add to all deployed objects                                                                                                                              | `{}`                                              |
| `hostAliases`                                | Deployment pod host aliases                                                                                                                                             | `[]`                                              |

### Quarkus configurations

Openk9 File Manager service is based on Quarkus Framework. Use following parameters to set main quarkus configurations.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `quarkus.HttpCors`    | If Cors is enabled                                                                              | `true`            |
| `quarkus.HttpCorsOrigin`  | Cors origin allowed                                   | `/https://.*.openk9.local/,` |
| `quarkus.LogConsoleJson`      | If json log is enabled | `false`                       |
| `quarkus.LogLevel`  | Default log level                                                                             | `INFO`             |

### JVM configuration

Openk9 File Manager is a JVM service. To control JVM based tool options use following parameters:

Openk9 File Manager service is based on Quarkus Framework. Use following parameters to set main quarkus configurations.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `jvm.toolOptions`    | JVM Tool Options                                                                       | ``            |

### Configure Minio

Openk9 File Manager needs Minio to work. 

To configure connection to Minio following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `minio.host`    | Minio url          | `http://minio:9000`            |
| `minio.passwordSecretName` | Name of the secret where password is stored                            | `minio`                       |
| `minio.keyPasswordSecret`       | Name of the key inside the secret where password is stored                                           | `root-password`                    |
| `minio.keyPasswordEnvName`       | Name of environment variable where password is set       | `QUARKUS_MINIO_SECRET_KEY`   |
| `minio.keyUserSecret`       | Name of the key inside the secret where password is stored      | `root-user`   |
| `minio.keyUserEnvName`       | Name of environment variable where password is set       | `QUARKUS_MINIO_ACCESS_KEY`   |


### Configure connections to other Openk9 services

Openk9 File Manager needs to communicate with other components to work. 
In particular Openk9 File Manager need to communicate with Datasource to perform tenant resolving.

To configure connection to Datasource following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `openk9.datasource.host`    | Datasource host                         | `openk9-datasource`            |


### Configure Opentelemetry

Openk9 File Manager supports collecting metrics about traffic using OpenTelemetry.

To enable this feature you need an active instance of OpenTelemetry Collector. Here [official documentation](https://opentelemetry.io/docs/collector/installation/) to install.

Once install you need to configure appropriately some parameters on this Helm Chart.

To activate and set pointing to Opentelemetry collector use following parameters.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `otel.disabled`    | If OpenTelemetry is disabled     | `true`            |
| `otel.endpoint`  | OpenTelemetry Collector endpoint                                    | `` |

### Service account and rbac

Openk9 File Manager doesn't need particular Service Account or rbac. 

But if you want for some reasons associate a pre-created Service Account to Openk9 File Manager you can do using following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `serviceAccount.create`    | If create a default Service Account     | `false`            |
| `serviceAccount.annotations`  | Service Account annotations                                    | `{}` |
| `serviceAccount.name`  | Name of pre created Service Account                                    | `` |


### RBAC parameters

| Name                                          | Description                                                                                | Value   |
| --------------------------------------------- | ------------------------------------------------------------------------------------------ | ------- |
| `serviceAccount.create`                       | Enable creation of ServiceAccount for RabbitMQ pods                                        | `true`  |
| `serviceAccount.name`                         | Name of the created serviceAccount                                                         | `""`    |
| `serviceAccount.automountServiceAccountToken` | Auto-mount the service account token in the pod                                            | `false` |
| `serviceAccount.annotations`                  | Annotations for service account. Evaluated as a template. Only used if `create` is `true`. | `{}`    |
| `rbac.create`                                 | Whether RBAC rules should be created                                                       | `true`  |
| `rbac.rules`                                  | Custom RBAC rules                                                                          | `[]`    |                              |

### Configure ingress or route

Openk9 File Manager doesn't need to openk9 Apis externally.

For Ingress creation presente of an Ingress Controller is mandatory on your cluster. Check [Kubernets Documentation](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/) for availables Ingress Controllers.

But if you want for some reasons you can modifying following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `ingress.enabled`    | If enable Ingress creation     | `false`            |
| `ingress.host`  | Ingress host                                    | `{}` |
| `ingress.ingressClassName`  | Ingress Class Name (If not present overwrite default Ingress Class Name)         | `{}` |
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

Openk9 File Manager supports Readiness, Liveness and Startup Probes.

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

Openk9 File Manager chart allow setting resource requests and limits for all containers inside the chart deployment. These are inside the `resources` value (check parameter table). Setting requests is essential for production workloads and these should be adapted to your specific use case.

| `resources`                                         | Set container requests and limits for different resources like CPU or memory (essential for production workloads)                                                                                                 | `{}`             |

### Security

You can use security context settings when you need to handle permissions on your cluster.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `podSecurityContext.enabled`                        | Enable File Manager pods' Security Context                                                                                                                                                                            | `true`           |
| `podSecurityContext.fsGroupChangePolicy`            | Set filesystem group change policy                                                                                                                                                                                | `Always`         |
| `podSecurityContext.sysctls`                        | Set kernel settings using the sysctl interface                                                                                                                                                                    | `[]`             |
| `podSecurityContext.supplementalGroups`             | Set filesystem extra groups                                                                                                                                                                                       | `[]`             |
| `podSecurityContext.fsGroup`                        | Set File Manager pod's Security Context fsGroup                                                                                                                                                                       | `1001`           |
| `podSecurityContext.enabled`                  | Enabled File Manager containers' Security Context                                                                                                                                                                     | `true`           |
| `podSecurityContext.runAsUser`                | Set File Manager containers' Security Context runAsUser                                                                                                                                                               | `1001`           |
| `podSecurityContext.runAsGroup`               | Set File Manager containers' Security Context runAsGroup                                                                                                                                                              | `1001`           |
| `podSecurityContext.runAsNonRoot`             | Set File Manager container's Security Context runAsNonRoot                                                                                                                                                            | `true`           |
| `podSecurityContext.allowPrivilegeEscalation` | Set container's privilege escalation                                                                                                                                                                              | `false`          |
| `podSecurityContext.readOnlyRootFilesystem`   | Set container's Security Context readOnlyRootFilesystem                                                                                                                                                           | `true`           |
| `podSecurityContext.capabilities.drop`        | Set container's Security Context runAsNonRoot                                                                                                                                                                     | `["ALL"]`        |

### Prometheus metrics

This chart can be integrated with Prometheus by setting `metrics.enabled` to `true`.

#### Prometheus requirements

It is necessary to have a working installation of Prometheus for the integration to work.

| Name                                                 | Description                                                                                        | Value                 |
| ---------------------------------------------------- | -------------------------------------------------------------------------------------------------- | --------------------- |
| `metrics.enabled`                                    | Enable exposing File Manager metrics to be gathered by Prometheus                                      | `false`               |
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
| `metrics.prometheusRule.rules`                       | List of rules, used as template by Helm.                                                           | `[]`                  ||

### Scale horizontally

To horizontally scale this chart once it has been deployed, two options are available:

- Use the `kubectl scale` command.
- Upgrade the chart modifying the `replicaCount` parameter.

```text
    replicaCount=3
```

When scaling down the solution, unnecessary File Manager nodes are automatically stopped. 

Is possible also to set autoscaling using following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `autoscaling.enabled`                             | Enable livenessProbe                                                                                                                                                                                              | `true`           |
| `autoscaling.minReplicas`                 | Initial delay seconds for livenessProbe                                                                                                                                                                           | `120`            |
| `autoscaling.maxReplicas`                       | Period seconds for livenessProbe                                                                                                                                                                                  | `30`             |
| `autoscaling.averageCpuUtilizationPercentage`                      | Timeout seconds for livenessProbe                                                                                                                                                                                 | `20`             |
| `autoscaling.averageMemoryUtilizationPercentage`                    | Failure threshold for livenessProbe                                                                                                                                                                               | `6`              |

### Advanced logging

In case you want to configure Openk9 File Manager logging you can set up following parameters:

An example:

```yaml
quarkus:
  LogConsoleJson: false ## put to true to force json log format
  LogLevel: "INFO" ## change log level

## Http Access Log configuration
config:
  httpAccessLog:
    enabled: true
    pattern: "%h %l %u %t \"%r\" %s %b %D"
```

### Known issues

No Known issues

## Troubleshooting

Find more information about how to deal with common errors related to Openk9's Helm charts in [this troubleshooting guide](https://staging-site.openk9.io/docs/monitoring/troubleshooting).


## Upgrading

### To 2.0.0


### To 1.7.0


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