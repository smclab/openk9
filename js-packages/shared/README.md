# js-packages/shared/

Plain TypeScript modules used by more than one frontend. No `package.json`, no
build step, no `dist`: the sources are imported directly and each app's own
bundler compiles them.

```
js-packages/shared/
├── image-query/        immagine come query per la ricerca vettoriale
│   ├── imageQuery.ts
│   └── imageQuery.test.ts
└── safe-external-url/  protocolli ammessi per i link esterni
    └── safeExternalUrl.ts
```

This folder is not a Yarn workspace: the root `package.json` lists the packages
one by one, and this is not one of them. That is deliberate: it stays a plain
source folder, not a workspace member.

## Rules for anything added here

- **Zero dependencies**, runtime or peer. These modules are compiled into the app
  that imports them and have no `package.json` to declare anything in, so an
  import would silently rely on whatever that app installs. The embeddable
  chatbot, should it ever import them, ships inside customer pages, where a
  strict CSP applies.
- **No JSX, no React.** Pure logic only. Anything that renders belongs to the app
  that renders it.
- **One TypeScript range for every consumer.** Each app compiles these sources
  with its own compiler, but `typescript` has the same range in all of them
  (syncpack keeps it that way, see `../README.md`), so a DOM lib feature reaches
  every consumer or none. The `FROM_IMAGE` cast in `imageQuery.ts` is a leftover
  from when the versions were spread out: `imageOrientation: "from-image"` only
  entered the DOM lib in TypeScript 5.
- **Tests live next to the module** and run inside whichever app consumes it:
  today that is `talk-to`, whose `vitest.config.ts` includes `../shared/**`.
  They import `describe`, `it` and `expect` from `vitest` like any other test.

## How a frontend consumes it

Today only `talk-to` imports these modules (`image-query` and
`safe-external-url`). Every frontend builds with Vite, so another one needs just
a relative import, plus `COPY ./js-packages/shared` in its Dockerfile (see
below): the sources get compiled and bundled like any other file.

## Docker

Every app image copies only its own folder, so a build fails resolving
`../shared/**` unless its Dockerfile also copies this directory:

```dockerfile
COPY ./js-packages/shared ./js-packages/shared
```

## Importing this from a published npm package

`@openk9ui/openk9-chatbot` is built with Vite in library mode and published to
npm. Importing from here works — verified by actually building it with such an
import:

- the shared source is **inlined into the published bundle**, so `dist/` carries
  no reference to `../shared` and consumers install something self-contained;
- one consequence worth knowing: each package that inlines a module gets its own
  copy, so the module-level memoisation in `prepareQueryImageCached` is not
  shared between them. Harmless here (a missed cache hit, never a wrong result).

**There is one thing to fix when that day comes.** Importing from outside the
package folder shifts TypeScript's inferred `rootDir` from `lib/` up to
`js-packages/`, so declarations move from `dist/types/lib/main.d.ts` to
`dist/types/openk9-chatbot/lib/main.d.ts`. The `types` field in the chatbot's
`package.json` then points at a file that no longer exists: the package still
publishes and its JavaScript still works, but TypeScript consumers silently lose
every typing. Either update that `types` path, or set `rollupTypes: true` on the
`dts` plugin so everything collapses into a single declaration file and the
problem cannot recur. Check it with:

```bash
node -e "const fs=require('fs'),p=require('./package.json');console.log(fs.existsSync(p.types))"
```
