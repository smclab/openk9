# OpenK9 Helm Charts 

This repository contains the Helm Charts and other elements that allow you to install OpenK9 **version 2026.1-SNAPSHOT**:

- in a standard [Kubernets](https://kubernetes.io/) installed in the Cloud or On-Premises
- in a standard [OpenShift](https://www.redhat.com/it/technologies/cloud-computing/openshift) installed in the Cloud or On-Premises

For documentation to install previous versions search and checkout for related tag in this repository.

Read more about compatibility matrix on Github.

## Index

1. [Prerequisites](#prerequisites)
    - [Namespace](#namespace)
    - [Domain](#domain)
    - [Tls](#tls)
    - [Utilities](#utilities)
      - [Adminer](#adminer)
      - [Netutils](#netutils)
2. [Requirements Installation](#requirements-installation)
    - [Opensearch](#opensearch-v2191)
    - [RabbitMQ](#rabbitmq-v410)
    - [PostgreSQL](#postgresql-v16x)
    - [Keycloak](#keycloak-v2614)
3. [Openk9 Base core components installation](#base-core-components-installation)
    - [Ingestion](#ingestion)
    - [Tenant Manager](#tenant-manager)
    - [Datasource](#datasource)
    - [Searcher](#searcher)
    - [K8s Client](#k8s-client)
4. [Openk9 Uis installation](#openk9-uis-installation)
    - [Tenant Ui](#tenant-ui)
    - [Admin Ui](#admin-ui)
    - [Search Frontend](#search-frontend)
5. [Openk9 Gen Ai components installation](#gen-ai-components-installation)
    - [Embedding Module](#embedding-module)
    - [Rag Module](#rag-module)
    - [Talk To](#talk-to)
    - [Chunk Evaluation Module](#chunk-evaluation-module)
6. [Openk9 File Handling components installation](#file-handling-components)
    - [Minio](#minio)
    - [File Manager](#file-manager)
    - [Tika](#tika)

    
## Prerequisites

To install Openk9 following this installation guide, you need to have locally installed tools.

For Kubernetes:

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


### Utilities

In this section you find advices about installing some utilities, helpful to test and debugging application during installation and configuration

#### Adminer

[Adminer](https://www.adminer.org/en/) is a lightweight DB client.

To install it on your Kubernetes/Openshift cluster run following command.

```bash
helm upgrade -i adminer 00-base-requirements/07-adminer -n openk9
```

#### Verify Installation

To verify correct installation port forward 8080 on your host.

For Kubernetes execute:

```bash
kubectl port-forward -n openk9 svc/adminer 8080
```

For Openshift execute:

```bash
oc port-forward -n openk9 svc/adminer 8080
```

Open browser on [http://localhost:8080](http://localhost:8080). You view adminer enter panel.

#### Netutils

Netutils is lightweight image with linux network utilities available.

To install it on your Kubernetes/Openshift cluster run following command.

```bash
helm upgrade -i netutils 00-base-requirements/10-netutils -n openk9
```

#### Verify Installation

To verify correct installation enter interactively in pod and test operation using for example ping command.

## Requirements Installation

OpenK9 uses established products for some aspects/functionalities. These products must be present in Kubernetes/OpenShift cluster before installing OpenK9. 

You can also use external installation outside Kubernetes/Openshift cluster if you prefer. The only requirement is that these services are reachable from pods within the cluster. If you use external services, it is important to configure credentials to access to them in the appropriate secrets.

Currently installing through [Helm Charts](https://helm.sh/docs/topics/charts/) is the best choice.

Inside this repository there is the
[00-base-requirements](./00-base-requirements) folder where, for each product, there are configuration files for different installation scenarios.

So clone this repository before start to install.

Every helm command assume you are located in root of this repository.

### Opensearch v2.19.1

[Opensearch](https://opensearch.org/) is a fondamental element in Openk9. It is used as search engine and vector database, to enable core functionalities of Openk9.

For its installation the official [Helm Charts](https://github.com/opensearch-project/helm-charts) in version 2.31.0 is used for Kubernetes.



For Openshift [Official Opensearch Chart]([text](https://opensearch-project.github.io/helm-charts/)) is used to handle better system actions.

To proceed add the repository containing the charts to your local helm:

For Kubernetes execute:

```bash
helm repo add opensearch https://opensearch-project.github.io/helm-charts/
```

For OpenShift execute:


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

For Kubernetes execute:

```bash
helm install opensearch opensearch/opensearch --version 2.32.0 -n openk9 -f 00-base-requirements/01-opensearch/local-runtime.yaml
```

For Openshift execute:

```bash
helm install opensearch opensearch/opensearch --version 2.32.0 -n openk9 -f 00-base-requirements/01-opensearch/local-crc.yaml
```

To customize Opensearch installation follow [official chart documentation](https://github.com/opensearch-project/helm-charts/tree/opensearch-2.20.0/charts/opensearch) for Kubernetes and Openshift.

#### Verify Installation

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


### RabbitMQ v4.1.4

[RabbitMQ](https://www.rabbitmq.com/) is a fundamental element of OpenK9 as it allows asynchronous dialogue between the different components of the solution.


Create a secret with credentials for Rabbitmq:

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic rabbitmq-password --from-literal=rabbitmq-password=openk9 --from-literal=erlang-cookie=$(openssl rand -base64 32)
```

For OpenShift execute:

```bash
oc -n openk9 create secret generic rabbitmq-password --from-literal=rabbitmq-password=openk9 --from-literal=erlang-cookie=$(openssl rand -base64 32)
```

Install RabbitMQ

For Kubernetes/OpenShift execute:

```yaml
helm install rabbitmq oci://registry-1.docker.io/cloudpirates/rabbitmq \
  -n openk9 \
  --version 0.7.7 \
  -f 00-base-requirements/02-rabbitmq/local-runtime.yaml
```

To customize Rabbitmq installation follow [chart documentation](https://github.com/CloudPirates-io/helm-charts/tree/main/charts/rabbitmq)

#### Verify Installation

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


### PostgreSQL v16.x

Several elements of OpenK9 require the presence of a relational database. [PostgreSQL](https://www.postgresql.org/) represents the best open source solution.

To install PostgreSQL use the Helm Chart created by [Cloud Pirates](https://github.com/CloudPirates-io/helm-charts/tree/main/charts/postgres) which manages the different aspects of a stand-alone installation.


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
helm install postgresql oci://registry-1.docker.io/cloudpirates/postgres \
  -n openk9 \
  --version 0.11.6 \
  -f 00-base-requirements/03-postgresql/local-runtime.yaml
```

#### Verify Installation

As suggested by the installation notes I can use `kubectl` and `oc` to activate a temporary pod with the psql command.

For Kubernetes execute: 

```bash
export POSTGRES_PASSWORD=$(kubectl get secret -n openk9 postgres-password -o jsonpath="{.data.postgres-password}" | base64 --decode)
```

```bash
kubectl run postgresql-client --rm --tty -i --restart='Never' \
   -n openk9 \
   --image docker.io/postgres:16.0 \
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
   --image docker.io/postgres:16.0 \
   --env="PGPASSWORD=$POSTGRES_PASSWORD" \
   --command -- psql --host postgresql -U postgres -d postgres -p 5432
```

When the message `If you don't see a command prompt, try pressing enter.` appears, you need to enter the password for the user `postgres` and then press enter.

From `psql` I can use the `\l` command 
```bash
oc describe jobs/keycloak-db -n openk9
```

If you get this response job is completeto see the databases and templates present.

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


### Keycloak v26.1.4

[Keycloak](https://www.keycloak.org/) is used by OpenK9 to manage and delegate user authentication logic. As well as some aspects of the authorization logic.

#### Database PostgreSQL

Keycloak requires the presence of a relational database, preferably PostgreSQL. 
Use a `Job` to create the necessary database on the PostgreSQL. The database will be managed by a specific user.

Apply file `00-base-requirements/06-keycloak/extras/postgresql-keycloak.yaml` to run the Job.

For Kubernetes execute:

```bash
kubectl -n openk9 apply -f 00-base-requirements/06-keycloak/extras/postgresql-keycloak.yaml
```

For Openshift execute:

```bash
oc -n openk9 apply -f 00-base-requirements/06-keycloak/extras/postgresql-keycloak.yaml
```

Check status of Job:

For Kubernetes execute:

```bash
kubectl describe jobs/keycloak-db -n openk9
```

For Openshift execute:

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

For Openshift execute:

```bash
oc -n openk9 create secret generic postgresql-keycloak-secret \
  --from-literal=database=keycloak \
  --from-literal=username=keycloak \
  --from-literal=password=openk9
```

For installation within Kubernets/OpenShift we will use the [Helm Charts](https://github.com/CloudPirates-io/helm-charts/tree/main/charts/keycloak) provided by Cloud Pirates.


Create a secrets with credentials for Keycloak admin user.

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic keycloak-secret \
  --from-literal=admin-password=openk9
```

Install Keycloak

For Kubernetes execute:

```bash
helm install keycloak oci://registry-1.docker.io/cloudpirates/keycloak \
  --namespace openk9 \
  --values 00-base-requirements/06-keycloak/local-runtime.yaml \
  --version 0.8.4

```

For Openshift execute:

```bash
helm install keycloak oci://registry-1.docker.io/cloudpirates/keycloak \
  --namespace openk9 \
  --values 00-base-requirements/06-keycloak/local-crc.yaml \
  --version 0.8.4
```


### Verify Installation

Expose the Management interface on the host PC.

For Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/keycloak 8280:80
```

For Openshift execute:

```bash
kubectl -n openk9 port-forward svc/keycloak 8280:80
```

Access to console using url [http://localhost:8280](http://localhost:8280) and login with user **admin** and with password inserted in secret previously created.


## Openk9 Components

To install Openk9 components you can use official Openk9 helm chart, present in this repository.

Helm charts are available also on remote Openk9 helm chart repository.

To add in your local environment Openk9 helm repo run:

```bash
helm repo add openk9 https://registry.smc.it/repository/helm-private/
```

## Base Core Components Installation

Base core components are mandatory components to install and run Openk9 in its core functionalities.

Inside the this repository there is
[01-base-core](./01-base-core) folder where, for each component, there is official chart.

Inside every chart folder, there is a README file with chart documentation. Explore it for advanced configuration.

For every charts, there is also a scenarios folder, with files to install components in different platforms (Kubernetes/Openshift).

### Api Gateway

The Api Gateway component is used to handle and protect traffic through openk9 services.

To learn more on Api Gateway component, read [official documentation](https://www.openk9.io/docs/api-gateway).

#### Main Configurations

Edit your local yaml file to overwrite main configurations and configure Tenant Manager to run correctly in your cluster.

Following are main configurations to edit:

```bash
TO-DO
```

For advanced configurations read [README.md](./01-base-core/openk9-api-gateway/README.md) inside Api Gateway chart folder.

#### Installation

The Api Gteway requires the presence of a database for historicizing the configurations relating to the tenants created and security infos. 
This is created by the job executed by the helm chart.

Now you can install the Api Gateway.

For Kubernetes execute:

```bash
helm upgrade -i api-gateway 01-base-core/openk9-api-gateway -n openk9 -f 01-base-core/openk9-api-gateway/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i api-gateway 01-base-core/openk9-api-gateway -n openk9 -f 01-base-core/openk9-api-gateway/scenarios/local-crc.yaml
```

Check status of Job about database creation:

For Kubernetes execute:

```bash
kubectl describe jobs/openk9-api-gateway-db -n openk9
```

For Openshift execute:

```bash
oc describe jobs/openk9-api-gateway-db -n openk9
```

If you get this response job is completed in proper way:

```bash
Events:
  Type    Reason            Age   From            Message
  ----    ------            ----  ----            -------
  Normal  SuccessfulCreate  38s   job-controller  Created pod: openk9-api-gateway-db-5mkn5
  Normal  Completed         34s   job-controller  Job completed
```

## Verify Installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-api-gateway 8080:8080
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-api-gateway 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health). If status is UP service is OK.

### Ingestion

The Ingestion component exposes the Rest API through which Openk9 accepts the data arriving from the different external data sources connected.

To learn more on Ingestion component, read [official documentation](https://www.openk9.io/docs/ingestion). 

#### Main Configurations

Edit your local yaml file to overwrite main configurations and configure Ingestion to run correctly in your cluster.

Following are main configurations to edit:

```bash
## Http limit max size
limit:
  max:
    size: 10240K # Change to accept message with body greater than default
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

#### Verify Installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-ingestion 8080:8080
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-ingestion 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health). If status is UP service is OK.

### Tenant Manager

The Tenant Manager component defines the logic for managing and creating tenants.

To learn more on Tenant Manager component, read [official documentation](https://www.openk9.io/docs/tenant-manager).

Tenant Manager interacts with Keycloak and needs a specific configured realm and client. This client is then used to log in and call tenant manager Apis using specific  dedicated UI.

Create it before install. Follow [official documentation](https://staging-site.openk9.io/docs/first-configuration#tenant-manager-keycloak-configuration).

Check [official documentation]() on how configure realm and client on Keycloak.

#### Main Configurations

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

For Kubernetes execute:

```bash
kubectl -n openk9 apply -f 01-base-core/openk9-tenant-manager/extras/postgresql-tenant-manager.yaml
```

For Openshift execute:

```bash
oc -n openk9 apply -f 01-base-core/openk9-tenant-manager/extras/postgresql-tenant-manager.yaml
```

Check status of Job:

For Kubernetes execute:

```bash
kubectl describe jobs/tenant-manager-db -n openk9
```

For Openshift execute:

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

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic postgresql-tenant-manager-secret \
  --from-literal=database=tenantmanager \
  --from-literal=username=openk9 \
  --from-literal=password=openk9
```

For Openshift execute:

```bash
oc -n openk9 create secret generic postgresql-tenant-manager-secret \
  --from-literal=database=tenantmanager \
  --from-literal=username=openk9 \
  --from-literal=password=openk9
```

Then create a Secret with the password for tenant manager admin user.

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic tenant-manager-admin-password \
  --from-literal=password=openk9
```

For Openshift execute:

```bash
oc -n openk9 create secret generic tenant-manager-admin-password-secret \
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

## Verify Installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-tenant-manager 8080:8080
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-tenant-manager 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health). If status is UP service is OK.

### Datasource

The Datasource component represents the manager of the various indexed data sources.

To learn more on Datasource component, read [official documentation](https://www.openk9.io/docs/datasource).

#### Main Configurations

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

For Kubernetes execute:

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

For Openshift execute:

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


For Kubernetes execute:

```bash
helm upgrade -i datasource 01-base-core/openk9-datasource -n openk9 -f 01-base-core/openk9-datasource/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i datasource 01-base-core/openk9-datasource -n openk9 -f 01-base-core/openk9-datasource/scenarios/local-crc.yaml
```

#### Verify Installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-datasource 8080:8080
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-datasource 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health) If status is UP service is OK.

### Searcher

The Searcher component exposes and implements Search Api use to search and filter indexed data.

To learn more on Searcher component, read [official documentation](https://www.openk9.io/docs/searcher).

#### Main Configurations

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

For Openshift execute:

```bash
helm upgrade -i searcher 01-base-core/openk9-searcher -n openk9 -f 01-base-core/openk9-searcher/scenarios/local-crc.yaml
```

#### Verify Installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-searcher 8080:8080
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-searcher 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health). If status is UP service is OK.

### K8s Client

The K8s Client component exposes and implements Search Api use to search and filter indexed data.

To learn more on K8s Client component, read [official documentation](https://www.openk9.io/docs/searcher).

#### Main Configurations

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

For Kubernetes execute:

```bash
helm upgrade -i k8s-client 06-utilities/openk9-k8s-client -n openk9 -f 06-utilities/openk9-k8s-client/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i k8s-client 06-utilities/openk9-k8s-client -n openk9 -f 06-utilities/openk9-k8s-client/scenarios/local-crc.yaml
```

#### Verify Installation
keycloak-tls
Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-k8s-client 8080:8080
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-k8s-client 8080:8080
```

Access to console using url [http://localhost:8080/q/health](http://localhost:8080/q/health). If status is UP service is OK.


## Openk9 UIs Installation

### Tenant Ui

The Tenant UI is used to configure and manage different tenants.

#### Main Configurations

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

For Openshift execute:

```bash
helm upgrade -i tenant-ui 01-base-core/openk9-tenant-ui -n openk9 -f 01-base-core/openk9-tenant-ui/scenarios/local-crc.yaml
```

#### Verify Installation

Learn more on how to use and configure all needed to access to Tenant UI on [official documentation](https://staging-site.openk9.io/docs/first-configuration#first-tenant-creation).

### Admin Ui

The Admin UI is used to configure and manage configurations of single tenant.

For advanced configurations read [README.md](./01-base-core/openk9-admin-ui/README.md) inside Admin UI chart folder.

#### Installation

For Kubernetes execute:

```bash
helm upgrade -i admin-ui 01-base-core/openk9-admin-ui -n openk9 -f 01-base-core/openk9-admin-ui/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i admin-ui 01-base-core/openk9-admin-ui -n openk9 -f 01-base-core/openk9-admin-ui/scenarios/local-crc.yaml
```

#### Verify Installation

Learn more on how to use and configure all needed to access to Admin UI on [official documentation](https://staging-site.openk9.io/docs/first-configuration#configure-and-start-your-first-datasource).

### Search Frontend

The Search Frontend is used to access to standard search interface of Openk9.

For advanced configurations read [README.md](./01-base-core/openk9-search-frontend/README.md) inside Search Frontend chart folder.

#### Installation

For Kubernetes execute:

```bash
helm upgrade -i search-frontend 01-base-core/openk9-search-frontend -n openk9 -f 01-base-core/openk9-search-frontend/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i search-frontend 01-base-core/openk9-search-frontend -n openk9 -f 01-base-core/openk9-search-frontend/scenarios/local-crc.yaml
```

#### Verify Installation

Learn more on how to use and access to standard Search Frontend on [official documentation](https://staging-site.openk9.io/docs/standalone-app).

## Gen AI Components Installation

Gen AI components add to Openk9 functionalities to chat with your data using Large Language Models.

Inside this repository there is the
[03-gen-ai](./03-gen-ai) folder where, for each component, there is official chart.

Inside every chart folder, there is a README file with chart documentation. Explore it for advanced configuration.

For every charts, there is also a scenarios folder, with files to install components in different platforms (Kubernetes/Openshift).

### Embedding Module

Embedding Module is module that perform chunking and embedding operations in case you need to enable data vectorization for Retrieval Augumented Generation experience.

To learn more on Embedding Module, read [official documentation]().

For advanced configurations read [README.md](./03-gen-ai/openk9-embedding-module/README.md) inside Embedding Module chart folder.

#### Installation


For kubernetes/K3s execute:

```bash
helm upgrade -i embedding-module 03-gen-ai/openk9-embedding-module -n openk9 -f 03-gen-ai/openk9-embedding-module/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i embedding-module 03-gen-ai/openk9-embedding-module -n openk9 -f 03-gen-ai/openk9-embedding-module/scenarios/local-crc.yaml
```

#### Verify Installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes/K3s execute:

```bash
kubectl -n openk9 port-forward svc/openk9-embedding-module 5000:5000
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-embedding-module 5000:5000
```

Access to Admin Ui using url [http://localhost:5000/docs](http://localhost:5000/docs). If page is reachable is ok.


### Rag Module

Rag Module is module that exposes Apis to chat with your data using Generative Apis.

To learn more on Rag Module, read [official documentation]().

For advanced configurations read [README.md](./03-gen-ai/openk9-rag-module/README.md) inside Rag Module chart folder.

#### Installation


For kubernetes/K3s execute:

```bash
helm upgrade -i rag-module 03-gen-ai/openk9-rag-module -n openk9 -f 03-gen-ai/openk9-rag-module/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i rag-module 03-gen-ai/openk9-rag-module -n openk9 -f 03-gen-ai/openk9-rag-module/scenarios/local-crc.yaml
```

#### Verify Installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes/K3s execute:

```bash
kubectl -n openk9 port-forward svc/openk9-rag-module 5000:5000
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-rag-module 5000:5000
```

Go to url [http://localhost:5000/docs](http://localhost:5000/docs). If page is reachable is ok.


### Talk To

Talk To is used to access to Generative search interface of Openk9.

To learn more on how to use Talk To, read [official documentation]().

For advanced configurations read [README.md](./03-gen-ai/openk9-talk-to/README.md) inside Talk To chart folder.

#### Installation


For kubernetes/K3s execute:

```bash
helm upgrade -i talk-to 03-gen-ai/openk9-talk-to -n openk9 -f 03-gen-ai/openk9-talk-to/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i talk-to 03-gen-ai/openk9-talk-to -n openk9 -f 03-gen-ai/openk9-talk-to/scenarios/local-crc.yaml
```

#### Verify Installation

Learn more on how to use and configure all needed to access and chat with your data using Talk To UI on [official documentation]().


### Chunk Evaluation Module

Chunk Evaluation Module is a module that evaluates and analyzes document chunks using various metrics. It communicates with RabbitMQ for message processing and integrates with Phoenix for observability.

To learn more on Chunk Evaluation Module, read [official documentation]().

For advanced configurations read [README.md](./03-gen-ai/openk9-chunk-evaluation-module/README.md) inside Chunk Evaluation Module chart folder.

#### Installation

For kubernetes/K3s execute:

```bash
helm upgrade -i chunk-evaluation-module 03-gen-ai/openk9-chunk-evaluation-module -n openk9 -f 03-gen-ai/openk9-chunk-evaluation-module/scenarios/local-runtime.yaml
```

For Openshift execute:

```bash
helm upgrade -i chunk-evaluation-module 03-gen-ai/openk9-chunk-evaluation-module -n openk9 -f 03-gen-ai/openk9-chunk-evaluation-module/scenarios/local-crc.yaml
```

#### Verify Installation

Check that the pod is running correctly.

For Kubernetes/K3s execute:

```bash
kubectl -n openk9 get pods -l app.kubernetes.io/name=openk9-chunk-evaluation-module
```

For Openshift execute:

```bash
oc -n openk9 get pods -l app.kubernetes.io/name=openk9-chunk-evaluation-module
```

If the pod status is `Running`, the module is correctly installed and operational.


## File Handling Components

File handling components add to Openk9 functionalities to handle and parse binaries coming from external data source.

Inside this repository there is the
[02-file-handling](./02-file-handling/) folder where, for each component, there is official chart.

Inside every chart folder, there is a README file with chart documentation. Explore it for advanced configuration.

For every charts, there is also a scenarios folder, with files to install components in different platforms (Kubernetes/Openshift).

### Minio

[MinIO](https://min.io/) is used as S3 storage to support data processing.

To install Minio we use the Helm Chart created by [Cloud Pirates](https://github.com/CloudPirates-io/helm-charts/tree/main/charts/minio).

Create a secret with credentials for Minio:

For Kubernetes execute:

```bash
kubectl -n openk9 create secret generic minio-secret \
  --from-literal=user=minio \
  --from-literal=password=minio123
```

Per OpenShift execute:

```bash
oc -n openk9 create secret generic minio-secret \
  --from-literal=user=minio \
  --from-literal=password=minio123
```

Install Minio

For kubernetes/OpenShift execute:

```yaml
helm install minio oci://registry-1.docker.io/cloudpirates/minio \
  -n openk9 \
  --version 0.6.1 \
  -f 00-base-requirements/08-minio/local-runtime.yaml
```

To customize Minio installation follow [chart documentation](https://github.com/bitnami/charts/tree/main/bitnami/minio)

#### Verify Installation

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


### File Manager

File Manager is the component delegated to upload and dowload binaries to Minio. It is used to save data when ingested and to download when they need to be processed.

To learn more on File Manager component, read [official documentation](https://www.openk9.io/docs/file-manager).

#### Main Configurations

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

For Openshift execute:

```bash
helm upgrade -i file-manager 02-file-handling/openk9-file-manager -n openk9 -f 02-file-handling/openk9-file-manager/scenarios/local-crc.yaml
```

#### Verify Installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-file-manager 8080:8080
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-file-manager 8080:8080
```

Access to console using url "http://localhost:8080/q/health". If status is UP service is OK.

### Tika

Tika is the component delegated to parse binaries coming from external data sources. It is developed wrapping [https://tika.apache.org/](https://tika.apache.org/).

To learn more on Tika component, read [official documentation](https://www.openk9.io/docs/tika).

#### Main Configurations

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

For Openshift execute:

```bash
helm upgrade -i tika 02-file-handling/openk9-tika -n openk9 -f 02-file-handling/openk9-tika/scenarios/local-crc.yaml
```

#### Verify Installation

Expose the http interface on the host PC and use health endpoint to verify status of component.

For Kubernetes execute:

```bash
kubectl -n openk9 port-forward svc/openk9-tika 8080:8080
```

For Openshift execute:

```bash
oc -n openk9 port-forward svc/openk9-tika 8080:8080
```

Access to console using url "http://localhost:8080/q/health". If status is UP service is OK.


## Connectors

Openk9 has available connectors you can install in your environment and use to extract data from your data source.

Explore Connectors section on [official site](https://staging-site.openk9.io/plugins/).

Some universal connectors are open and is possibile to install them from Tenant Ui.

Check [official document]() to view how to install and configure it to index data.