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
handler send one message only.

The handle stays the same for the life of the chatbot, so it can be kept aside
and called later; once the chatbot unmounts, `send` returns `false`, so remove any
listener that still holds it. On this version (React 18) `Chatbot` forwards the
ref with `forwardRef`; the integration code above is the same on the React 19
versions, where `ref` is a regular prop.

The library exposes no global event or API to send a message: mapping the host's
own events (portal events, fragments) to `send` stays in the host project.

## AI interaction disclosure

The chatbot always renders a notice under the input telling the user the conversation
is held with an AI system, as required by Regulation (EU) 2024/1689 (AI Act) art. 50 §1
for systems that interact directly with people. Embedding the widget on a third-party
site does not make that obvious from the context, so the notice is not optional.

By default the text comes from the translation for the active language (`it_IT`,
`en_US`, `fr_FR`, `es_ES`, `de_DE`), resolved by `LanguageProvider` from the `language`
prop or from `document.documentElement.lang`.

Pass `aiDisclosureText` to replace the wording — for a client's own legal copy or
branding:

```tsx
<Chatbot
  icon={icon}
  aiDisclosureText="Assistente virtuale del Comune — risposte generate da IA"
/>
```

`aiDisclosureText` customizes the text, it does not switch the notice off: there is no
prop, prop combination or value (`""`, `null`, `undefined`) that removes it from the DOM
— a missing or blank value falls back to the translated default.

To restyle it, target the `openk9-ai-disclosure` class, alongside the other stable
classes the widget exposes (`openk9-toggle-chatbot-button`,
`openk9-toggle-icon-wrapper`). Note that the panel is one text line taller than before:
check the layout if you constrain the container height or position elements over the
panel with custom CSS.
