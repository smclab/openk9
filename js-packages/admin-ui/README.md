# OpenK9 Admin UI

This package contains the Next App for the search admin configuration. See Next.js documentation for more info.

## Building Docker Image

On the root folder of the repository:

```
docker build -t smclab/admin-ui:latest -f js-packages/admin-ui/Dockerfile .
docker run -p 3000:3000 smclab/admin-ui
```

## Development

To start development run the task `yarn dev`. To set up proxy for API set the `BASE_PROXY_PATH` variable in a `.env.local` file.
