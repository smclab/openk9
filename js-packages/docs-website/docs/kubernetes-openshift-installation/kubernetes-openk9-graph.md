---
id: kubernetes-openk9-graph
title: Openk9 Knowledge handling Pack
---

In this section is described how to install Neo4j and Openk9 Entity Manager service.
Installation of these components allows to handle e manage ontologies and relations defined in 
your data and in your company.

To install components helm charts are used.


### Preparing the installation

OpenK9 is formed by multiples core components to install.
Currently installing through [Helm Charts](https://helm.sh/docs/topics/charts/) is the best choice.

Inside the [openk9-helm-charts repository](https://github.com/smclab/openk9-helm-charts) there is the
`kubernetes/ 01-core-charts` folder where, for each component, there are configuration files for the different installation scenarios.

So clone this repository before start to install.

## Neo4J v4.2.6

### Preparing the installation

[Neo4J](https://neo4j.com/) is used by OpenK9 to manage/describe the relationships between indexed items and construct a Knowledge graph for the solution.

To install Neo4J use the [Helm Chart](https://github.com/neo4j-contrib/neo4j-helm) made available by the community.

> For Neo4J version 4.3 and later, the official [Helm Charts](https://neo4j.com/labs/neo4j-helm/1.0.0/) are available.

Before proceeding with the installation of the chart it is necessary to refine some parameters present in the configuration file
[values.yaml](https://github.com/neo4j-contrib/neo4j-helm/blob/4.2.6-1/values.yaml) for local development scenario
[00-requirements/04-neo4j/local-runtime.yaml](https://github.com/smclab/openk9-helm-charts/blob/master/00-requirements/04-neo4j/local-runtime.yaml).
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
  [00-requirements/04-neo4j/local-runtime.yaml](https://github.com/smclab/openk9-helm-charts/blob/master/00-requirements/04-neo4j/local-runtime.yaml)

Then you can perform queries using Cypher Query Language.

#### Notes


As indicated in *External Exposure of Neo4j Clusters - Neo4j-Helm User Guide* it is possible to make Neo4J accessible
from outside Kubernetes. Check
[https://github.com/neo4j-contrib/neo4j-helm/tree/master/tools/external-exposure-legacy](https://github.com/neo4j-contrib/neo4j-helm/tree/master/tools/external-exposure-legacy)

## Entity Manager

See architecture [Entity Manager](entity-manager) documentation to go into detail.

Install using the local chart, which is already set up to use the latest stable version of the component,
and the configuration file that *adapts* it to the chosen scenario.

```bash
helm install entity-manager 01-core-charts/openk9-entity-manager \
  -n openk9 \
  -f 01-core-charts/openk9-entity-manager/scenarios/local-runtime.yaml
```


### Verify installation

Check the pod startup logs for the absence of serious errors.

```bash
kubectl -n openk9 logs $(kubectl -n openk9 get pod --selector="app.kubernetes.io/name=openk9-entity-manager" -o name)
```