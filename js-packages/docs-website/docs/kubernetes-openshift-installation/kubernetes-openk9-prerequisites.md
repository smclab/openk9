---
id: kubernetes-openk9-prerequisites
title: Openk9 requirements
---

In this section is described how to install Openk9 requirements. To install components helm charts are used.

## Installation pre-requirements

### Preparing the installation

OpenK9 uses established products for some aspects/functionalities. These products must be present in Kubernetes
before installing OpenK9. Currently installing through [Helm Charts](https://helm.sh/docs/topics/charts/) is the best choice.

Inside the [openk9-helm-charts repository](https://github.com/smclab/openk9-helm-charts) there is the
`kubernetes/00-requirements` folder where, for each product, there are configuration files for the different installation scenarios.

So clone this repository before start to install.

## Elasticsearch v7.17

### Preparing the installation

Before proceeding with the installation of the chart it is necessary to refine some parameters present in the configuration
file [values.yaml](https://github.com/elastic/helm-charts/blob/v7.15.0/elasticsearch/values.yaml) for local development
scenario using
[00-requirements/00-elasticsearch/local-runtime.yaml](https://github.com/smclab/openk9-helm-charts/blob/master/00-requirements/00-elasticsearch/local-runtime.yaml).
Choose how to refine based on your needs.

Add helm repository if not already done

```bash
helm repo add elastic https://helm.elastic.co
```


```yaml
# This scenario creates a single-instance standalone ElasticSearch
# machine, with the most basic configuration and limited
# resource to be fit in a local K8s/K3s/Minukube environment

# Permit co-located instances for solitary k3s virtual machines.
antiAffinity: "soft"

# Shrink default JVM heap (values have to be the same)
esJavaOpts: "-Xmx512m -Xms512m"

# ElasticSearch Version
imageTag: 7.15.0

# Only a single pod for this development enviroment
replicas: 1

# Allocate smaller chunks of memory per pod.
resources:
  requests:
    cpu: "400m"
    memory: "512Mi"
  limits:
    cpu: "1000m"
    memory: "1Gi"

# Request smaller persistent volumes. In a cloud environment small
# volume means low IOPS
volumeClaimTemplate:
  accessModes: [ "ReadWriteOnce" ]
  resources:
    requests:
      storage: 5Gi
```

### Install ElasticSearch

To install on Kubernetes run:

```bash
helm install elasticsearch elastic/elasticsearch \
  -n openk9 \
  -f 00-requirements/00-elasticsearch/local-runtime.yaml
```

To install on Openshift run:

```bash
helm install elasticsearch elastic/elasticsearch \
  -n openk9 \
  -f 00-requirements/00-elasticsearch/local-crc.yaml
```

### Verify installation

To verify correct installation:

- make port *9200* visible from the local station

```bash
kubectl port-forward -n openk9 svc/elasticsearch-master 9200
```

- openk browser on [http://localhost:9200](http://localhost:9200) to get the informational JSON

```
{
  "name" : "elasticsearch-master-0",
  "cluster_name" : "elasticsearch",
  "cluster_uuid" : "nnIJtp_KRtSX8bExBtNJ8Q",
  "version" : {
    "number" : "7.15.0",
    "build_flavor" : "default",
    "build_type" : "docker",
    "build_hash" : "79d65f6e357953a5b3cbcc5e2c7c21073d89aa29",
    "build_date" : "2021-09-16T03:05:29.143308416Z",
    "build_snapshot" : false,
    "lucene_version" : "8.9.0",
    "minimum_wire_compatibility_version" : "6.8.0",
    "minimum_index_compatibility_version" : "6.0.0-beta1"
  },
  "tagline" : "You Know, for Search"
}
```


## RabbitMQ v3.11

### Preparing the installation

[RabbitMQ](https://www.rabbitmq.com/) is another fundamental element of OpenK9 as it allows asynchronous
dialogue between the different components of the solution.

To install RabbitMQ use the helm Chart created by [Bitnami](https://github.com/bitnami/charts/tree/master/bitnami/rabbitmq)
which manages in an excellent way the different needs both for stand-alone installation both for cluster installation.

Add to helm the repository that contains the charts

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```

Before proceeding with the installation of the chart it is necessary to refine some parameters present in the configuration file
[values.yaml](https://github.com/bitnami/charts/blob/master/bitnami/rabbitmq/values.yaml)
for local development scenario using
[00-requirements/02-rabbitmq/local-runtime.yaml](https://github.com/smclab/openk9-helm-charts/blob/master/00-requirements/02-rabbitmq/local-runtime.yaml).
Choose how to refine based on your needs.

```yaml
# This scenario creates a single-instance standalone RabbitMQ
# machine, with the most basic configuration and limited
# resource to be fit in a local K8s/K3s/Minukube environment

image:
  tag: 3.8

auth:
  username: "openk9"
  password: "openk9"

extraPlugins: "rabbitmq_amqp1_0"

clustering:
  enabled: false

replicaCount: 1

terminationGracePeriodSeconds: 30

resources:
  limits:
    cpu: "500m"
    memory: "512M"
  requests:
    cpu: "100m"
    memory: "256M"

persistence:
  size: 1Gi
```

### Install RabbitMQ

To install on Kubernetes run:

```yaml
helm install rabbitmq bitnami/rabbitmq \
  -n openk9 \
  -f 00-requirements/02-rabbitmq/local-runtime.yaml
```

To install on Openshift run:

```yaml
helm install rabbitmq bitnami/rabbitmq \
  -n openk9 \
  -f 00-requirements/02-rabbitmq/local-crc.yaml
```

### Verify Installation

As suggested by the installation notes

* expose the Management interface on the host PC

```bash
kubectl port-forward -n openk9 svc/rabbitmq 15672:15672
```

* open a browser on [http://localhost:15672](http://localhost:15672)

* log in with the user and password declared in
[00-requirements/02-rabbitmq/local-runtime.yaml](https://github.com/smclab/openk9-helm-charts/blob/master/00-requirements/02-rabbitmq/local-runtime.yaml).

## PostgreSQL v14.x

### Preparing the installation

Different elements of OpenK9 needs relational database. [PostgreSQL](https://www.postgresql.org/) represents best solution.

To install PostgreSQL use helm chart by [Bitnami](https://github.com/bitnami/charts/tree/master/bitnami/postgres).

Add to helm repository that contains chart.

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```

Before chart installation is necessary to refine some parameters present in the configuration file
[values.yaml](https://github.com/bitnami/charts/blob/master/bitnami/postgresql/values.yaml)
for local development scenario using
[00-requirements/03-postgresql/local-runtime.yaml](https://github.com/smclab/openk9-helm-charts/blob/master/00-requirements/03-postgresql/local-runtime.yaml).
Choose how to refine based on your needs.


```yaml
# This scenario creates a single-instance standalone RabbitMQ
# machine, with the most basic configuration and limited
# resource to be fit in a local K8s/K3s/Minukube environment


image:
  # We want latest PostgreSQL 13.x
  tag: 13

auth:
  # enable default "postgres" admin user
  enablePostgresUser: true
  # password for default postgres admin user
  postgresPassword: "system"
  # custom user to create
  username: "openk9"
  # custom user password
  password: "openk9"
  # custom database
  database: "openk9"

architecture: standalone

primary:
  initdb:
    args: "--no-locale --encoding=UTF8"
  resources:
    limits:
      memory: 256Mi
      cpu: 250m
    requests:
      memory: 256Mi
      cpu: 250m

persistence:
  size: 2Gi
```

### Install PostgreSQL

To install on Kubernetes run:

```bash
helm install postgresql bitnami/postgresql \
  -n openk9 \
  -f 00-requirements/03-postgresql/local-runtime.yaml
```

To install on Openshift run:

```bash
helm install postgresql bitnami/postgresql \
  -n openk9 \
  -f 00-requirements/03-postgresql/local-crc.yaml
```

### Verify installation

Use kubectl to activate a temporary pod with psql command

```bash
$ export POSTGRES_PASSWORD=$(kubectl get secret --namespace openk9 postgresql -o jsonpath="{.data.password}" | base64 --decode)
$ kubectl run postgresql-client --rm --tty -i --restart='Never' \
   -n openk9 \
   --image docker.io/bitnami/postgresql:13 \
   --env="PGPASSWORD=$POSTGRES_PASSWORD" \
   --command -- psql --host postgresql -U openk9 -d openk9 -p 5432
```

Using `psql` use `\l` command to see list of databases.

```
If you don't see a command prompt, try pressing enter.

psql (13.5)
Type "help" for help.

openk9=> \l
                             List of databases
   Name    |  Owner   | Encoding | Collate | Ctype |   Access privileges
-----------+----------+----------+---------+-------+-----------------------
 openk9    | openk9   | UTF8     | C       | C     | =Tc/openk9           +
           |          |          |         |       | openk9=CTc/openk9
 postgres  | postgres | UTF8     | C       | C     |
 template0 | postgres | UTF8     | C       | C     | =c/postgres          +
           |          |          |         |       | postgres=CTc/postgres
 template1 | postgres | UTF8     | C       | C     | =c/postgres          +
           |          |          |         |       | postgres=CTc/postgres
(4 rows)

openk9=> \q
pod "postgresql-client" deleted
```

## Keycloack v16.1.1

[Keycloak](https://www.keycloak.org/) is used by OpenK9 to manage and delegate user authentication logics.
As well as some aspects of the authorization logic.

### Preparing the installation

Keycloak needs relational database, preferably PostgreSQL.
A `Job` to create database on postgres is provided as pre-requisite. Database is handled by specific user.

Use [postgresql-keycloak.yaml](https://github.com/smclab/openk9-helm-charts/blob/master/00-requirements/06-keycloak/extras/postgresql-keycloak.yaml).

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: create-keycloak-db
spec:
  ttlSecondsAfterFinished: 120
  template:
    spec:
      containers:
      - name: keycloak-db
        image: bitnami/postgresql:13.6.0
        command: ["/bin/sh", "-c"]
        args: ["psql -c \"CREATE ROLE keycloak WITH LOGIN ENCRYPTED PASSWORD 'openk9'\"; psql -c \"CREATE DATABASE keycloak OWNER keycloak;\"" ]
        env:
        - name: PGHOST
          value: "postgresql"
        - name: PGPORT
          value: "5432"
        - name: PGUSER
          value: "postgres"
        - name: PGPASSWORD
          valueFrom:
            secretKeyRef:
              name: postgresql
              key: postgres-password
      restartPolicy: Never
```

Apply yaml

```bash
kubectl -n openk9 apply -f 00-requirements/06-keycloak/extras/postgresql-keycloak.yaml
```

For future needs create a Secret with the coordinates of the newly created user.

```bash
kubectl -n openk9 create secret generic postgresql-keycloak-secret \
  --from-literal=database=keycloak \
  --from-literal=username=keycloak \
  --from-literal=password=openk9
```

For its installation within K3s we will use the
[Helm Charts](https://github.com/codecentric/helm-charts/tree/master/charts/keycloak) provided by CodeCentric.

Add to helm the repository that contains the charts.

```bash
helm repo add codecentric https://codecentric.github.io/helm-charts
```

Before proceeding with the installation of the chart it is necessary to refine some parameters present in the configuration file
[values.yaml](https://github.com/codecentric/helm-charts/blob/master/charts/keycloak/values.yaml)
for a local development scenario
[00-requirements/06-keycloak/local-runtime.yaml](https://github.com/smclab/openk9-helm-charts/blob/master/00-requirements/06-keycloak/local-runtime.yaml).
Choose how to refine based on your needs.

```yaml
# This scenario creates a single-instance standalone Keycloak
# machine, with the most basic configuration and limited
# resource to be fit in a local K3s/Minukube environment
###############################################################

# Number of Keycloak replicas to deploy
replicaCount: 1

# Keycloak image version. The preferred way is to install the chart
# version with the desired app version
# images:
#   tag: 16.1.1

# Disable PostgreSQL dependency
postgresql:
  enabled: false

# Set existing PostgreSQL
extraEnv: |
  - name: DB_VENDOR
    value: postgres
  - name: DB_ADDR
    value: postgresql.openk9
  - name: DB_PORT
    value: "5432"
  - name: DB_DATABASE
    value: keycloak
  - name: DB_USER_FILE
    value: /secrets/db-creds/username
  - name: DB_PASSWORD_FILE
    value: /secrets/db-creds/password
  - name: JAVA_OPTS
    value: >-
      -XX:+UseContainerSupport
      -XX:MaxRAMPercentage=70.0
      -Djava.net.preferIPv4Stack=true
      -Djboss.modules.system.pkgs=$JBOSS_MODULES_SYSTEM_PKGS
      -Djava.awt.headless=true

extraVolumeMounts: |
  - name: db-creds
    mountPath: /secrets/db-creds
    readOnly: true

extraVolumes: |
  - name: db-creds
    secret:
      secretName: postgresql-keycloak-secret

# Keycloak resource requests and limits
resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 250m
    memory: 1Gi

extraInitContainers: |
  - name: keycloak-to-rabbit
    image: busybox
    command: ['sh', '-c', 'mkdir -p /opt/jboss/keycloak/standalone/deployments; wget https://github.com/aznamier/keycloak-event-listener-rabbitmq/blob/target/keycloak-to-rabbit-1.0.jar?raw=true -O /opt/jboss/keycloak/standalone/deployments/keycloak-to-rabbit-1.0.jar']

ingress:
  enabled: true
  annotations:
    kubernetes.io/ingress.class: traefik
  rules:
    - host: "keycloak.demo.openk9.local"
      paths:
        - path: /
          pathType: Prefix
  tls:
    - hosts:
        - "keycloak.demo.openk9.local"
      secretName: selfsigned-ca-secret
```

### Install Keycloak

To install on Kubernetes run:

```bash
helm install keycloak codecentric/keycloak \
  -n openk9 \
  --version 17.0.2 \
  -f 00-requirements/06-keycloak/local-runtime.yaml
```

To install on Openshift run:

```bash
helm install keycloak codecentric/keycloak \
  -n openk9 \
  --version 17.0.2 \
  -f 00-requirements/06-keycloak/local-crc.yaml
```

Log in to the console to create the admin user and to interact with the Keycloak console.
Port forward to acces to console:

```bash
kubectl -n openk9 port-forward svc/keycloak-http 8280:80
```

Access to console using url [http://localhost:8280](http://localhost:8280) and
define admin user "<u>admin</u>" with password "<u>openk9</u>".

![image-20220226111139171](../../static/img/installation/image-20220226111139171.png)

After creation click on *Administration Console* to access on console with just created user.

![image-20220226111305989](../../static/img/installation/image-20220226112558454.png)

### Expose using ingress

The chart made available by CodeCentric provides the possibility to create an Ingress using Traefik
as seen from the `local-runtime.yaml`. It is therefore advisable to exploit the potential of
the chart rather than create an ad-hoc Ingress.