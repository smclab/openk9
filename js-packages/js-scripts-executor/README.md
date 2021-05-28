# JS Scripts Executor

## Building Docker Image

On the root folder of the repository:

```
docker build -t smclab/openk9-admin-logs-service:latest -f js-packages/openk9-admin-logs-service/Dockerfile .
docker run -p 3000:3000 -v /var/run/docker.sock:/var/run/docker.sock smclab/openk9-admin-logs-service
```
