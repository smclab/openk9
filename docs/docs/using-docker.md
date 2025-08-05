---
id: using-docker
title: Install on Docker
---

The fastest way to try OpenK9 is by using our prebuilt Docker images and our docker-compose configuration.

Docker Compose is configure to run only [core installation](./types-of-installation.md#different-packages) and [gen ai installation](./types-of-installation.md#different-packages).

:::caution
Docker Compose installation it is not recommended for production environments. Check for [Kubernetes/Openshift installation](./using-kubernetes-openshift.md) for production cases.
:::

## Prerequisites

To run OpenK9 under Docker you need to have installed:

- [Git](https://git-scm.com/)
- [Docker](https://www.docker.com/)
- [docker-compose](https://docs.docker.com/compose/)

:::caution
OpenK9 requires the use of Opensearch in Docker, which may require some specific OS–level configuration regarding swap memory.

To learn more about using Opensearch in docker read the official article about
[using Docker images in production](https://opensearch.org/docs/latest/install-and-configure/install-opensearch/docker/).
:::

### Clone the repository

Once you have installed the necessary dependencies clone the repository on your machine to get all the required files and configs:

```bash
git clone https://github.com/smclab/openk9.git
cd openk9
```

### Start OpenK9

To make Openk9 run on your machine with latest stable release, you just need [docker](https://docs.docker.com/get-started/get-docker/) installed and run:

```bash
docker compose up -d
```

After all components have been started, openk9 is runinng with initial configuration at address *demo.openk9.localhost*.

To accesso to admin panel go to [http://demo.openk9.localhost/admin](http://demo.openk9.localhost/admin]). Access with username *k9admin* and password *openk9*.

Search frontend is available here:

- [Standalone search frontend](http://demo.openk9.localhost) to test search on indexed data.

To test conversational search:

- [Conversational search frontend](http://demo.openk9.localhost/chat) to chat with indexed data

Enjoy Openk9!

### Docker Compose environment

In this Docker Compose file, OpenK9 is configured only with a web crawler connector. To add other connectors check out for [connectors section](/plugins) a find how to add to Docker Compose file.