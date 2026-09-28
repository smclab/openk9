# talk-to

## Available Scripts

In the project directory, you can run:

### `yarn start`

Runs the app in development mode with Vite.\
Open [http://localhost:3000/chat/](http://localhost:3000/chat/) to view it in the browser. Requests to `/api` are
proxied to `https://k9-frontend.openk9.io`.

### `yarn test`

Runs the tests with Vitest in watch mode (`yarn test --run` for a single run). The tests of `../shared` run here too.

### `yarn build`

Type-checks the sources with `tsc`, then builds the app for production with Vite into the `build` folder.

## Environment variables

Vite exposes to the code only the variables prefixed with `VITE_`, read through `import.meta.env`. The `REACT_APP_`
prefix of Create React App is no longer read.

## AI interaction disclosure

Talk-to always shows a notice telling the user the conversation is held with an AI
system, as required by Regulation (EU) 2024/1689 (AI Act) art. 50 §1. It appears in two
places: on the initial screen, under the welcome message, and permanently under the
input once the conversation has started.

The wording lives in the `ai-disclosure` key of
`src/translations/translation_{it,en,fr,es,de}.json` and follows the language selector
like the rest of the interface. Customize it by editing that key in every language you
ship; keep it aligned with the `aiDisclosure` key of `@openk9ui/openk9-chatbot`, so the
two packages do not carry two different wordings of the same notice.

The notice is not something the interface can turn off.
