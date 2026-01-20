# Helm package for Openk9 Datasource

## Introduction

This chart bootstraps Openk9 Datasource deployment on a [Kubernetes](https://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

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
helm upgrade -i datasource openk9/openk9-datasource
```

The command deploys Openk9 Datasource on the Kubernetes cluster in the default configuration. The [Parameters](#parameters) section lists the parameters that can be configured during installation.

# Parameters

### Configure Image

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `image.registry`    | Openk9 Datasource image registry                                                                                  | `REGISTRY_NAME`            |
| `image.repository`  | Openk9 Datasource image repository                                                                                | `REPOSITORY_NAME/openk9` |
| `image.digest`      | Openk9 Datasource image digest in the way sha256:aa.... Please note this parameter, if set, will override the tag | `""`                       |
| `image.pullPolicy`  | Openk9 Datasource image pull policy                                                                               | `IfNotPresent`             |
| `image.pullSecrets` | Specify docker-registry secret names as an array                                                         | `[]`                       |
| `image.debug`       | Set to true if you would like to see extra information on logs                                           | `false`                    |

### Common parameters

| Name                                         | Description                                                                                                                                                             | Value                                             |
| -------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- |
| `nameOverride`                               | String to partially override datasource.fullname template (will maintain the release name)                                                                                | `""`                                              |
| `fullnameOverride`                           | String to fully override datasource.fullname template                                                                                                                     | `""`                                              |
| `commonAnnotations`                          | Annotations to add to all deployed objects                                                                                                                              | `{}`                                              |
| `hostAliases`                                | Deployment pod host aliases                                                                                                                                             | `[]`                                              |

### Quarkus configurations

Openk9 Datasource service is based on Quarkus Framework. Use following parameters to set main quarkus configurations.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `quarkus.ReactiveIdleTimeout`    | Timeout for idle database connections      | `PT30M`            |
| `quarkus.ReactiveMaxSize`  | Size of connection pool used by Datasource                               | `70` |
| `quarkus.PekkoClusterBootstrapServiceName`      | Name used to discover replicas to join in cluster | `datasource`                       |
| `quarkus.HttpCors`    | If Cors is enabled                                                                              | `true`            |
| `quarkus.HttpCorsOrigin`  | Cors origin allowed                                   | `/https://.*.openk9.local/,` |
| `quarkus.LogConsoleJson`      | If json log is enabled | `false`                       |
| `quarkus.LogLevel`  | Default log level                                                                             | `INFO`             |
| `quarkus.LogLevelHibernate`  | Default log level for Hibernate                 | `INFO`             |
| `debug.enabled`    | Enable Java debug mode                             | `false`            |
| `graphql.uiAlwaysInclude`    | Always include GraphQL UI in the application                             | `true`            |

### Scheduling configurations

Openk9 Datasource service handles scheduling for differente datasources. Following parameters allow to handle in better possible way system based on 
loading and capabilities of system.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `scheduling.PurgeCron`    | Cron Expression to handle old orphan data index purge     | `0 */10 * ? * *`            |
| `scheduling.PurgeMaxAge`  | Max Age for old orphan data indexes                              | `2d` |
| `scheduling.Timeout`      | Timeout to handle closing of idle schedulations | `500s`                       |
| `scheduling.WorkersPerNode`    | Active workers for every replica       | `2`            |
| `scheduling.ConsumerMaxRetries`  |  Maximum number of retries attempted when error occurs  | `2` |
| `scheduling.ConsumerTimeout`      | Timeout to handle releasing of a message by a consumer after specific time | `10m`                       |

### Pipeline configurations

Openk9 Datasource service handles pipelines. Following parameters allow to handle in better possible way system based on response time of enrich items.


| Name                | Description                                      | Value |
| ------------------- |--------------------------------------------------|-------|
| `pipeline.HttpTimeout`      | Timeout used by Http client calling enrich items | `10s` |

### QueryParser and search configurations

Openk9 Datasource service handles query parser and query construction. Following parameters allow to handle the parsing of search query tokens and search globally.

| Name                             | Description                                                                                 | Value |
|----------------------------------|---------------------------------------------------------------------------------------------|-------|
| `queryParser.maxTextQueryLength` | Enforce a maximum text query length (disabled if set to 0 or a negative value)              | `0` |
| `queryAnalysis.searchTextLength` | Enforce a maximum search text for query analysis (disabled if set to 0 or a negative value) | `0` |
| `search.page.from` | Enforce a maximum value for search from pagination parameter              | `10000` |
| `search.page.size` | Enforce a maximum value for search size pagination parameter | `200` |

### Datasource specific configurations

Openk9 Datasource service has specific configurations that can be customized to control its behavior.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `datasource.aclQueryExtraParamsEnabled`    | Enable extra parameters for ACL queries                             | `true`            |


### JVM configuration

Openk9 Datasource is a JVM service. To control JVM based tool options use following parameters:

Openk9 Datasource service is based on Quarkus Framework. Use following parameters to set main quarkus configurations.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `jvm.toolOptions`    | JVM Tool Options                                                                       | ``            |

### Configure Database

Openk9 Datasource needs Postgresql or Oracle to work. 

To configure connection to Postgresql or Oracle following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `database.type`    | Database to use. Default is postgresql         | `postgresql`            |
| `hibernate.orm.databaseGeneration`    | Database generation strategy for Hibernate                             | `none`            |
| `hibernate.orm.logSql`    | Enable SQL logging in Hibernate                             | `false`            |

Configure these when database type is postgresql

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `postgresql.reactiveUrl`    | Postgresql url          | `postgresql://postgresql/openk9`            |
| `postgresql.port`  | Port where Postgresql is exposed                                    | `5432` |
| `postgresql.database`    | Postgresql databse          | `openk9`            |
| `postgresql.username`  | Postgresql user                                 | `openk9`             |
| `postgresql.passwordSecretName` | Name of the secret where password is stored                            | `postgres-password`                       |
| `postgresql.keyPasswordSecret`       | Name of the key inside the secret where password is stored                                           | `user-password`                    |
| `postgresql.keyPasswordEnvName`       | Name of environment variable where password is set       | `QUARKUS_DATASOURCE_PASSWORD`   |

Configure these when database type is oracle

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `oracle.reactiveUrl`    | Oracle url          | `oracle://my-oracle-db-oracle-db:1521/K9`            |
| `oracle.port`  | Port where Oracle is exposed                                    | `5432` |
| `oracle.database`    | Oracle databse          | `K9`            |
| `oracle.username`  | Oracle user                                 | `OPENK9`             |
| `oracle.passwordSecretName` | Name of the secret where password is stored                            | `oracle-password`                       |
| `oracle.keyPasswordSecret`       | Name of the key inside the secret where password is stored                                           | `oracle-user-password`                    |
| `oracle.keyPasswordEnvName`       | Name of environment variable where password is set       | `QUARKUS_DATASOURCE_PASSWORD`   |

### Configure Opensearch

Openk9 Datasource needs Opensearch to work. 

To configure connection to Opensearch following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `opensearch.host`    | Opensearch host                             | `- opensearch-cluster-master-headless:9200`            |
| `opensearch.username`  | Opensearch user                                                                               | `opensearch`             |
| `opensearch.passwordSecretName` | Name of the secret where password is stored                            | `opensearch-password`                       |
| `opensearch.keyPasswordSecret`       | Name of the key inside the secret where password is stored               | `OPENSEARCH_INITIAL_ADMIN_PASSWORD`                    |
| `opensearch.keyPasswordEnvName`       | Name of environment variable where password is set       | `QUARKUS_OPENSEARCH_PASSWORD`   |

### Configure Rabbitmq

Openk9 Datasource needs Rabbitmq to work. 

To configure connection to Rabbitmq following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `rabbitmq.host`    | Rabbitmq host                                                                                  | `rabbitmq-headless`            |
| `rabbitmq.port`  | Port where Rabbitmq is exposed                                    | `5672` |
| `rabbitmq.username`  | Rabbitmq user                                                                               | `openk9`             |
| `rabbitmq.passwordSecretName` | Name of the secret where password is stored                            | `rabbitmq-password`                       |
| `rabbitmq.keyPasswordSecret`       | Name of the key inside the secret where password is stored                                           | `rabbitmq-password`                    |
| `rabbitmq.keyPasswordEnvName`       | Name of environment variable where password is set       | `RABBITMQ_PASSWORD`   |
| `messaging.incoming.events.routingKeys`    | Routing keys for incoming events                             | `noop`            |

### Configure Keycloak

Openk9 Datasource needs Keycloak to work. 

To configure connection to Keycloak following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `keycloak.host`    | Keycloak host                         | `keycloak.openk9.local`            |
| `keycloak.clientId`  | Keycloak client                             | `openk9` |
| `keycloak.realm.master`    | Master realm name for Keycloak authentication                             | `master`            |

### Configure connections to other Openk9 services

Openk9 Datasource needs to communicate with other components to work. 
In particular Openk9 Datasource need to communicate with Tenant Manager to perform tenant resolving.

To configure connection to Tenant Manager following parameters are available:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `openk9.tenantManager.host`    | Tenant Manager host                         | `openk9-tenant-manager`            |


### Configure Opentelemetry

Openk9 Datasource supports collecting metrics about traffic using OpenTelemetry.

To enable this feature you need an active instance of OpenTelemetry Collector. Here [official documentation](https://opentelemetry.io/docs/collector/installation/) to install.

Once install you need to configure appropriately some parameters on this Helm Chart.

To activate and set pointing to Opentelemetry collector use following parameters.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `otel.disabled`    | If OpenTelemetry is disabled     | `true`            |
| `otel.endpoint`  | OpenTelemetry Collector endpoint                                    | `` |

### Service account and rbac

Openk9 Datasource doesn't need particular Service Account or rbac. 

But if you want for some reasons associate a pre-created Service Account to Openk9 Datasource you can do using following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `serviceAccount.create`    | If create a default Service Account     | `false`            |
| `serviceAccount.annotations`  | Service Account annotations                                    | `{}` |
| `serviceAccount.name`  | Name of pre created Service Account                                    | `` |

### RBAC parameters

| Name                                          | Description                                                                                | Value   |
| --------------------------------------------- | ------------------------------------------------------------------------------------------ | ------- |
| `serviceAccount.create`                       | Enable creation of ServiceAccount for Datasource pods                                        | `true`  |
| `serviceAccount.name`                         | Name of the created serviceAccount                                                         | `""`    |
| `serviceAccount.automountServiceAccountToken` | Auto-mount the service account token in the pod                                            | `false` |
| `serviceAccount.annotations`                  | Annotations for service account. Evaluated as a template. Only used if `create` is `true`. | `{}`    |
| `rbac.create`                                 | Whether RBAC rules should be created                                                       | `true`  |
| `rbac.rules`                                  | Custom RBAC rules                                                                          | `[]`    |                              |

### Configure ingress or route

Openk9 Datasource doesn't need to openk9 Apis externally.

For Ingress creation presente of an Ingress Controller is mandatory on your cluster. Check [Kubernets Documentation](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/) for availables Ingress Controllers.

But if you want for some reasons you can modifying following parameters:

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `ingress.enabled`    | If enable Ingress creation     | `false`            |
| `ingress.host`  | Ingress host                                    | `{}` |
| `ingress.annotations`  | Ingress annotations                                    | `{}` |
| `ingress.ingressClassName`  | Ingress Class Name (If not present overwrite default Ingress Class Name)         | `{}` |
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

Openk9 Datasource supports Readiness, Liveness and Startup Probes.

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

Openk9 Datasource chart allow setting resource requests and limits for all containers inside the chart deployment. These are inside the `resources` value (check parameter table). Setting requests is essential for production workloads and these should be adapted to your specific use case.

| `resources`                                         | Set container requests and limits for different resources like CPU or memory (essential for production workloads)                                                                                                 | `{}`             |

### Security

You can use security context settings when you need to handle permissions on your cluster.

| Name                | Description                                                                                              | Value                      |
| ------------------- | -------------------------------------------------------------------------------------------------------- | -------------------------- |
| `podSecurityContext.enabled`                        | Enable Datasource pods' Security Context                                                                                                                                                                            | `true`           |
| `podSecurityContext.fsGroupChangePolicy`            | Set filesystem group change policy                                                                                                                                                                                | `Always`         |
| `podSecurityContext.sysctls`                        | Set kernel settings using the sysctl interface                                                                                                                                                                    | `[]`             |
| `podSecurityContext.supplementalGroups`             | Set filesystem extra groups                                                                                                                                                                                       | `[]`             |
| `podSecurityContext.fsGroup`                        | Set Datasource pod's Security Context fsGroup                                                                                                                                                                       | `1001`           |
| `podSecurityContext.enabled`                  | Enabled Datasource containers' Security Context                                                                                                                                                                     | `true`           |
| `podSecurityContext.runAsUser`                | Set Datasource containers' Security Context runAsUser                                                                                                                                                               | `1001`           |
| `podSecurityContext.runAsGroup`               | Set Datasource containers' Security Context runAsGroup                                                                                                                                                              | `1001`           |
| `podSecurityContext.runAsNonRoot`             | Set Datasource container's Security Context runAsNonRoot                                                                                                                                                            | `true`           |
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

### Advanced logging

In case you want to configure Openk9 Datasource logging you can set up following parameters:

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


### To 3.1.0

- remove from values references to keycloak from your scenario values file

- new configurations added to control search

```yaml
## QueryParser configuration
queryParser:
  maxTextQueryLength: 0

## QueryParser configuration
queryAnalysis:
  searchTextLength: 30
```

- new configurations added or modified to control opensearch configurations

```yaml
## Opensearch configuration
opensearch:
  ## Modified to configure Opensearch cluster
  host: 
    - "opensearch-cluster-master-headless:9200"
  sslVerify: false
  protocol: "http"
```

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
