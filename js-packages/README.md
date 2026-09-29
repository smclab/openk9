# OpenK9 JS Workspace

This folder contains the monorepo with all the JavaScript packages for OpenK9.

The monorepo is managed using Lerna and Yarn. Please use Yarn, not NPM, to install dependencies.

## Packages

The root `package.json` lists the Yarn workspaces one by one: `admin-ui`,
`openk9-chatbot`, `search-frontend`, `talk-to` and `tenant-ui`. The other
folders here (`shared`, `keycloak-theme`, `scripts`) are not packages. Lerna and
syncpack read that same list, so a new package is added there, in
`bump-version.sh` and in its own CI pipeline.

A single `yarn install` at the repository root installs every package from the
root `yarn.lock`. There are no per-package lockfiles.

Lerna runs in fixed mode: every package carries the project version (the one in
`lerna.json`), which `bump-version.sh` sets everywhere at once.

## Dependency versions

A library used by more than one package is declared with the same range in each
of them, and in every section that lists it (`dependencies`, `devDependencies`,
`peerDependencies`). The root `resolutions` hold only runtime singletons (React,
React DOM, their types, Emotion) and security pins, and repeat the declared range
word for word.

[syncpack](https://syncpack.dev) checks all of this, with the configuration in
`.syncpackrc.json` at the repository root:

```bash
yarn lint:versions   # the same check as the "JS Dependency Versions" CI job
yarn syncpack fix    # moves every declaration to the highest range in use
```

To bump a shared library, raise it in one package and run `yarn syncpack fix`:
the other packages, and the matching resolution if there is one, follow. Then run
`yarn install` to update the lockfile.

The configuration lists the deliberate exceptions, each with a label saying why:

- **lodash**: the resolution is an exact security pin, while the packages declare
  a range. The two may differ, but the range must still include the pin.
- **`@openk9ui/openk9-chatbot` in search-frontend**: an npm alias to the published
  package, on purpose not the workspace copy.
- **Package versions**: they are the project version, owned by `bump-version.sh`
  and Lerna, not by syncpack.
