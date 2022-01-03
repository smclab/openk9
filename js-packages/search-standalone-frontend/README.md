DEPRECATED

# OpenK9 Standalone Search UI

This package contains the React App for the standalone search UI. See create-react-app docs for more info.

## Building Docker Image

On the root folder of the repository:

```
docker build -t smclab/search-standalone-frontend:latest -f js-packages/search-standalone-frontend/Dockerfile .
docker run -p 8080:80 smclab/search-standalone-frontend
```
