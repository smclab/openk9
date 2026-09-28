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

Yarn ignores this folder even though the workspace glob is `js-packages/*`,
because there is no `package.json` here. That is deliberate: it stays a plain
source folder, not a workspace member.

## Rules for anything added here

- **Zero dependencies**, runtime or peer. These modules are compiled into several
  apps that do not share a dependency tree; one of them (the embeddable chatbot)
  also ships inside customer pages, where a strict CSP applies.
- **No JSX, no React.** Pure logic only. Anything that renders belongs to the app
  that renders it.
- **Mind the TypeScript version spread.** Each app compiles these sources with
  its own compiler, and they are not aligned: `talk-to` and `admin-ui` are on
  5.6, `search-frontend` on 5.9. Anything relying on a newer DOM lib breaks the
  oldest consumer. `imageQuery.ts` hits exactly this with `imageOrientation:
  "from-image"`, which only entered the DOM lib in TypeScript 5 — see the
  `FROM_IMAGE` constant and its comment. Typecheck against the **oldest**
  consumer before assuming a change is safe.
- **Tests live next to the module** and run inside whichever app consumes it:
  today that is `talk-to`, whose `vitest.config.ts` includes `../shared/**`.
  They import `describe`, `it` and `expect` from `vitest` like any other test.

## How each frontend consumes it

| App | Bundler | What it needs |
|---|---|---|
| `talk-to` | Vite | relative import + `COPY ./js-packages/shared` in its Dockerfile |
| `search-frontend` | Vite | same |
| `admin-ui` | Vite | same |
| `tenant-ui` | Vite | same |

Vite needs no special configuration: a relative import is enough, and the
sources get compiled and bundled like any other file.

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
