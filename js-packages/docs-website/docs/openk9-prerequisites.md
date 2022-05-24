---
id: openk9-prerequisites
title: Install Openk9 prerequisites
---

In this section is described how to install Openk9 prerequisites. To install components helm charts are used.

## Installation pre-requirements

### Preparing the installation

OpenK9 uses established products for some aspects/functionalities. These products must be present in Kubernetes
before installing OpenK9. Currently installing through [Helm Charts](https://helm.sh/docs/topics/charts/) is the best choice.

Inside the [openk9-kubernetes repository](https://github.com/smclab/openk9-kubernetes) there is the
`kubernetes/00-requirements` folder where, for each product, there are configuration files for the different installation scenarios.

So clone this repository before start to install.

## Elasticsearch v7.15.0

### Preparing the installation

Before proceeding with the installation of the chart it is necessary to refine some parameters present in the configuration
file [values.yaml](https://github.com/elastic/helm-charts/blob/v7.15.0/elasticsearch/values.yaml) for local development
scenario using
[00-requirements/00-elasticsearch/local-runtime.yaml](https://github.com/smclab/openk9-kubernetes/blob/master/00-requirements/00-elasticsearch/local-runtime.yaml).
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

```bash
helm install elasticsearch elastic/elasticsearch \
  -n openk9 \
  -f 00-requirements/00-elasticsearch/local-runtime.yaml
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

### Expose using ingress

If you want to expose ElasticSearch outside Kubernetes you need to configure an **Ingress**,
preferably in https.
The chart allows you to do this using *nginx-controller* as a backend while K3s cluster has *traefik*.

Create Ingress

```bash
cat <<_EOF_ | kubectl apply -n openk9 -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: elasticsearch-master
  annotations:
    kubernetes.io/ingress.class: traefik
spec:
  rules:
    - host: "elastic.demo.openk9.local"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name:  elasticsearch-master-headless
                port:
                  number: 9200
  tls:
    - hosts:
        - "elastic.demo.openk9.local"
      secretName: selfsigned-ca-secret
_EOF_
```

where:

* expose ElasticSearch as *elastic.demo.openk9.local*. In order to use this hostname register it in your hosts file

* provide both http and https access(using the self-signed certificates produced by the cert-manager)

* use the headless service to get the IPs of the pods and allow Traefik to apply its load-balancing logic.

At this point, after updating the hosts file, if you open a browser on
[http://elastic.demo.openk9.local](http://elastic.demo.openk9.local) you should get the json with the server info.


## Kibana v7.15.0

### Preparing the installation

Kibana is not a fundamental component for a local environment; if you don't need to directly access data on
Elasticsearch you can skip this step.
Also for Kibana you can use the official [Helm Charts](https://github.com/elastic/helm-charts).

I add to helm the repository that contains the charts(if not already done for Elasticsearch)

```bash
helm repo add elastic https://helm.elastic.co
```

Before proceeding with the installation of the chart, it is necessary to refine some parameters present in the configuration
file [values.yaml](https://github.com/elastic/helm-charts/blob/v7.15.0/kibana/values.yaml) for local development
scenario using
[00-requirements/01-kibana/local-runtime.yaml](https://github.com/smclab/openk9-kubernetes/blob/master/00-requirements/01-kibana/local-runtime.yaml).
Choose how to refine based on your needs.

```yaml
---
# ElasticSearch Kibana Version
imageTag: 7.15.0

# ElasticSearch server (dns resolution needs namespace)
elasticsearchHosts: "http://elasticsearch-master.openk9:9200"

# Only a single pod for this development enviroment
replicas: 1

# Allocate smaller chunks of memory per pod.
resources:
  requests:
    cpu: "100m"
    memory: "256M"
  limits:
    cpu: "1000m"
    memory: "512M"
```

### Install Kibana

```bash
helm install kibana elastic/kibana \
  -n openk9 \
  -f 00-requirements/01-kibana/local-runtime.yaml
```

### Verify installation

To verify correct installation:

- verify that pod is available

```bash
kubectl get pod -n openk9
```

```bash
NAME                             READY   STATUS    RESTARTS   AGE
elasticsearch-master-0           1/1     Running   0          24m
kibana-kibana-759bc99675-qt4n9   1/1     Running   0          67s
```

- make the Kibana port usable outside the virtual machine

```
kubectl port-forward -n openk9 deployment/kibana-kibana 5601
```

- from local open [http://localhost:5601](http://localhost:5601)


### Expose using ingress


If you want to expose Kibana outside of Kubernetes you need to configure an **Ingress**, preferably in https.
The chart allows you to do this using *nginx-controller* as a backend while K3s cluster has *traefik*.

Create Ingress

```bash
cat <<_EOF_ | kubectl apply -n openk9 -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kibana-master
  annotations:
    kubernetes.io/ingress.class: traefik
spec:
  rules:
    - host: "kibana.demo.openk9.local"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name:  kibana-master-headless
                port:
                  number: 5601
  tls:
    - hosts:
        - "kibana.demo.openk9.local"
      secretName: selfsigned-ca-secret
_EOF_
```

where:

* expose the Kibana console as *kibana.demo.openk9.local*. In order to use this hostname you have to register it in your hosts file

* provide both http and https access(using the self-signed certificates produced by the cert-manager)

* use the headless service to get the IPs of the pods and allow Traefik to apply its load-balancing logics.


At this point, after updating the hosts file, open a browser on [http://kibana.demo.openk9.local](http://kibana.demo.openk9.local).
You should access the console.


## RabbitMQ v3.8

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
[00-requirements/02-rabbitmq/local-runtime.yaml](https://github.com/smclab/openk9-kubernetes/blob/master/00-requirements/02-rabbitmq/local-runtime.yaml).
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

```yaml
helm install rabbitmq bitnami/rabbitmq \
  -n openk9 \
  -f 00-requirements/02-rabbitmq/local-runtime.yaml
```

### Verify Installation

As suggested by the installation notes

* expose the Management interface on the host PC

```bash
kubectl port-forward -n openk9 svc/rabbitmq 15672:15672
```

* open a browser on [http://localhost:15672](http://localhost:15672)

* log in with the user and password declared in
[00-requirements/02-rabbitmq/local-runtime.yaml](https://github.com/smclab/openk9-kubernetes/blob/master/00-requirements/02-rabbitmq/local-runtime.yaml).


### Expose using ingress

To expose the RabbitMQ console outside Kubernetes it is necessary to configure an **Ingress**, preferably in https.
The chart allows you to do this using *nginx-controller* as a backend while K3s cluster has *traefik*.

Create Ingress

```bash
cat <<_EOF_ | kubectl apply -n openk9 -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kubernetes-dashboard
  annotations:
    kubernetes.io/ingress.class: traefik
spec:
  rules:
    - host: "dashboard.openk9.local"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: kubernetes-dashboard
                port:
                  number: 8
  tls:
    - hosts:
        - "rabbitmq.demo.openk9.local"
      secretName: selfsigned-ca-secret
_EOF_
```

where:

* expose the RabbitMQ console as *rabbitmq.demo.openk9.local*. In order to use this hostname to register it in your hosts file

* provide both http and https access(using the self-signed certificates produced by the cert-manager)

* use the headless service to get the IPs of the pods and allow Traefik to apply its load-balanciong logic.


At this point, after updating the hosts file,
open browser on [http://rabbitmq.demo.openk9.local](http://rabbitmq.demo.openk9.local) you should access the console.

## PostgreSQL v13.x

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
[00-requirements/03-postgresql/local-runtime.yaml](https://github.com/smclab/openk9-kubernetes/blob/master/00-requirements/03-postgresql/local-runtime.yaml).
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

```bash
helm install postgresql bitnami/postgresql \
  -n openk9 \
  -f 00-requirements/03-postgresql/local-runtime.yaml
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

## Neo4J v4.2.6

### Preparing the installation

[Neo4J](https://neo4j.com/) is used by OpenK9 to manage/describe the relationships between indexed items and construct a Knowledge graph for the solution.

To install Neo4J use the [Helm Chart](https://github.com/neo4j-contrib/neo4j-helm) made available by the community.

> For Neo4J version 4.3 and later, the official [Helm Charts](https://neo4j.com/labs/neo4j-helm/1.0.0/) are available.

Before proceeding with the installation of the chart it is necessary to refine some parameters present in the configuration file
[values.yaml](https://github.com/neo4j-contrib/neo4j-helm/blob/4.2.6-1/values.yaml) for local development scenario
[00-requirements/04-neo4j/local-runtime.yaml](https://github.com/smclab/openk9-kubernetes/blob/master/00-requirements/04-neo4j/local-runtime.yaml).
Choose how to refine based on your needs.

```yaml
# This scenario creates a single-instance standalone Neo4j
# machine, with the most basic configuration and limited
# resource to be fit in a local K3s/Minukube environment
###############################################################

core:
  standalone: true
  numberOfServers: 1
  persistentVolume:
    size: 4Gi
  resources:
    limits:
      cpu: "500m"
      memory: 2Gi
    requests:
      cpu: "100m"
      memory: 512Mi

dbms:
    memory:
        use_memrec: false
        heap:
            initial_size: 1024m
            max_size: 1024m
        pagecache:
            size: 123m
        transaction:
            memory_allocation: ON_HEAP
            max_size: 60m
            global_max_size: 200m


acceptLicenseAgreement: "yes"
neo4jPassword: openk9
defaultDatabase: "neo4j"
```

From chart:

- default user is `neo4j`

- default database is `neo4j`

- APOC plugin, needed for OpenK9, is already included in image

- volumes "conf", "plugin", etc. are handles as directories in volume "data"


### Install Neo4j

```bash
helm install neo4j \
   https://github.com/neo4j-contrib/neo4j-helm/releases/download/4.2.6-1/neo4j-4.2.6-1.tgz \
   -n openk9 \
   -f  00-requirements/04-neo4j/local-runtime.yaml
```

Your cluster is now being deployed, and may take up to 5 minutes to become available.
If you'd like to track status and wait on your rollout to complete, run:

```bash
$ kubectl rollout status \
    --namespace openk9 \
    StatefulSet/neo4j-neo4j-core \
    --watch
```

We can see the content of the logs by running the following command:

```bash
$ kubectl logs --namespace openk9 -l \
    "app.kubernetes.io/instance=neo4j,app.kubernetes.io/name=neo4j,app.kubernetes.io/component=core"
```

We can now run a query to find the topology of the cluster.

```bash
export NEO4J_PASSWORD=$(kubectl get secrets neo4j-neo4j-secrets --namespace openk9 -o jsonpath='{.data.neo4j-password}' | base64 -d)
kubectl run -it --rm cypher-shell \
    --image=neo4j:4.2.6-enterprise \
    --restart=Never \
    --namespace openk9 \
    --command -- ./bin/cypher-shell -u neo4j -p "$NEO4J_PASSWORD" -a neo4j://neo4j-neo4j.openk9.svc.cluster.local "call dbms.routing.getRoutingTable({}, 'system');"
```

This will print out the addresses of the members of the cluster.

Note:
You'll need to substitute *password* with the password you set when installing the Helm package.
If you didn't set a password, one will be auto generated.
You can find the base64 encoded version of the password by running the following command:

```
kubectl get secrets neo4j-neo4j-secrets -o yaml --namespace openk9
```

### Verify installation

Wait a couple of minutes for all the resources to be installed, the run following commands to verify the correct activation of the service.

```bash
$ export NEO4J_PASSWORD=$(kubectl get secrets neo4j-neo4j-secrets --namespace openk9 -o jsonpath='{.data.neo4j-password}' | base64 -d)
$ kubectl run -it --rm cypher-shell \
    --image=neo4j:4.2.6-enterprise \
    --restart=Never \
    --namespace openk9 \
    --command -- ./bin/cypher-shell -u neo4j -p "$NEO4J_PASSWORD" -a neo4j://neo4j-neo4j.openk9.svc.cluster.local "call dbms.routing.getRoutingTable({}, 'system');"
```

```
If you don't see a command prompt, try pressing enter.
+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ttl | servers                                                                                                                                                                                                                                                                                      |
+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| 300 | [{addresses: ["neo4j-neo4j-core-0.neo4j-neo4j.openk9.svc.cluster.local:7687"], role: "WRITE"}, {addresses: ["neo4j-neo4j-core-0.neo4j-neo4j.openk9.svc.cluster.local:7687"], role: "READ"}, {addresses: ["neo4j-neo4j-core-0.neo4j-neo4j.openk9.svc.cluster.local:7687"], role: "ROUTE"}] |
+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

1 row available after 451 ms, consumed after another 9 ms
pod "cypher-shell" deleted
```

### Neo4j Browser

Neo4j disposes of a web console to perform queries on data. We need to expose this console.

 ```bash
 kubectl -n openk9 port-forward svc/neo4j-neo4j 7474:7474 7687:7687
 ```

Then you can access to [http://localhost:7474/browser](http://localhost:7474/browser):

 * as URL Connect uso "bolt://localhost:7687"
 * as username use "neo4j"
 * as password use password specified in
 [00-requirements/04-neo4j/local-runtime.yaml](https://github.com/smclab/openk9-kubernetes/blob/master/00-requirements/04-neo4j/local-runtime.yaml)

Then you can perform queries using Cypher Query Language.

#### Notes


As indicated in *External Exposure of Neo4j Clusters - Neo4j-Helm User Guide* it is possible to make Neo4J accessible
from outside Kubernetes. Check
[https://github.com/neo4j-contrib/neo4j-helm/tree/master/tools/external-exposure-legacy](https://github.com/neo4j-contrib/neo4j-helm/tree/master/tools/external-exposure-legacy)

## Consul v1.11.2

### Preparing the installation

[Consul](https://www.consul.io/) is a product developed by HashiCorp and provides
[Service Discovery](https://www.consul.io/use-cases/discover-services) and
[KeyValue Store](https://www.consul.io/docs/dynamic-app-config/kv) features.

To install Consul use [Helm Chart](https://github.com/hashicorp/consul-k8s/tree/main/charts/consul)
provided by HashiCorp.

Add to helm repository:

```bash
helm repo add hashicorp https://helm.releases.hashicorp.com
```

Before chart installation is necessary to change some parameters in
[values.yaml](https://github.com/hashicorp/consul-k8s/blob/v0.40.0/charts/consul/values.yaml)
for local development
[00-requirements/05-consul/local-runtime.yaml](https://github.com/smclab/openk9-kubernetes/tree/master/00-requirements/05-consul).

```yaml
# This scenario creates a single-instance standalone Consul
# machine, with the most basic configuration and limited
# resource to be fit in a local K3s/Minukube environment
###############################################################

global:
  name: consul

  # The name of the datacenter that the agents should
  # register as. This can't be changed once the Consul cluster is up and running
  # since Consul doesn't support an automatic way to change this value currently:
  # https://github.com/hashicorp/consul/issues/1858.
  datacenter: dc1

  # The domain Consul will answer DNS queries for
  # (see `-domain` (https://consul.io/docs/agent/options#_domain)) and the domain services synced from
  # Consul into Kubernetes will have, e.g. `service-name.service.consul`.
  domain: consul

server:
  replicas: 1
  storage: 1Gi

ui:
  enabled: true
  service:
    type: 'NodePort'

# Configures the automatic Connect sidecar injector.
connectInject:
  # True if you want to enable connect injection. Set to "-" to inherit from
  # global.enabled.
  enabled: true

  # The number of deployment replicas.
  replicas: 1

  # If true, the injector will inject the
  # Connect sidecar into all pods by default. Otherwise, pods must specify the
  # injection annotation (https://consul.io/docs/k8s/connect#consul-hashicorp-com-connect-inject)
  # to opt-in to Connect injection. If this is true, pods can use the same annotation
  # to explicitly opt-out of injection.
  default: false

controller:
  enabled: true
```

### Install Consul

Install Consul in ad-hoc namespace


```bash
helm install consul hashicorp/consul \
  --namespace consul --create-namespace \
  -f 00-requirements/05-consul/local-runtime.yaml
```

### Verify installation


Acces to *consul*  inside pod and get list of registered nodes

```bash
kubectl -n consul exec -it consul-server-0 -- consul members
```

```
Node                 Address         Status  Type    Build   Protocol  DC   Partition  Segment
consul-server-0      10.85.0.8:8301  alive   server  1.11.2  2         dc1  default    <all>
k3s-master.vm.local  10.85.0.6:8301  alive   client  1.11.2  2         dc1  default    <default>

### Consul Dashboard

Installation contains also a dashboard. Perform port-forward to connect locally to it.

```bash
kubectl -n consul port-forward consul-server-0 8500:8500
```

Then access to url [http://localhost:8500](http://localhost:8500)

### Expose using ingress

To expose the Consul dashboard outside Kubernetes it is necessary to configure an **Ingress**, preferably in https.
The chart allows to do this using *nginx-controller* as a backend while K3s cluster has *traefik*.

```bash
cat <<_EOF_ | kubectl apply -n consul -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: consul-openk9
  annotations:
    kubernetes.io/ingress.class: traefik
spec:
  rules:
    - host: "consul.demo.openk9.local"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name:  consul-server
                port:
                  number: 8500
  tls:
    - hosts:
        - "consul.demo.openk9.local"
      secretName: selfsigned-ca-secret
_EOF_
```

where:

* expose the Consul dashboard as *consul.demo.openk9.local*. In order to use this hostname register it in your hosts file

* provide both http and https access (using the self-signed certificates produced by the cert-manager)

* use the headless service to get the IPs of the pods and allow Traefik to apply its load-balanciong logic.

At this point, after updating the hosts file, if you open a browser on
[http://consul.demo.openk9.local](http://consul.demo.openk9.local) you should access the console

## Keycloack v16.1.1

[Keycloak](https://www.keycloak.org/) is used by OpenK9 to manage and delegate user authentication logics.
As well as some aspects of the authorization logic.

### Preparing the installation

Keycloak needs relational database, preferably PostgreSQL.
A `Job` to create database on postgres is provided as pre-requisite. Database is handled by specific user.

Use [postgresql-keycloak.yaml](https://github.com/smclab/openk9-kubernetes/blob/master/00-requirements/06-keycloak/extras/postgresql-keycloak.yaml).

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
[00-requirements/06-keycloak/local-runtime.yaml](https://github.com/smclab/openk9-kubernetes/blob/master/00-requirements/06-keycloak/local-runtime.yaml).
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

```bash
helm install keycloak codecentric/keycloak \
  -n openk9 \
  --version 17.0.2 \
  -f 00-requirements/06-keycloak/local-runtime.yaml
```


Log in to the console to create the admin user and to interact with the Keycloak console.
Port forward to acces to console:

```bash
kubectl -n openk9 port-forward svc/keycloak-http 8280:80
```

Access to console using url [http://localhost:8280](http://localhost:8280) and
define admin user "<u>admin</u>" with password "<u>openk9</u>".

![image-20220226111139171](../static/img/installation/image-20220226111139171.png)

After creation click on *Administration Console* to access on console with just created user.

![image-20220226111305989](../static/img/installation/image-20220226112558454.png)

### Expose using ingress

The chart made available by CodeCentric provides the possibility to create an Ingress using Traefik
as seen from the `local-runtime.yaml`. It is therefore advisable to exploit the potential of
the chart rather than create an ad-hoc Ingress.

## Adminer v4.8.1

Adminer (also known as phpMinAdmin) is a database management tool written in PHP.
The supported engines are MySQL, PostgreSQL, SQLite, MS SQL, Oracle, Firebird, SimpleDB, Elasticsearch and MongoDB.

This is an **optional** requirement: in local scenarios is preferable to use "port-forwards" and access databases with desktop
tools like [DBeaver](https://dbeaver.io/).
However, it is true that in a cloud or non-local context,
the availability of a tool that communicates with the DB without network latencies is an important help.

For installation we use the chart prepared by us:

```bash
helm install adminer 00-requirements/07-adminer -n openk9
```

### Verify installation

To verify installation:

* active port-forward from local:

```bash
kubectl -n openk9 port-forward svc/adminer 18080:8080
```

* access to url "[httpd://localhost:18080](httpd://localhost:18080)" and authenticate with PostgreSQL credentials.

### Expose using ingress

To expose Adminer outside Kubernetes it is necessary to configure an **Ingress**, preferably in https.
The chart allows to do this using *nginx-controller* as a backend while K3s cluster has *traefik*.

```bash
cat <<_EOF_ | kubectl apply -n openk9 -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: adminer
  annotations:
    kubernetes.io/ingress.class: traefik
spec:
  rules:
    - host: "adminer.demo.openk9.local"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name:  adminer
                port:
                  number: 8080
  tls:
    - hosts:
        - "adminer.demo.openk9.local"
      secretName: selfsigned-ca-secret
_EOF_
```

where:

* expose Adminer as *adminer.demo.openk9.local*. In order to use this hostname register it in your hosts file

* provide both http and https access (using the self-signed certificates produced by the cert-manager)

* use the headless service to get the IPs of the pods and allow Traefik to apply its load-balanciong logic.

At this point, after updating the hosts file, if you open a browser on
[http://adminer.demo.openk9.local](http://adminer.demo.openk9.local) you should access the console
