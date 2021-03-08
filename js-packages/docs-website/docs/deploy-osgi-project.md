---
id: deploy-osgi-project
title: Deploy OSGi project
---

## Prerequisites

Prerequisites are described in [`OSGi project requirements`](/docs/osgi-requirements).

### Clone the repository

Once you have installed the right java version, unzipped karaf archives and configured lombok plugin in your IDE, you need to clone github repository using de following command:

```bash
git clone https://github.com/smclab/openk9.git
cd openk9
```

### Deploy OpenK9

To build OpenK9 you have to execute the following steps:

- create file `gradle-local.properties`
- define `karafDir` into file `gradle-local.properties`:
```
karafDir=/path/to/openk9
```
- add volume to `openk9-karaf` in `docker-compose.yml`:
```
volumes:
  - ./configs:/opt/apache-karaf/configs
  - ./deploy:/opt/apache-karaf/deploy
```
- in the project's root execute command:
```aidl
./gradlew deployDist
```