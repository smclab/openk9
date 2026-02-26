---
id: using-kubernetes-openshift
title: Install on Kubernetes/Openshift
---

To run Openk9 for production use of container orchestration platform like Kubernetes or Openshift is recommended.

Easy way to install Openk9 on these type of Platforms is to use Helm Charts.

Git repository is available with tutorial and all needed files.

## Prerequisites

To run OpenK9 under Docker you need to have installed:

- [Git](https://git-scm.com/)
- [Kubernetes](https://kubernetes.io/) or [Openshift](https://www.redhat.com/en/technologies/cloud-computing/openshift) cluster
- [Kubectl](https://kubernetes.io/docs/tasks/tools/): for Kubernetes
- [Oc](https://docs.openshift.com/container-platform/4.11/cli_reference/openshift_cli/getting-started-cli.html): for Openshift
- [Helm](https://www.docker.com/)


### Clone the repository

Once you have installed the necessary dependencies clone the repository on your machine to get all the required files and configs:

```bash
git clone https://git.smc.it/openk9/openk9.git
cd helm-charts
```

Now you can follow [Readme.md](https://github.com/smclab/openk9/blob/main/helm-charts/README.md) file to install Openk9.
