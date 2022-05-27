# OpenK9 Admin UI

This package contains the Next App for the search admin configuration. See Next.js documentation for more info.

## Building Docker Image

On the root folder of the repository:

```
docker build -t smclab/admin-ui:latest -f js-packages/admin-ui/Dockerfile .
docker run -p 3000:3000 smclab/admin-ui
```

## Building and linking reast-api and search-frontend

cd rest-api && yarn build && yarn link
cd search-frontend && yarn build && yarn link
cd admin-ui && yarn link @openk9/rest-api && yarn link @openk9/search-frontend

## Development

To start development run the task `yarn dev`. To set up proxy for API set the `BASE_PROXY_PATH` variable in a `.env.local` file.

### Development with custom back-end

`yarn dev`
`node reverseProxy.js`
