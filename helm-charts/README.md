# OpenK9 Helm Charts 

This repository contains the Helm Charts and other elements that allow you to install OpenK9 **version 3.0.0-SNAPSHOT**:

- in a standard [Kubernets](https://kubernetes.io/) installed in the Cloud or On-Premises
- in a standard [OpenShift](https://www.redhat.com/it/technologies/cloud-computing/openshift) installed in the Cloud or On-Premises

For documentation to install previous versions search and checkout for related tag in this repository.

Read more about compatibility matrix on Github.

## Index

1. [Prerequisites](#prerequisites)
    - [Namespace](#namespace)
    - [Domain](#domain)
    - [Tls](#tls)
2. [Requirements Installation](#requirements-installation)
    - [Opensearch](#opensearch-v2150)
    - [Rabbitmq](#rabbitmq-v3127)
    - [Postresql](#postgresql-v14x)
    - [Keycloak](#keycloack-v2307)
3. [Openk9 Base core components installation](#base-core-components-installation)
    - [Ingestion](#ingestion)
    - [Tenant Manager](#tenant-manager)
    - [Datasource](#datasource)
    - [Searcher](#searcher)
    - [K8s Client](#k8s-client)
3. [Openk9 Uis installation](#openk9-uis-installation)
    - [Tenant Ui](#tenant-ui)
    - [Admin Ui](#admin-ui)
    - [Search Frontend](#search-frontend)
3. [Openk9 Gen Ai components installation](#gen-ai-components-installation)
    - [Embedding Module](#tenant-ui)
    - [Rag Module](#rag-module)
    - [Talk To](#talk-to)
4. [Openk9 File Handling components installation](#file-handling-components)
    - [Minio](#minio)
    - [File Manager](#file-manager)
    - [Tika](#tika)


## Prerequisites

To install Openk9 following this installation guide, you need to have locally installed tools.

For Kubernetes/k3s:

- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [helm](https://helm.sh/docs/intro/install/)

For Openshift:

- [oc](https://docs.openshift.com/container-platform/4.8/cli_reference/openshift_cli/getting-started-cli.html)
- [helm](https://helm.sh/docs/intro/install/)

### Namespace

A namespace (Kubernetes) or project (Openshift) must be created in which to install the product.

To create a namespace for kubernetes execute:

```bash
kubectl create namespace openk9
```

To create a project for OpenShift execute:

```bash
oc create project openk9
```

Warning, the use of a namespace other than `default` requires that in each `oc` or `helm` command there is an indication of the namespace/project to use: `-n openk9`. To overcome this *problem* you can make "openk9" the default namespace/project.

To set current namespace for kubernetes execute:

```bash
kubectl config set-context --current --namespace=openk9
```

To set current namespace for openshift execute:

```bash
oc project openk9
```

### Domain

Once Openk9 is installed, you need a valid domain to run it and create new virtual tenant. 
If you don't have a valid domain you can configure your local dns or map cluster IP in local hosts file.

### Tls

Openk9 to work needs use of tls communication. Once you are in possession of a valid certificate for your domain, add it to a secret named **openk9-tls-star-secret**. This is default name used in values files in charts. If you change this name, you need to update tls secret name in all values files.

To add a certificate to secret follow official kubernetes documentation [here](https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets).

If you don't have valid certifacate, use [certmanager](https://cert-manager.io/v1.1-docs/installation/kubernetes/) to create your self managed certificate.


## Requirements Installation

OpenK9 uses established products for some aspects/functionalities. These products must be present in Kubernetes/OpenShift cluster before installing OpenK9. 

You can also use external installation outside Kubernetes/Openshift cluster if you prefer. The only requirement is that these services are reachable from pods within the cluster. If you use external services, it is important to configure credentials to access to them in the appropriate secrets.

Currently installing through [Helm Charts](https://helm.sh/docs/topics/charts/) is the best choice.

Inside this repository there is the
[00-base-requirements](./00-base-requirements) folder where, for each product, there are configuration files for different installation scenarios.

So clone this repository before start to install.

Every helm command assume you are located in root of this repository.

### Opensearch v2.15.0

[Opensearch](https://opensearch.org/) is a fondamental element in Openk9. It is used as search engine and vector database, to enable core functionalities of Openk9.
For its installation the official [Helm Charts](https://github.com/opensearch-project/helm-charts) in version 2.20.0 is used.

To proceed add the repository containing the charts to your local helm:

```bash
helm repo add opensearch https://opensearch-project.github.io/helm-charts/
```

Create a secret with credentials for Opensearch:

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic opensearch-password --from-literal=OPENSEARCH_INITIAL_ADMIN_PASSWORD=20dfFHt45yu
```

For OpenShift execute:

```bash
oc -n openk9 create secret generic opensearch-password --from-literal=OPENSEARCH_INITIAL_ADMIN_PASSWORD=20dfFHt45yu
```

Install Opensearch

For Kubernetes/OpenShift execute:

```bash
helm install opensearch opensearch/opensearch --version 2.20.0 -n openk9 -f 00-base-requirements/01-opensearch/local-runtime.yaml
```

To customize Opensearch installation follow [chart documentation](https://github.com/opensearch-project/helm-charts/tree/opensearch-2.20.0/charts/opensearch)

#### Verify installation

To verify correct installation port forward 9200 on your host.

For Kubernetes execute:

```bash
kubectl port-forward -n openk9 svc/opensearch-cluster-master 9200
```

For Openshift execute:

```bash
oc port-forward -n openk9 svc/opensearch-cluster-master 9200
```

Open browser on [http://localhost:9200](http://localhost:9200). If you get following response service is ok.

```
{
  "name" : "opensearch-cluster-master-0",
  "cluster_name" : "opensearch-cluster",
  "cluster_uuid" : "6LNJ8Ts7Q2KVKh6JjU6gvA",
  "version" : {
    "distribution" : "opensearch",
    "number" : "2.14.0",
    "build_type" : "tar",
    "build_hash" : "aaa555453f4713d652b52436874e11ba258d8f03",
    "build_date" : "2024-05-09T18:51:00.973564994Z",
    "build_snapshot" : false,
    "lucene_version" : "9.10.0",
    "minimum_wire_compatibility_version" : "7.10.0",
    "minimum_index_compatibility_version" : "7.0.0"
  },
  "tagline" : "The OpenSearch Project: https://opensearch.org/"
}
```


### RabbitMQ v3.12.7

[RabbitMQ](https://www.rabbitmq.com/) is a fundamental element of OpenK9 as it allows asynchronous dialogue between the different components of the solution.

To install RabbitMQ we use the Helm Chart created by [Bitnami](https://github.com/bitnami/charts/tree/master/bitnami/rabbitmq).

To proceed add the repository containing the charts to your local helm:

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```

Create a secret with credentials for Rabbitmq:

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic rabbitmq-password --from-literal=rabbitmq-password=openk9
```

Per OpenShift execute:

```bash
oc -n openk9 create secret generic rabbitmq-password --from-literal=rabbitmq-password=openk9
```

Install RabbitMQ

For Kubernetes/OpenShift execute:

```yaml
helm install rabbitmq bitnami/rabbitmq \
  -n openk9 \
  --version 12.3.0 \
  -f 00-base-requirements/02-rabbitmq/local-runtime.yaml
```

To customize Rabbitmq installation follow [chart documentation](https://github.com/bitnami/charts/tree/main/bitnami/rabbitmq)

#### Verify installation

As suggested by the installation notes, expose the Management interface on the host PC.

For Kubernetes execute:

```bash
kubectl port-forward -n openk9 svc/rabbitmq 15672:15672
```

For Openshift execute:

```bash
oc port-forward -n openk9 svc/rabbitmq 15672
```

open browser on [http://localhost:15672](http://localhost:15672) and log in with the credentials entered in the previously created secret.


### PostgreSQL v14.x

Several elements of OpenK9 require the presence of a relational database. [PostgreSQL](https://www.postgresql.org/) represents the best open source solution.

To install PostgreSQL use the Helm Chart created by [Bitnami](https://github.com/bitnami/charts/tree/master/bitnami/postgres) which manages the different aspects of a stand-alone installation.

To proceed add the repository containing the charts to your local helm:

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```

Create a secret with credentials for PostgreSQL:

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic postgres-password --from-literal=postgres-password=system --from-literal=user-password=openk9 --from-literal=replication-password=password
```

For OpenShift execute:

```bash
oc -n openk9 create secret generic postgres-password --from-literal=postgres-password=system --from-literal=user-password=openk9 --from-literal=replication-password=password
```

Install PostgreSQL

For Kubernetes/OpenShift execute:

```bash
helm install postgresql bitnami/postgresql \
  -n openk9 \
  --version 13.2.21 \
  -f 00-base-requirements/03-postgresql/local-runtime.yaml
```

#### Verify installation

As suggested by the installation notes I can use `kubectl` and `oc` to activate a temporary pod with the psql command.

For Kubernetes execute: 

```bash
export POSTGRES_PASSWORD=$(kubectl get secret -n openk9 postgres-password -o jsonpath="{.data.postgres-password}" | base64 --decode)
```

```bash
kubectl run postgresql-client --rm --tty -i --restart='Never' \
   -n openk9 \
   --image docker.io/bitnami/postgresql:14 \
   --env="PGPASSWORD=$POSTGRES_PASSWORD" \
   --command -- psql --host postgresql -U postgres -d postgres -p 5432
```

For Openshift execute: 

```bash
export POSTGRES_PASSWORD=$(oc get secret -n openk9 postgres-password -o jsonpath="{.data.postgres-password}" | base64 --decode)
```

```bash
oc run postgresql-client --rm --tty -i --restart='Never' \
   -n openk9 \
   --image docker.io/bitnami/postgresql:14 \
   --env="PGPASSWORD=$POSTGRES_PASSWORD" \
   --command -- psql --host postgresql -U postgres -d postgres -p 5432
```

When the message `If you don't see a command prompt, try pressing enter.` appears, you need to enter the password for the user `postgres` and then press enter.

From `psql` I can use the `\l` command to see the databases and templates present.

```
If you don't see a command prompt, try pressing enter.

psql (13.5)
Type "help" for help.

openk9=> \l
                             List of databases
   Name    |  Owner   | Encoding | Collate | Ctype |   Access privileges   
-----------+----------+----------+---------+-------+-----------------------
 postgres  | postgres | UTF8     | C       | C     | 
 template0 | postgres | UTF8     | C       | C     | =c/postgres          +
           |          |          |         |       | postgres=CTc/postgres
 template1 | postgres | UTF8     | C       | C     | =c/postgres          +
           |          |          |         |       | postgres=CTc/postgres
(3 rows)


openk9=> \q
pod "postgresql-client" deleted
```


### Keycloack v25.0.6

[Keycloak](https://www.keycloak.org/) is used by OpenK9 to manage and delegate user authentication logic. As well as some aspects of the authorization logic.

#### Database PostgreSQL

Keycloak requires the presence of a relational database, preferably PostgreSQL. 
Use a `Job` to create the necessary database on the PostgreSQL. The database will be managed by a specific user.

Apply file `00-base-requirements/06-keycloak/extras/postgresql-keycloak.yaml` to run the Job.

For Kubernetes execute:

```bash
kubectl -n openk9 apply -f 00-base-requirements/06-keycloak/extras/postgresql-keycloak.yaml
```

Per Openshift execute:

```bash
oc -n openk9 apply -f 00-base-requirements/06-keycloak/extras/postgresql-keycloak.yaml
```

Check status of Job:

For Kubernetes execute:

```bash
kubectl describe jobs/keycloak-db -n openk9
```

Per Openshift execute:

```bash
oc describe jobs/keycloak-db -n openk9
```

If you get this response job is completed in proper way:

```bash
Events:
  Type    Reason            Age   From            Message
  ----    ------            ----  ----            -------
  Normal  SuccessfulCreate  98s   job-controller  Created pod: keycloak-db-9698d
  Normal  Completed         82s   job-controller  Job completed
```

Then create a secret with credentials for the just created user to access to database.

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic postgresql-keycloak-secret \
  --from-literal=database=keycloak \
  --from-literal=username=keycloak \
  --from-literal=password=openk9
```

Per Openshift execute:

```bash
oc -n openk9 create secret generic postgresql-keycloak-secret \
  --from-literal=database=keycloak \
  --from-literal=username=keycloak \
  --from-literal=password=openk9
```

For installation within Kubernets/OpenShift we will use the [Helm Charts](https://github.com/bitnami/charts/tree/main/bitnami/keycloak) provided by Bitnami.

To proceed add the repository containing the charts to your local helm:

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```

Create a secrets with credentials for Keycloak admin user.

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic keycloak-secret \
  --from-literal=admin-password=openk9
```

Install Keycloak

For Kubernetes/OpenShift execute:

```bash
helm install keycloak --version 23.0.0 bitnami/keycloak -n openk9 -f 00-base-requirements/06-keycloak/local-runtime.yaml
```


### Verify installation

Expose the Management interface on the host PC.

Per Kubernetes eseguire:

```bash
kubectl -n openk9 port-forward svc/keycloak 8280:80
```

Per Openshift eseguire:

```bash
kubectl -n openk9 port-forward svc/keycloak 8280:80
```

Access to console using url [http://localhost:8280](http://localhost:8280) and login with user **user** and with password inserted in secret previously created.


#### Expose Keycloak using Ingress

Keycloak must be exposed through Ingress in order to be externally reachable for the login flow.

Use following command to create ingress from terminal. Update host in spec.rules and in tls.hosts to match your domain and sustitute to `keycloak.openk9.local`.

Change also tls.secretName if yoy have renamed tls secret.

```bash
cat <<_EOF_ | kubectl apply -n openk9 -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: keycloak
  annotations:
    nginx.ingress.kubernetes.io/proxy-buffer-size: 8k
spec:
  rules:
    - host: "keycloak.openk9.local"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name:  keycloak-headless
                port:
                  number: 80
  tls:
    - hosts:
        - "keycloak.openk9.local"
      secretName: openk9-tls-star-secret
_EOF_
```

The annotation `nginx.ingress.kubernetes.io/proxy-buffer-size: 8k` is needed to handle redirects to Keycloak with big headers.



## OPENK9 COMPONENTS

To install Openk9 components you can use official Openk9 helm chart, present in this repository.

Helm charts are available also on remote Openk9 helm chart repository.

To add in your local environment Openk9 helm repo run:

```bash
helm repo add openk9 https://registry.smc.it/repository/helm-private/
```

Inside the [openk9-helm-charts repository](https://github.com/smclab/openk9-helm-charts) there is the
[01-base-core](./01-base-core) folder where, for each component, there is official chart.

Inside every chart folder, there is a README file with chart documentation. Explore it for advanced configuration.

For every charts, there is also a scenarios folder, with files to install components in different platforms (Kubernetes/Openshift).

## BASE CORE COMPONENTS INSTALLATION

Base core components are mandatory components to install and run Openk9 in its core functionalities.

### INGESTION

The Ingestion component exposes the Rest API through which Openk9 accepts the data arriving from the different external data sources connected.

To learn more on Ingestion component, read [official documentation](https://www.openk9.io/docs/ingestion). 

#### Main configurations

Edit your local yaml file to overwrite main configurations and configure Ingestion to run correctly in your cluster.

Following are main configurations to edit:

```bash
## Quarkus configuration
quarkus:
  HttpCors: true ## Change to disable Cors
  HttpCorsOrigin: "/https://.*.openk9.local/," ## Change to configure Cors for your domain
```

For advanced configurations read [README.md](./01-base-core/openk9-ingestion/README.md) inside Ingestion chart folder.

#### Installation

For Kubernetes execute:

```bash
helm upgrade -i ingestion 01-base-core/openk9-ingestion -n openk9 -f 01-base-core/openk9-ingestion/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i ingestion 01-base-core/openk9-ingestion -n openk9 -f 01-base-core/openk9-ingestion/scenarios/local-crc.yaml
```

#### Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-ingestion 8080:8080
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-ingestion 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health). If status is UP service is OK.

### TENANT MANAGER

The Tenant Manager component defines the logic for managing and creating tenants.

To learn more on Tenant Manager component, read [official documentation](https://www.openk9.io/docs/tenant-manager).

Tenant Manager interacts with Keycloak and needs a specific configured realm and client. This client is then used to log in and call tenant manager Apis using specific [dedicated UI](#tenant-ui).

Create it before install. Follow [official documentation](https://staging-site.openk9.io/docs/first-configuration#tenant-manager-keycloak-configuration).

Check [official documentation]() on how configure realm and client on Keycloak.

#### Main configurations

Edit your local yaml file to overwrite main configurations and configure Tenant Manager to run correctly in your cluster.

Following are main configurations to edit:

```bash
## Keycloak configuration
keycloak:
  host: "keycloak.openk9.local"   ## Change to configure correctly keycloak host used to set Ingress

## Quarkus configuration
quarkus:
  HttpCors: true ## Change to disable Cors
  HttpCorsOrigin: "/https://.*.openk9.local/," ## Change to configure Cors for your domain
  kubernetes:
    namespace: "openk9" ## Change to insert correct namespace name

## Ingress configuration
ingress:
  enabled: false ## Change to enable ingress
  host: tenant-manager.openk9.local   ## Change with your domain
  paths:
    - /api/tenant-manager
  tls:
    enabled: false ## Change to enable tls
    secretName: openk9-tls-star-secret   ## Change if secret name is not the default

```

For advanced configurations read [README.md](./01-base-core/openk9-tenant-manager/README.md) inside Tenant Manager chart folder.

#### Installation

The Tenant Manager requires the presence of a database for historicizing the configurations relating to the tenants created. 
Use a `Job` to create the necessary database on PostgreSQL for this component. The database will be managed by a dedicated user.

The Job definition is present in the file `01-base-core/openk9-tenant-manager/extras/postgresql-tenant-manager.yaml`.

Per Kubernetes execute:

```bash
kubectl -n openk9 apply -f 01-base-core/openk9-tenant-manager/extras/postgresql-tenant-manager.yaml
```

Per openshift execute:

```bash
oc -n openk9 apply -f 01-base-core/openk9-tenant-manager/extras/postgresql-tenant-manager.yaml
```

Check status of Job:

For Kubernetes execute:

```bash
kubectl describe jobs/tenant-manager-db -n openk9
```

Per Openshift execute:

```bash
oc describe jobs/tenant-manager-db -n openk9
```

If you get this response job is completed in proper way:

```bash
Events:
  Type    Reason            Age   From            Message
  ----    ------            ----  ----            -------
  Normal  SuccessfulCreate  98s   job-controller  Created pod: tenant-manager-db-9698d
  Normal  Completed         82s   job-controller  Job completed
```

Then create a Secret with the coordinates of the newly created user.

Per Kubernetes execute:

```bash
kubectl -n openk9 create secret generic postgresql-tenant-manager-secret \
  --from-literal=database=tenantmanager \
  --from-literal=username=openk9 \
  --from-literal=password=openk9
```

Per openshift execute:

```bash
oc -n openk9 create secret generic postgresql-tenant-manager-secret \
  --from-literal=database=tenantmanager \
  --from-literal=username=openk9 \
  --from-literal=password=openk9
```

Now you can install the Tenant Manager.

For Kubernetes execute:

```bash
helm upgrade -i tenant-manager 01-base-core/openk9-tenant-manager -n openk9 -f 01-base-core/openk9-tenant-manager/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i tenant-manager 01-base-core/openk9-tenant-manager -n openk9 -f 01-base-core/openk9-tenant-manager/scenarios/local-crc.yaml
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-tenant-manager 8080:8080
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-tenant-manager 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health). If status is UP service is OK.

### DATASOURCE

The Datasource component represents the manager of the various indexed data sources.

To learn more on Datasource component, read [official documentation](https://www.openk9.io/docs/datasource).

#### Main configurations

Edit your local yaml file to overwrite main configurations and configure Datasource to run correctly in your cluster.

Following are main configurations to edit:

```bash
## Quarkus configuration
quarkus:
  HttpCors: true ## Change to disable Cors
  HttpCorsOrigin: "/https://.*.openk9.local/," ## Change to configure Cors for your domain

## Keycloak configuration
keycloak:
  host: "keycloak.openk9.local"  ## Change to configure correctly keycloak host used to set Ingress
```

For advanced configurations read [README.md](./01-base-core/openk9-datasource/README.md) inside Datasource chart folder.

#### Installation

The Datasource requires the presence of a database for historicizing the product configuration. 
Use a `Job` to create the necessary database on PostgreSQL for this component. The database will be managed by a dedicated user.

The Job definition is present in the file `01-base-core/openk9-datasource/extras/postgresql-openk9.yaml`.

For kubernetes execute:

```bash
kubectl -n openk9 apply -f 01-base-core/openk9-datasource/extras/postgresql-openk9.yaml
```

For Openshift execute:

```bash
oc -n openk9 apply -f 01-base-core/openk9-datasource/extras/postgresql-openk9.yaml
```

Check status of Job:

For Kubernetes execute:

```bash
kubectl describe jobs/openk9-db -n openk9
```

Per Openshift execute:

```bash
oc describe jobs/openk9-db -n openk9
```

If you get this response job is completed in proper way:

```bash
Events:
  Type    Reason            Age   From            Message
  ----    ------            ----  ----            -------
  Normal  SuccessfulCreate  98s   job-controller  Created pod: openk9-db-9698d
  Normal  Completed         82s   job-controller  Job completed
```

Now you can install the Datasource.


For kubernetes execute:

```bash
helm upgrade -i datasource 01-base-core/openk9-datasource -n openk9 -f 01-base-core/openk9-datasource/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i datasource 01-base-core/openk9-datasource -n openk9 -f 01-base-core/openk9-datasource/scenarios/local-crc.yaml
```

#### Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-datasource 8080:8080
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-datasource 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health) If status is UP service is OK.

### SEARCHER

The Searcher component exposes and implements Search Api use to search and filter indexed data.

To learn more on Searcher component, read [official documentation](https://www.openk9.io/docs/searcher).

#### Main configurations

Edit your local yaml file to overwrite main configurations and configure searcher to run correctly in your cluster.

Following are main configurations to edit:

```bash
## Keycloak configuration
keycloak:
  host: "keycloak.openk9.local"   ## Change to configure correctly keycloak host used to set Ingress

## Quarkus configuration
quarkus:
  HttpCors: true
  HttpCorsOrigin: "/https://.*.openk9.local/," ## Change to configure Cors for your domain
```

For advanced configurations read [README.md](./01-base-core/openk9-searcher/README.md) inside Searcher chart folder.

#### Installation


For Kubernetes execute:

```bash
helm upgrade -i searcher 01-base-core/openk9-searcher -n openk9 -f 01-base-core/openk9-searcher/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i searcher 01-base-core/openk9-searcher -n openk9 -f 01-base-core/openk9-searcher/scenarios/local-crc.yaml
```

#### Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-searcher 8080:8080
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-searcher 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health). If status is UP service is OK.

### K8S CLIENT

The K8s Client component exposes and implements Search Api use to search and filter indexed data.

To learn more on K8s Client component, read [official documentation](https://www.openk9.io/docs/searcher).

#### Main configurations

Edit your local yaml file to overwrite main configurations and configure k8s client to run correctly in your cluster.

Following are main configurations to edit:

```bash
## Keycloak configuration
keycloak:
  host: "keycloak.openk9.local"   ## Change to configure correctly 

## Quarkus configuration
quarkus:
  HttpCors: true
  HttpCorsOrigin: "/https://.*.openk9.local/," ## Change to configure Cors for your domain
  kubernetes:
    namespace: "openk9" ## Change to insert correct namespace name
    secretName: openk9-tls-star-secret

## K8s Namespace
k8s:
  namespace: "openk9"
```

For advanced configurations read [README.md](./06-utilities/openk9-k8s-client/README.md) inside K8s Client chart folder.

#### Installation

For kubernetes execute:

```bash
helm upgrade -i k8s-client 06-utilities/openk9-k8s-client -n openk9 -f 06-utilities/openk9-k8s-client/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i k8s-client 06-utilities/openk9-k8s-client -n openk9 -f 06-utilities/openk9-k8s-client/scenarios/local-crc.yaml
```

#### Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-k8s-client 8080:8080
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-k8s-client 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health). If status is UP service is OK.


## Openk9 UIs Installation

### Tenant Ui

The Tenant UI is used to configure and manage different tenants.

#### Main configurations

Edit your local yaml file to overwrite main configurations and configure Tenant UI to run correctly in your cluster.

Following are main configurations to edit:

```bash
## Ingress configuration
ingress:
  enabled: false ## Change to enable ingress
  host: tenant-manager.openk9.local   ## Change with your domain ## Must be the same host configure for ingress in Tenant Manager
  paths:
    - /admin
  tls:
    enabled: false ## Change to enable tls
    secretName: openk9-tls-star-secret   ## Change if secret name is not the default
```

For advanced configurations read [README.md](./01-base-core/openk9-tenant-ui/README.md) inside Tenant UI chart folder.

#### Installation

For Kubernetes execute:

```bash
helm upgrade -i tenant-ui 01-base-core/openk9-tenant-ui -n openk9 -f 01-base-core/openk9-tenant-ui/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i tenant-ui 01-base-core/openk9-tenant-ui -n openk9 -f 01-base-core/openk9-tenant-ui/scenarios/local-crc.yaml
```

#### Verify installation

Learn more on how to use and configure all needed to access to Tenant UI on [official documentation](https://staging-site.openk9.io/docs/first-configuration).

### Admin Ui

The Admin UI is used to configure and manage configurations of single tenant.

For advanced configurations read [README.md](./01-base-core/openk9-admin-ui/README.md) inside Admin UI chart folder.

#### Installation

For kubernetes execute:

```bash
helm upgrade -i admin-ui 01-base-core/openk9-admin-ui -n openk9 -f 01-base-core/openk9-admin-ui/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i admin-ui 01-base-core/openk9-admin-ui -n openk9 -f 01-base-core/openk9-admin-ui/scenarios/local-crc.yaml
```

#### Verify installation

Learn more on how to use and configure all needed to access to Admin UI on [official documentation](https://staging-site.openk9.io/docs/first-configuration#configure-and-start-your-first-datasource).

### Search Frontend

The Search Frontend is used to access to standard search interface of Openk9.

For advanced configurations read [README.md](./01-base-core/openk9-search-frontend/README.md) inside Search Frontend chart folder.

#### Installation

For kubernetes execute:

```bash
helm upgrade -i search-frontend 01-base-core/openk9-search-frontend -n openk9 -f 01-base-core/openk9-search-frontend/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i search-frontend 01-base-core/openk9-search-frontend -n openk9 -f 01-base-core/openk9-search-frontend/scenarios/local-crc.yaml
```

#### Verify installation

Learn more on how to use and access to standard Search Frontend on [official documentation](https://staging-site.openk9.io/docs/standalone-app).

## GEN AI COMPONENTS INSTALLATION

Gen AI components add to Openk9 functionalities to chat with your data using Large Language Models.

### Embedding Module

Embedding Module is module that perform chunking and embedding operations in case you need to enable data vectorization for Retrieval Augumented Generation experience.

To learn more on Embedding Module, read [official documentation]().

For advanced configurations read [README.md](./08-gen-ai/openk9-embedding-module/README.md) inside Embedding Module chart folder.

## Installation


For kubernetes/K3s execute:

```bash
helm upgrade -i embedding-module 08-gen-ai/openk9-embedding-module -n openk9 -f 08-gen-ai/openk9-embedding-module/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i embedding-module 08-gen-ai/openk9-embedding-module -n openk9 -f 08-gen-ai/openk9-embedding-module/scenarios/local-crc.yaml
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes/K3s execute:

```bash
kubectl -n openk9 port-forward svc/openk9-embedding-module 5000:5000
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-rag-module 5000:5000
```

Access to Admin Ui using url [http://localhost:5000/docs](http://localhost:5000/docs). If page is reachable is ok.


### Rag Module

Rag Module is module that exposes Apis to chat with your data using Generative Apis.

To learn more on Rag Module, read [official documentation]().

For advanced configurations read [README.md](./08-gen-ai/openk9-embedding-module/README.md) inside Embedding Module chart folder.

## Installation


For kubernetes/K3s execute:

```bash
helm upgrade -i rag-module 08-gen-ai/openk9-rag-module -n openk9 -f 08-gen-ai/openk9-rag-module/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i rag-module 08-gen-ai/openk9-rag-module -n openk9 -f 08-gen-ai/openk9-rag-module/scenarios/local-crc.yaml
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes/K3s execute:

```bash
kubectl -n openk9 port-forward svc/openk9-rag-module 5000:5000
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-rag-module  5000:5000
```

Go to url [http://localhost:5000/docs](http://localhost:5000/docs). If page is reachable is ok.


### Talk To

Talk To is used to access to Generative search interface of Openk9.

To learn more on how to use Talk To, read [official documentation]().

For advanced configurations read [README.md](./08-gen-ai/openk9-talk-to/README.md) inside Talk To chart folder.

## Installation


For kubernetes/K3s execute:

```bash
helm upgrade -i talk-to 08-gen-ai/openk9-talk-to -n openk9 -f 08-gen-ai/openk9-talk-to/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i talk-to 08-gen-ai/openk9-talk-to -n openk9 -f 08-gen-ai/openk9-talk-to/scenarios/local-crc.yaml
```

## Verify installation

Learn more on how to use and configure all needed to access and chat with your data using Talk To UI on [official documentation]().


## FILE HANDLING COMPONENTS

File handling components add to Openk9 functionalities to handle and parse binaries coming from external data source.

### Minio

[MinIO](https://min.io/) is used as S3 storage to support data processing.

To install Minio we use the Helm Chart created by [Bitnami](https://github.com/bitnami/charts/tree/main/bitnami/minio).

To proceed add the repository containing the charts to helm

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
```

Create a secret with credentials for Minio:

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic minio-secret --from-literal=root-user=minio --from-literal=root-password=minio123
```

Per OpenShift execute:

```bash
oc -n openk9 create secret generic minio-secret --from-literal=root-user=minio --from-literal=root-password=minio123
```

Install Minio

For kubernetes/OpenShift execute:

```yaml
helm install minio bitnami/minio \
  -n openk9 \
  --version 12.10.0 \
  -f 02-file-handling/08-minio/local-runtime.yaml
```

To customize Minio installation follow [chart documentation](https://github.com/bitnami/charts/tree/main/bitnami/minio)

#### Verify installation

As suggested by the installation notes, expose the Management interface on the host PC.

For Kubernetes execute:

```bash
kubectl port-forward -n openk9 svc/minio 9001:9001
```

For Openshift execute:

```bash
oc port-forward -n openk9 svc/minio 9001
```

Open browser on [http://localhost:9001](http://localhost:9001) and log in with the credentials entered in the previously created secret.


## FILE MANAGER

File Manager is the component delegated to upload and dowload binaries to Minio. It is used to save data when ingested and to download when they need to be processed.

To learn more on File Manager component, read [official documentation](https://www.openk9.io/docs/file-manager).

## Main configurations

Edit your local yaml file to overwrite main configurations and configure File Manager to run correctly in your cluster.

Following are main configurations to edit:

```bash
## Quarkus configuration
quarkus:
  HttpCors: true
  HttpCorsOrigin: "/https://.*.openk9.local/," ## Change to configure Cors for your domain
```

For advanced configurations read [README.md](./02-file-handling/openk9-file-manager/README.md) inside File Manager chart folder.

Now you can install the File Manager.

For Kubernetes execute:

```bash
helm upgrade -i file-manager 02-file-handling/openk9-file-manager -n openk9 -f 02-file-handling/openk9-file-manager/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i file-manager 02-file-handling/openk9-file-manager -n openk9 -f 02-file-handling/openk9-file-manager/scenarios/local-crc.yaml
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-file-manager 8080:8080
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-file-manager 8080:8080
```

Access to console using url "http://localhost:8080/q/health". If status is UP service is OK.

## TIKA

Tika is the component delegated to parse binaries coming from external data sources. It is developed wrapping [https://tika.apache.org/](https://tika.apache.org/).

To learn more on Tika component, read [official documentation](https://www.openk9.io/docs/tika).

## Main configurations

Edit your local yaml file to overwrite main configurations and configure Tika to run correctly in your cluster.

Following are main configurations to edit:

```bash
## Quarkus configuration
quarkus:
  HttpCors: true
  HttpCorsOrigin: "/https://.*.openk9.local/," ## Change to configure Cors for your domain
```

For advanced configurations read [README.md](./02-file-handling/openk9-tika/README.md) inside Tika chart folder.

Now you can install the Tika.

For Kubernetes execute:

```bash
helm upgrade -i tika 02-file-handling/openk9-tika -n openk9 -f 02-file-handling/openk9-tika/scenarios/local-runtime.yaml
```

For Opeshift execute:

```bash
helm upgrade -i tika 02-file-handling/openk9-tika -n openk9 -f 02-file-handling/openk9-tika/scenarios/local-crc.yaml
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-tika 8080:8080
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-tika 8080:8080
```

Access to console using url "http://localhost:8080/q/health". If status is UP service is OK.


## CONNECTORS

Openk9 has available connectors you can install in your environment and use to extract data from your data source.

### OPENK9 WEB CONNECTOR

Openk9 Web Connector is a crawler that permits to extract data from web site using Sitemap or alternately browsing the URLs in depth.

To learn more on Openk9 Web Connector, read [official documentation]().

## Installation

For Kubernetes execute:

```bash
helm upgrade -i openk9-web-connector 04-connectors/openk9-web-connector -n openk9
```

For Opeshift execute:

```bash
helm upgrade -i openk9-web-connector 04-connectors/openk9-web-connector -n openk9
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-web-connector 5000:5000
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-web-connector  5000:5000
```

Go to url [http://localhost:5000/docs](http://localhost:5000/docs). If page is reachable is ok.

### OPENK9 LIFERAY CONNECTOR

Openk9 Liferay Connector is a connector that permits to extract data from Liferay portal.

To learn more on Openk9 Liferay Connector, read [official documentation]().

## Installation


For Kubernetes execute:

```bash
helm upgrade -i openk9-liferay-connector 04-connectors/openk9-liferay-connector -n openk9
```

For Opeshift execute:

```bash
helm upgrade -i openk9-liferay-connector 04-connectors/openk9-liferay-connector -n openk9
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-liferay-connector 5000:5000
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-liferay-connector  5000:5000
```

Go to url [http://localhost:5000/docs](http://localhost:5000/docs). If page is reachable is ok.

### OPENK9 EMAIL CONNECTOR

Openk9 Email Connector is a connector that permits to extract data from Imap email server.

To learn more on Openk9 Email Connector, read [official documentation]().

## Installation


For Kubernetes execute:

```bash
helm upgrade -i openk9-email-connector 04-connectors/openk9-email-connector -n openk9
```

For Opeshift execute:

```bash
helm upgrade -i openk9-email-connector 04-connectors/openk9-email-connector -n openk9
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-email-connector 5000:5000
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-email-connector  5000:5000
```

Go to url [http://localhost:5000/docs](http://localhost:5000/docs). If page is reachable is ok.

### OPENK9 GITLAB CONNECTOR

Openk9 Gitlab Connector is a connector that permits to extract data from Gitlab server.

To learn more on Openk9 Gitlab Connector, read [official documentation]().

## Installation


For Kubernetes execute:

```bash
helm upgrade -i openk9-gitlab-connector 04-connectors/openk9-gitlab-connector -n openk9
```

For Opeshift execute:

```bash
helm upgrade -i openk9-gitlab-connector 04-connectors/openk9-gitlab-connector -n openk9
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-gitlab-connector 5000:5000
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-gitlab-connector  5000:5000
```

Go to url [http://localhost:5000/docs](http://localhost:5000/docs). If page is reachable is ok.

### OPENK9 MINIO CONNECTOR

Openk9 Minio Connector is a connector that permits to extract data from Minio server.

To learn more on Openk9 Minio Connector, read [official documentation]().

## Installation


For Kubernetes execute:

```bash
helm upgrade -i openk9-minio-connector 04-connectors/openk9-minio-connector -n openk9
```

For Opeshift execute:

```bash
helm upgrade -i openk9-minio-connector 04-connectors/openk9-minio-connector -n openk9
```

## Verify installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

Per Kubernetes execute:

```minio
kubectl -n openk9 port-forward svc/openk9-web-connector 5000:5000
```

Per Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-minio-connector  5000:5000
```

Go to url [http://localhost:5000/docs](http://localhost:5000/docs). If page is reachable is ok.

## ENRICHERS

