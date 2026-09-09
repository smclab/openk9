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
