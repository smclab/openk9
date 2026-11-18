# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type aware lint rules:

- Configure the top-level `parserOptions` property like this:

```js
export default tseslint.config({
  languageOptions: {
    // other options...
    parserOptions: {
      project: ['./tsconfig.node.json', './tsconfig.app.json'],
      tsconfigRootDir: import.meta.dirname,
    },
  },
})
```

- Replace `tseslint.configs.recommended` to `tseslint.configs.recommendedTypeChecked` or `tseslint.configs.strictTypeChecked`
- Optionally add `...tseslint.configs.stylisticTypeChecked`
- Install [eslint-plugin-react](https://github.com/jsx-eslint/eslint-plugin-react) and update the config:

```js
// eslint.config.js
import react from 'eslint-plugin-react'

export default tseslint.config({
  // Set the react version
  settings: { react: { version: '18.3' } },
  plugins: {
    // Add the react plugin
    react,
  },
  rules: {
    // other rules...
    // Enable its recommended rules
    ...react.configs.recommended.rules,
    ...react.configs['jsx-runtime'].rules,
  },
})
```

## RAG endpoint

`ragMode` picks which endpoint answers the conversation, and is chosen when the
chatbot is instantiated:

```tsx
import { Chatbot, type RagMode } from "@openk9ui/openk9-chatbot";

<Chatbot icon={icon} ragMode="chat" />
```

| value | endpoint | behaviour |
|---|---|---|
| `chat-tool` (default) | `/api/rag/chat-tool` | the agent decides whether to consult the knowledge base before answering |
| `chat` | `/api/rag/chat` | always goes through retrieval |

Both take the same request body and emit the same stream events, so switching
changes only the url. Omitting the prop keeps the previous behaviour.

Which one suits an integration depends on the tenant's RAG configuration: the
two endpoints read different configurations (`CHAT_RAG_TOOL` and `CHAT_RAG`),
each with its own prompt and its own routing instruction.

## Sending a message from the host page

The page that hosts the chatbot can ask a question on the user's behalf, e.g. from
a button outside the widget. Pass a `ref` and call `send` on it:

```tsx
import { Chatbot, type ChatbotHandle } from "@openk9ui/openk9-chatbot";

const chatbotRef = React.useRef<ChatbotHandle>(null);

<button onClick={() => chatbotRef.current?.send("What is OpenK9?")}>
  Ask the chatbot
</button>
<Chatbot ref={chatbotRef} icon={icon} />
```

`send` behaves like the user typing the message and pressing send: it opens the
panel (moving the focus to its input, as the toggle button does) and shows the
question in the conversation. It returns `true` when the message goes out and
`false` when nothing is sent: an empty or whitespace-only message, an answer
still streaming, or a chatbot that has been unmounted. Two calls in the same event
handler send one message only. With `requireLogin` and an anonymous user, `send`
returns `false` too: the panel opens on the login notice, as after the send
button, and the message is not kept.

The handle stays the same for the life of the chatbot, so it can be kept aside
and called later; once the chatbot unmounts, `send` returns `false`, so remove any
listener that still holds it. This version requires React 19, where `ref` is a
regular prop: forcing it onto React 18 leaves the ref empty (React warns about it
in development only), so `send` is never reachable.

The library exposes no global event or API to send a message: mapping the host's
own events (portal events, fragments) to `send` stays in the host project.

## Opening and closing the panel

The same handle opens and closes the panel without asking anything, e.g. from an
"Ask the assistant" button in the header of the host site:

```tsx
<button onClick={() => chatbotRef.current?.open()}>Ask the assistant</button>
<Chatbot ref={chatbotRef} icon={icon} showLauncher={false} />
```

- `open()` opens the panel and moves the focus to its input; no request starts.
- `close()` closes it; the conversation is still there when it opens again.

Both do nothing when the panel is already in that state, or once the chatbot has
unmounted.

`showLauncher` (default `true`) shows the round button that opens and closes the
panel. With `showLauncher={false}` the button is not rendered: the panel opens only
through `open()` or `send()`, and closes with the close button in its header, Esc
or `close()`.

The panel is a modal dialog (`role="dialog"`, `aria-modal="true"`) named after
`title`, so a screen reader announces it with the title; without `title` it is
named after `nameChatbot`, or "Chatbot". While it is open, Tab stays inside it; the
close button in the header is there on every screen size, and Esc pressed while the
focus is inside the panel closes it too. That Esc stops bubbling there, so the
host's bubbling Esc listeners (e.g. on `document`) do not receive it; listeners in
the capture phase still do. When the panel closes with the focus inside it, the
focus goes back to the element that had it before opening: the round button or the
host's own button. When that element is gone or hidden, or the browser never gave
it the focus, the focus goes to the round button, if it is shown. If something else
has taken the focus in the meantime (e.g. a login modal of the host page), the
focus stays there.

If `onLoginRequest` opens a login modal of the host page, call `close()` first:
while the panel is open its focus trap pulls Tab back inside it.

The header buttons fall back to default icons when `icon.refreshChatIcon` or
`icon.closeModal` are not passed. `placeholder` replaces the placeholder of the input
(default: the translation for the active language):

```tsx
<Chatbot icon={icon} title="Assistente" placeholder="Scrivi la tua domanda…" />
```

## AI interaction disclosure

The chatbot always renders a notice telling the user the conversation is held with an
AI system — under the input by default — as required by Regulation (EU) 2024/1689
(AI Act) art. 50 §1 for systems that interact directly with people. Embedding the widget on a third-party
site does not make that obvious from the context, so the notice is not optional.

By default the text comes from the translation for the active language (`it_IT`,
`en_US`, `fr_FR`, `es_ES`, `de_DE`), resolved by `LanguageProvider` from the `language`
prop or from `document.documentElement.lang`.

Pass `aiDisclosure` to replace the wording — for a client's own legal copy or
branding — and to choose where it goes. It takes two optional slots, each a node:
`bottom` renders under the input, `top` above the message list.

```tsx
<Chatbot
  icon={icon}
  aiDisclosure={{
    bottom: "Assistente virtuale del Comune — risposte generate da IA",
  }}
/>
```

Filling one slot only moves the notice there, so `{ top: … }` puts it above the
conversation and leaves nothing under the input. Filling both shows it twice — the
pattern `talk-to` uses, with the copy on the opening screen and again by the field.

```tsx
<Chatbot
  icon={icon}
  aiDisclosure={{
    top: (
      <>
        Stai parlando con un <strong>assistente virtuale</strong>.
      </>
    ),
  }}
/>
```

`aiDisclosure` customizes the wording and the position, it does not switch the notice
off: there is no prop, prop combination or value (`{}`, `""`, `null`, `undefined`) that
removes it from the DOM — when neither slot carries anything the translated default
lands under the input.

Only the instance the input points at through `aria-describedby` carries an id, so
showing the notice in both slots never puts two nodes with the same id in the document.

To restyle it, target the `openk9-ai-disclosure` class, alongside the other stable
classes the widget exposes (`openk9-toggle-chatbot-button`,
`openk9-toggle-icon-wrapper`). Note that the panel is one text line taller than before:
check the layout if you constrain the container height or position elements over the
panel with custom CSS.

## Look and theming

The chatbot has no UI library behind it: it renders plain HTML elements styled by
its own stylesheet, which the package imports together with the components
(`sideEffects: ["**/*.css"]`), so there is no CSS to import by hand. It no longer
depends on MUI or emotion.

### Light and dark

`colorScheme` picks the set of default colours: `"light"` (default) or `"dark"`.

```tsx
<Chatbot icon={icon} colorScheme="dark" />
```

### Changing the look with CSS variables

Every colour, the fonts, the text sizes and the size of the panel come from CSS
variables with the `--openk9-chatbot-` prefix. Set them on `:root`, on any ancestor of the chatbot
or on the chatbot itself (`.openk9-chatbot-structure`): your value wins over the
default of either scheme. Those are the supported places: on a deeper element the
variable still applies where it is read directly, but the defaults derived from it
(e.g. the user bubble from `--openk9-chatbot-primary`) do not follow.

```css
:root {
  --openk9-chatbot-primary: #0b5394;
  --openk9-chatbot-primary-hover: #3d85c6;
  --openk9-chatbot-font-family: "Source Sans 3", sans-serif;
  --openk9-chatbot-message-font-size: 16px;
}
```

Variables left alone keep the default of the active `colorScheme`, so a dark
integration can start from `colorScheme="dark"` and change only its brand colours.
Some defaults follow `--openk9-chatbot-primary` (marked "primary" below): set the
variable itself to break the link. `--openk9-chatbot-primary-hover` does not, so set
it together with the primary colour.

| variable | light | dark | used for |
|---|---|---|---|
| `--openk9-chatbot-primary` | `#C0272B` | `#CF3236` | toggle and send buttons, bubble borders, user bubble, loader, input focus, clear-chat button |
| `--openk9-chatbot-primary-hover` | `#E77979` | `#A82125` | toggle button on hover |
| `--openk9-chatbot-on-primary` | `#FFFFFF` | `#FFFFFF` | icons and text on the primary colour |
| `--openk9-chatbot-panel-bg` | `#FFFFFF` | `#1E1E1E` | panel background |
| `--openk9-chatbot-panel-text` | inherited from the page | `#EDEDED` | text colour of the panel (e.g. the `title`) |
| `--openk9-chatbot-border` | `rgba(0, 0, 0, 0.12)` | `rgba(255, 255, 255, 0.12)` | panel, header and login notice borders |
| `--openk9-chatbot-user-message-bg` | primary | primary | user bubble background |
| `--openk9-chatbot-user-message-text` | on-primary | on-primary | user bubble text |
| `--openk9-chatbot-bot-message-bg` | `#FFFFFF` | `#2A2A2A` | chatbot bubble background |
| `--openk9-chatbot-bot-message-text` | `#000000` | `#EDEDED` | chatbot bubble text |
| `--openk9-chatbot-message-border` | primary | primary | border of both bubbles |
| `--openk9-chatbot-secondary-text` | `#666666` | `#A8A8A8` | timestamps, sources count |
| `--openk9-chatbot-input-bg` | `#FFFFFF` | `#2A2A2A` | input background |
| `--openk9-chatbot-input-text` | `#000000` | `#EDEDED` | input text |
| `--openk9-chatbot-input-border` | `rgba(0, 0, 0, 0.23)` | `rgba(255, 255, 255, 0.23)` | input border |
| `--openk9-chatbot-input-border-hover` | `#000000` | `#EDEDED` | input border on hover |
| `--openk9-chatbot-input-placeholder` | `#757575` | `#9E9E9E` | input placeholder |
| `--openk9-chatbot-search-border` | `#C7C7C7C7` | `rgba(255, 255, 255, 0.16)` | line above the input area |
| `--openk9-chatbot-icon-button` | `rgba(0, 0, 0, 0.54)` | `rgba(255, 255, 255, 0.7)` | close buttons, copy and expand buttons of the sources |
| `--openk9-chatbot-icon-button-hover-bg` | `rgba(0, 0, 0, 0.04)` | `rgba(255, 255, 255, 0.08)` | those buttons on hover |
| `--openk9-chatbot-disabled-bg` | `rgba(0, 0, 0, 0.12)` | `rgba(255, 255, 255, 0.12)` | send button while disabled |
| `--openk9-chatbot-disabled-text` | `rgba(0, 0, 0, 0.26)` | `rgba(255, 255, 255, 0.3)` | disabled send and clear-chat buttons |
| `--openk9-chatbot-error-bg` | `#FCEAEA` | `#3B1E1E` | error bubble background |
| `--openk9-chatbot-error-border` | `#D32F2F` | `#F28B82` | error bubble border |
| `--openk9-chatbot-error-text` | `#C62828` | `#F28B82` | error bubble text and icon |
| `--openk9-chatbot-disclosure-text` | `#595959` | `#B3B3B3` | AI disclosure text |
| `--openk9-chatbot-sources-control` | `#12518F` | `#8AB4F8` | "show more / show less" of the sources |
| `--openk9-chatbot-sources-control-hover` | `#2782EA` | `#AECBFA` | the same, on hover |
| `--openk9-chatbot-notice-bg` | `#FFFFFF` | `#2A2A2A` | login notice background |
| `--openk9-chatbot-notice-text` | `#000000` | `#EDEDED` | login notice text |
| `--openk9-chatbot-notice-link` | primary | `#FF8A80` | login notice icon and "Log in" button |
| `--openk9-chatbot-link` | not applied: links keep the page colour | `#8AB4F8` | links in the answers, dark scheme only |
| `--openk9-chatbot-font-family` | `"Titillium Web", sans-serif` | same | messages, input, sources, AI disclosure |
| `--openk9-chatbot-font-family-secondary` | `Roboto, sans-serif` | same | timestamps, sources count, login notice |
| `--openk9-chatbot-message-font-size` | `12px` | same | message text, error bubble, login notice |
| `--openk9-chatbot-input-font-size` | `12px` | same | input text |
| `--openk9-chatbot-time-font-size` | `10px` | same | timestamps, sources count |
| `--openk9-chatbot-disclosure-font-size` | `10px` | same | AI disclosure |
| `--openk9-chatbot-sources-font-size` | `12px` | same | source titles, "show more / show less" |
| `--openk9-chatbot-panel-width` | `365px` | same | width of the panel; on screens narrower than 576px the panel fills the screen |
| `--openk9-chatbot-panel-height` | `460px` | same | height of the panel; on screens narrower than 576px the panel fills the screen |

The colours of the single sources are computed per source and are not variables
yet. Roboto and Titillium Web are still loaded by the library.

The `openk9-*` class names stay available for anything the variables do not
cover. The `--_k9-*` variables are internal: do not set or read them.

### Migrating from `themeCustom`

`themeCustom` and the `@mui/*` and `@emotion/*` peer dependencies are gone: remove
the prop, and the packages too if nothing else in your project uses them. Carry your
theme over to the variables:

| MUI theme | variable |
|---|---|
| `palette.primary.main` | `--openk9-chatbot-primary` |
| `palette.primary.light` | `--openk9-chatbot-primary-hover` |
| `palette.primary.contrastText` | `--openk9-chatbot-on-primary`, `--openk9-chatbot-panel-bg` |
| `palette.text.secondary` | `--openk9-chatbot-secondary-text` |
| `palette.divider` | `--openk9-chatbot-border` |
| `palette.background.default` | `--openk9-chatbot-bot-message-bg` |
| `palette.background.paper` | `--openk9-chatbot-notice-bg` |
| `palette.text.primary` | `--openk9-chatbot-notice-text` |
| `typography.fontFamily` | `--openk9-chatbot-font-family` |
| `typography.body2.fontFamily` | `--openk9-chatbot-font-family-secondary` |

CSS written against the classes MUI and emotion generated (`Mui*`, `css-*`) no
longer matches anything: move it to the variables or to the `openk9-*` classes.
