# Helm package for Openk9 API Gateway

## Introduction

This chart bootstraps Openk9 API Gateway deployment on a [Kubernetes](https://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

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
helm upgrade -i api-gateway openk9/openk9-api-gateway
```

The command deploys Openk9 API Gateway on the Kubernetes cluster in the default configuration. The [Parameters](#parameters) section lists the parameters that can be configured during installation.

# Parameters

### Configure Image

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `image.registry`    | Openk9 API Gateway image registry                                                                        | `REGISTRY_NAME`            |
| `image.name`        | Openk9 API Gateway image name                                                                            | `smclab/openk9-api-gateway`|
| `image.tag`         | Openk9 API Gateway image tag (overrides the chart appVersion)                                            | `""`                       |
| `image.pullPolicy`  | Openk9 API Gateway image pull policy                                                                     | `Always`                   |
| `image.pullSecrets` | Specify docker-registry secret names as an array                                                         | `[docker-registry-secret]` |

### Common parameters

| Name                                         | Description                                                                                             | Value                                             |
| -------------------------------------------- | ------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| `nameOverride`                               | String to partially override api-gateway.fullname template (will maintain the release name)             | `""`                                              |
| `fullnameOverride`                           | String to fully override api-gateway.fullname template                                                  | `""`                                              |
| `commonAnnotations`                          | Annotations to add to all deployed objects                                                              | `{}`                                              |
| `hostAliases`                                | Deployment pod host aliases                                                                             | `[]`                                              |

### Spring Gateway configurations

Openk9 API Gateway service is based on Spring Cloud Gateway. Use following parameters to set main configurations.

| Name                                    | Description                                                                                              | Value                      |
| --------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `spring.gateway.trustedProxies`         | Trusted proxies pattern for X-Forwarded-* headers                                                        | `.*`                       |
| `debug.enabled`                         | Enable Java debug mode                                                                                   | `false`                    |

### Configure Database

Openk9 API Gateway needs PostgreSQL to work. 

To configure connection to PostgreSQL following parameters are available:

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `database.type`                             | Database to use. Default is postgresql                                                                   | `postgresql`               |
| `database.postgresql.host`                  | PostgreSQL host                                                                                          | `postgresql`               |
| `database.postgresql.port`                  | Port where PostgreSQL is exposed                                                                         | `5432`                     |
| `database.postgresql.database`              | PostgreSQL database                                                                                      | `apigateway`               |
| `database.postgresql.username`              | PostgreSQL user                                                                                          | `openk9`                   |
| `database.postgresql.passwordSecretName`    | Name of the secret where password is stored                                                              | `postgres-password`        |
| `database.postgresql.keyPasswordSecret`     | Name of the key inside the secret where password is stored                                               | `user-password`            |
| `database.postgresql.keyPasswordEnvName`    | Name of environment variable where password is set                                                       | `SPRING_R2DBC_PASSWORD`    |
| `database.postgresql.liquibasePasswordEnvName` | Name of environment variable where Liquibase password is set                                          | `SPRING_LIQUIBASE_PASSWORD`|

### Database Job configuration

Openk9 API Gateway can run a database initialization job.

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `databaseJob.enabled`                       | Enable database initialization job                                                                       | `true`                     |
| `databaseJob.ttlSecondsAfterFinished`       | TTL for completed job                                                                                    | `120`                      |
| `databaseJob.image.registry`                | Database job image registry                                                                              | `docker.io`                |
| `databaseJob.image.name`                    | Database job image name                                                                                  | `bitnamilegacy/postgresql` |
| `databaseJob.image.tag`                     | Database job image tag                                                                                   | `16.2.0-debian-12-r8`      |

### Configure RabbitMQ

Openk9 API Gateway needs RabbitMQ to work. 

To configure connection to RabbitMQ following parameters are available:

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `rabbitmq.host`                             | RabbitMQ host                                                                                            | `rabbitmq-headless`        |
| `rabbitmq.port`                             | Port where RabbitMQ is exposed                                                                           | `5672`                     |
| `rabbitmq.username`                         | RabbitMQ user                                                                                            | `openk9`                   |
| `rabbitmq.passwordSecretName`               | Name of the secret where password is stored                                                              | `rabbitmq-password`        |
| `rabbitmq.keyPasswordSecret`                | Name of the key inside the secret where password is stored                                               | `rabbitmq-password`        |
| `rabbitmq.keyPasswordEnvName`               | Name of environment variable where password is set                                                       | `SPRING_RABBITMQ_PASSWORD` |

### Configure connections to other Openk9 services

Openk9 API Gateway needs to communicate with other components to work. 
In particular Openk9 API Gateway needs to communicate with Searcher and Datasource services.

To configure connection to these services following parameters are available:

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `openk9.searcher.host`                      | Searcher service host                                                                                    | `openk9-searcher`          |
| `openk9.searcher.port`                      | Searcher service port                                                                                    | `8080`                     |
| `openk9.datasource.host`                    | Datasource service host                                                                                  | `openk9-datasource`        |
| `openk9.datasource.port`                    | Datasource service port                                                                                  | `8080`                     |

### Service account

Openk9 API Gateway doesn't need particular Service Account. 

But if you want for some reasons associate a pre-created Service Account to Openk9 API Gateway you can do using following parameters:

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `serviceAccount.create`                     | Enable creation of ServiceAccount for API Gateway pods                                                   | `true`                     |
| `serviceAccount.annotations`                | Service Account annotations                                                                              | `{}`                       |
| `serviceAccount.name`                       | Name of the created serviceAccount                                                                       | `api-gateway`              |

### Configure ingress or route

Openk9 API Gateway exposes APIs externally through Ingress or Route.

For Ingress creation presence of an Ingress Controller is mandatory on your cluster. Check [Kubernetes Documentation](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/) for available Ingress Controllers.

To configure Ingress use following parameters:

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `ingress.enabled`                           | If enable Ingress creation                                                                               | `false`                    |
| `ingress.host`                              | Ingress host                                                                                             | `openk9.local`             |
| `ingress.annotations`                       | Ingress annotations                                                                                      | `{}`                       |
| `ingress.servicePort`                       | Service port for Ingress                                                                                 | `8080`                     |
| `ingress.paths`                             | Paths exposed by Ingress                                                                                 | `[/api/datasource, /api/searcher]` |
| `ingress.tls.enabled`                       | If enable tls on Ingress                                                                                 | `false`                    |
| `ingress.tls.secretName`                    | Secret with tls certificate to associate to Ingress                                                      | `openk9-tls-star-secret`   |

To configure Route for OpenShift use:

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `route.enabled`                             | If enable Route creation                                                                                 | `false`                    |
| `route.host`                                | Route host                                                                                               | `openk9.local`             |
| `route.annotations`                         | Route annotations                                                                                        | `{}`                       |
| `route.paths`                               | Paths exposed by Route                                                                                   | `[/api/datasource, /api/searcher]` |
| `route.tls.enabled`                         | If enable tls on Route                                                                                   | `false`                    |
| `route.tls.secretName`                      | Secret with tls certificate to associate to Route                                                        | `openk9-tls-star-secret`   |

### Readiness and liveness probes

Openk9 API Gateway supports Readiness and Liveness Probes.

It uses Spring Boot Actuator health check endpoints to perform health check actions. 

To configure probes appropriately you can use following parameters:

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `livenessProbe.enabled`                     | Enable livenessProbe                                                                                     | `true`                     |
| `livenessProbe.path`                        | Path for livenessProbe                                                                                   | `/actuator/health/liveness`|
| `livenessProbe.initialDelaySeconds`         | Initial delay seconds for livenessProbe                                                                  | `30`                       |
| `livenessProbe.periodSeconds`               | Period seconds for livenessProbe                                                                         | `10`                       |
| `livenessProbe.timeoutSeconds`              | Timeout seconds for livenessProbe                                                                        | `5`                        |
| `livenessProbe.failureThreshold`            | Failure threshold for livenessProbe                                                                      | `5`                        |
| `livenessProbe.successThreshold`            | Success threshold for livenessProbe                                                                      | `1`                        |
| `readinessProbe.enabled`                    | Enable readinessProbe                                                                                    | `true`                     |
| `readinessProbe.path`                       | Path for readinessProbe                                                                                  | `/actuator/health/readiness`|
| `readinessProbe.initialDelaySeconds`        | Initial delay seconds for readinessProbe                                                                 | `30`                       |
| `readinessProbe.periodSeconds`              | Period seconds for readinessProbe                                                                        | `10`                       |
| `readinessProbe.timeoutSeconds`             | Timeout seconds for readinessProbe                                                                       | `5`                        |
| `readinessProbe.failureThreshold`           | Failure threshold for readinessProbe                                                                     | `5`                        |
| `readinessProbe.successThreshold`           | Success threshold for readinessProbe                                                                     | `1`                        |

### Resource requests and limits

Openk9 API Gateway chart allows setting resource requests and limits for all containers inside the chart deployment. These are inside the `resources` value (check parameter table). Setting requests is essential for production workloads and these should be adapted to your specific use case.

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `resources.requests.cpu`                    | CPU resource requests                                                                                    | `100m`                     |
| `resources.requests.memory`                 | Memory resource requests                                                                                 | `128Mi`                    |
| `resources.limits.cpu`                      | CPU resource limits                                                                                      | `1000m`                    |
| `resources.limits.memory`                   | Memory resource limits                                                                                   | `512Mi`                    |

### Autoscaling

Openk9 API Gateway supports Horizontal Pod Autoscaling.

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `autoscaling.enabled`                       | Enable autoscaling                                                                                       | `false`                    |
| `autoscaling.minReplicas`                   | Minimum number of replicas                                                                               | `1`                        |
| `autoscaling.maxReplicas`                   | Maximum number of replicas                                                                               | `5`                        |
| `autoscaling.averageCpuUtilizationPercentage` | Target CPU utilization percentage                                                                      | `80`                       |
| `autoscaling.averageMemoryUtilizationPercentage` | Target memory utilization percentage                                                                | `80`                       |

### Metrics

Openk9 API Gateway supports collecting metrics using Prometheus.

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `metrics.enabled`                           | Enable metrics                                                                                           | `false`                    |
| `metrics.serviceMonitor.enabled`            | If the operator is installed in your cluster, set to true to create a Service Monitor Entry             | `false`                    |
| `metrics.serviceMonitor.namespace`          | Namespace which Prometheus is running in                                                                 | `""`                       |
| `metrics.serviceMonitor.labels`             | Extra labels for the ServiceMonitor                                                                      | `{release: prometheus-stack}` |
| `metrics.serviceMonitor.path`               | HTTP path to scrape for metrics                                                                          | `/actuator/prometheus`     |
| `metrics.serviceMonitor.port`               | Port for metrics expose                                                                                  | `management`               |
| `metrics.serviceMonitor.interval`           | Interval at which metrics should be scraped                                                              | `30s`                      |

### Security

You can use security context settings when you need to handle permissions on your cluster.

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `podSecurityContext`                        | API Gateway pods' Security Context                                                                       | `{}`                       |
| `securityContext`                           | API Gateway containers' Security Context                                                                 | `{}`                       |

### Scheduling

| Name                                        | Description                                                                                              | Value                      |
| ------------------------------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `nodeSelector`                              | Node labels for pod assignment                                                                           | `{}`                       |
| `tolerations`                               | Tolerations for pod assignment                                                                           | `[]`                       |
| `affinity`                                  | Affinity for pod assignment                                                                              | `{}`                       |

## Production Scenario

For production deployments, use the provided production scenario file:

```bash
helm upgrade -i api-gateway openk9/openk9-api-gateway -f scenarios/production.yaml
```

The production scenario includes:
- Higher resource limits
- Autoscaling enabled
- Ingress with TLS enabled
- Metrics collection enabled
- Proper namespace configuration for dependencies
