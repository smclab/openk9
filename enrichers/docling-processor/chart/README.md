# Helm package for Openk9 Docling Processor

## Introduction

This chart bootstraps Openk9 Docling Processor deployment on a [Kubernetes](https://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

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
helm upgrade -i docling-processor openk9/openk9-docling-processor
```

The command deploys Openk9 Docling Processor on the Kubernetes cluster in the default configuration. The [Parameters](#parameters) section lists the parameters that can be configured during installation.

# Parameters

### Configure Image

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `image.registry`    | Openk9 Docling Processor image registry                                                                                  | `REGISTRY_NAME`            |
| `image.repository`  | Openk9 Docling Processor image repository                                                                                | `REPOSITORY_NAME/openk9` |
| `image.digest`      | Openk9 Docling Processor image digest in the way sha256:aa.... Please note this parameter, if set, will override the tag | `""`                       |
| `image.pullPolicy`  | Openk9 Docling Processor image pull policy                                                                               | `IfNotPresent`             |
| `image.pullSecrets` | Specify docker-registry secret names as an array                                                         | `[]`                       |
| `image.debug`       | Set to true if you would like to see extra information on logs                                           | `false`                    |


### Configure connections to other Openk9 services

Openk9 Docling Processor needs to communicate with other components to work. 
In particular Openk9 Docling Processor need to communicate with File Manager to perform binaries download.

To configure connection to File Manager following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `openk9.fileManager.host`    | File Manager host                         | `openk9-file-manager`            |

To configure connection to Datasource following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `openk9.datasource.host`    | Datasource host                         | `openk9-datasource`            |


### Service account and rbac

Openk9 Docling Processor doesn't need particular Service Account or rbac. 

But if you want for some reasons associate a pre-created Service Account to Openk9 Docling Processor you can do using following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `serviceAccount.create`    | If create a default Service Account     | `false`            |
| `serviceAccount.annotations`  | Service Account annotations                                    | `{}` |
| `serviceAccount.name`  | Name of pre created Service Account                                    | `` |


### RBAC parameters

| Name                                          | Description                                                                                | Value   |
| --------------------------------------------- | ------------------------------------------------------------------------------------------ | ------- |
| `serviceAccount.create`                       | Enable creation of ServiceAccount for Docling Processor pods                                        | `true`  |
| `serviceAccount.name`                         | Name of the created serviceAccount                                                         | `""`    |
| `serviceAccount.automountServiceAccountToken` | Auto-mount the service account token in the pod                                            | `false` |
| `serviceAccount.annotations`                  | Annotations for service account. Evaluated as a template. Only used if `create` is `true`. | `{}`    |
| `rbac.create`                                 | Whether RBAC rules should be created                                                       | `true`  |
| `rbac.rules`                                  | Custom RBAC rules                                                                          | `[]`    |                              |

### Configure ingress or route

Openk9 Docling Processor can openk9 Apis externally.

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

Openk9 Docling Processor supports Readiness, Liveness and Startup Probes.

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

Openk9 Docling Processor chart allow setting resource requests and limits for all containers inside the chart deployment. These are inside the `resources` value (check parameter table). Setting requests is essential for production workloads and these should be adapted to your specific use case.

| `resources`                                         | Set container requests and limits for different resources like CPU or memory (essential for production workloads)                                                                                                 | `{}`             |

### Security

You can use security context settings when you need to handle permissions on your cluster.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `podSecurityContext.enabled`                        | Enable Docling Processor pods' Security Context                                                                                                                                                                            | `true`           |
| `podSecurityContext.fsGroupChangePolicy`            | Set filesystem group change policy                                                                                                                                                                                | `Always`         |
| `podSecurityContext.sysctls`                        | Set kernel settings using the sysctl interface                                                                                                                                                                    | `[]`             |
| `podSecurityContext.supplementalGroups`             | Set filesystem extra groups                                                                                                                                                                                       | `[]`             |
| `podSecurityContext.fsGroup`                        | Set Docling Processor pod's Security Context fsGroup                                                                                                                                                                       | `1001`           |
| `podSecurityContext.enabled`                  | Enabled Docling Processor containers' Security Context                                                                                                                                                                     | `true`           |
| `podSecurityContext.runAsUser`                | Set Docling Processor containers' Security Context runAsUser                                                                                                                                                               | `1001`           |
| `podSecurityContext.runAsGroup`               | Set Docling Processor containers' Security Context runAsGroup                                                                                                                                                              | `1001`           |
| `podSecurityContext.runAsNonRoot`             | Set Docling Processor container's Security Context runAsNonRoot                                                                                                                                                            | `true`           |
| `podSecurityContext.allowPrivilegeEscalation` | Set container's privilege escalation                                                                                                                                                                              | `false`          |
| `podSecurityContext.readOnlyRootFilesystem`   | Set container's Security Context readOnlyRootFilesystem                                                                                                                                                           | `true`           |
| `podSecurityContext.capabilities.drop`        | Set container's Security Context runAsNonRoot                                                                                                                                                                     | `["ALL"]`        |


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

### Advanced logging

No settings are available to set up advanced configuration for logging.


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