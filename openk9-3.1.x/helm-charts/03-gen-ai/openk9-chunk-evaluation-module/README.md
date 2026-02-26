# OpenK9 Chunk Evaluation Module Helm Chart

This Helm chart deploys the OpenK9 Chunk Evaluation Module on a Kubernetes cluster.

## Description

The Chunk Evaluation Module is responsible for evaluating and analyzing document chunks using various metrics. It communicates with RabbitMQ for message processing and integrates with Phoenix for observability.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+
- RabbitMQ instance
- (Optional) Phoenix instance for observability

## Installing the Chart

To install the chart with the release name `chunk-evaluation`:

```bash
helm install chunk-evaluation ./openk9-chunk-evaluation-module
```

## Uninstalling the Chart

To uninstall/delete the `chunk-evaluation` deployment:

```bash
helm delete chunk-evaluation
```

## Configuration

The following table lists the configurable parameters of the chart and their default values.

### Image Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `image.registry` | Image registry | `""` |
| `image.name` | Image name | `smclab/openk9-chunk-evaluation-module` |
| `image.tag` | Image tag | `Chart.AppVersion` |
| `image.pullPolicy` | Image pull policy | `Always` |
| `image.pullSecrets` | Image pull secrets | `[docker-registry-secret]` |

### Deployment Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `replicaCount` | Number of replicas | `1` |
| `resources.requests.cpu` | CPU request | `250m` |
| `resources.requests.memory` | Memory request | `512Mi` |
| `resources.limits.cpu` | CPU limit | `1000m` |
| `resources.limits.memory` | Memory limit | `2Gi` |

### Service Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `service.type` | Kubernetes service type | `ClusterIP` |
| `service.port` | Service port | `8080` |

### RabbitMQ Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `rabbitmq.host` | RabbitMQ host (supports cross-namespace format: service.namespace.svc.cluster.local) | `rabbitmq.k9-requirements.svc.cluster.local` |
| `rabbitmq.port` | RabbitMQ port | `5672` |
| `rabbitmq.user` | RabbitMQ username | `openk9` |
| `rabbitmq.password.secretName` | Secret name containing RabbitMQ password | `rabbitmq-password` |
| `rabbitmq.password.secretKey` | Key in secret containing RabbitMQ password | `rabbitmq-password` |
| `rabbitmq.retryDelayMs` | Retry delay in milliseconds | `5000` |
| `rabbitmq.maxRetries` | Maximum number of retries | `10` |

### Phoenix Configuration

| Parameter | Description | Default |
|-----------|-------------|---------|
| `phoenix.collectorEndpoint` | Phoenix collector endpoint (supports cross-namespace format) | `http://phoenix.monitoring.svc.cluster.local:6006` |
| `phoenix.projectName` | Phoenix project name | `openk9-chunk-evaluation` |
| `phoenix.apiKey` | Phoenix API key | `""` |

### Persistence

| Parameter | Description | Default |
|-----------|-------------|---------|
| `persistence.enabled` | Enable persistent volume | `false` |
| `persistence.storageClass` | Storage class | `""` |
| `persistence.accessMode` | Access mode | `ReadWriteOnce` |
| `persistence.size` | Volume size | `1Gi` |
| `persistence.mountPath` | Mount path | `/app/dati` |

### Autoscaling

| Parameter | Description | Default |
|-----------|-------------|---------|
| `autoscaling.enabled` | Enable autoscaling | `false` |
| `autoscaling.minReplicas` | Minimum replicas | `1` |
| `autoscaling.maxReplicas` | Maximum replicas | `5` |
| `autoscaling.averageCpuUtilizationPercentage` | Target CPU utilization | `80` |
| `autoscaling.averageMemoryUtilizationPercentage` | Target memory utilization | `80` |

## Example Values

```yaml
replicaCount: 2

image:
  registry: "myregistry.io"
  tag: "latest"

rabbitmq:
  host: "rabbitmq.k9-requirements.svc.cluster.local"
  user: "myuser"
  password:
    secretName: "rabbitmq-password"
    secretKey: "rabbitmq-password"

phoenix:
  collectorEndpoint: "http://phoenix.monitoring.svc.cluster.local:6006"
  apiKey: "my-api-key"

persistence:
  enabled: true
  size: 5Gi

resources:
  requests:
    cpu: 500m
    memory: 512Mi
  limits:
    cpu: 1000m
    memory: 1024Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
```

## Dependencies

This chart requires:
- A running RabbitMQ instance
- (Optional) A Phoenix instance for observability

## Notes

- The module processes messages from RabbitMQ queues
- Phoenix integration is optional but recommended for production monitoring
- Persistent storage is optional and can be enabled for data retention
