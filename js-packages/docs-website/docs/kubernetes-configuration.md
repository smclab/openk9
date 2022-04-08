---
id: kubernetes-configuration
title: Kubernetes configuration
---

In this guide we'll see how to prepare Openk9 environment on K3s server.

## Intro

This guide assumes the presence of a server [K3s](https://k3s.io/) also in configuration
[single node] where the *control plane* and *worker* roles coexist on the same server.

All operations can be carried out from your workstation as indicated in [Kubernetes from PC Host].

## Namespace

Even if you have an entire dedicated kubernetes, it is a good idea to install OpenK9 in its own namespace.

``bash
kubectl create namespace openk9
``

Attention, the use of a namespace other than `default` requires that in every `kubectl` or `helm` command
there is the indication of the namespace to use: `-n openk9`.
To get around this *inconvenience* it is possible to make "openk9" the default namespace

``bash
kubectl config set-context --current --namespace = openk9
``

## Dns resolution

Inside the pods created in the `openk9` namespace the `/etc/resolv.conf` file contains the indications for resolving DNS names.
The file is populated with

``
search openk9.svc.cluster.local svc.cluster.local cluster.local localdomain
nameserver x.x.x.x
``

Therefore, although it is always preferable to indicate a service with ``x<name>.<namespace>``,
if the service is within the same namespace it will also be found by indicating only its name.


## Ingress and tls

OpenK9 is a headless solution and the interfaces provided are built with client technologies (eg: ReactJS) that use the
API on `http` or `https`(better).

Furthermore, the various third-party products used have dashboards or similar that it would be useful to be able to use
externally to Kuberertes in the contexts of development or search for errors.

Before proceeding with the installation, it is therefore necessary to define:

* the Full Qualified Domain Name through which the OpenK9 solution will be accessible from outside the Kubernetes cluster

* how the x509 certificate used by Ingress will be provided.


### Certificate for "openk9.local"

Thanks to the presence of the **cert-manager** within our K3s, we are autonomous in creating a start certificate for "openk9.local".

We prepare the `star-openk9-local-certs.yaml` file with the definition of the certificate and its Issuer

```yaml
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: selfsigned-ca
spec:
  isCA: true
  commonName: "* .openk9.local"
  secretName: selfsigned-ca-secret
  dnsNames:
    - "* .openk9.local"
    - ".openk9.local"
    - "openk9.local"
  privateKey:
    algorithm: RSA
    size: 2048
  issuerRef:
    name: selfsigned-cluster-issuer
    kind: ClusterIssuer
---
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: openk9-ca-issuer
spec:
  ca:
    secretName: selfsigned-ca-secret
```

and we apply it

``
kubectl apply -n openk9 -f star-openk9-local-certs.yaml
``

and I verify it is created

``
kubectl -n openk9 describe secret selfsigned-ca-secret
``
