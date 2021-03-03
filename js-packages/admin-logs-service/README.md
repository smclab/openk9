# Admin Logs Service

A small Node.JS service to serve Docker containers status and logs, regardless of the backend status.

## Building Docker Image

On the root folder of the repository:

```
docker build -t smclab/admin-logs-service:latest -f js-packages/admin-logs-service/Dockerfile .
docker run -p 3005:3005 -v /var/run/docker.sock:/var/run/docker.sock smclab/admin-logs-service
```
