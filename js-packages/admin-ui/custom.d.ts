/// <reference types="vite/client" />

declare module "*.svg" {
  const content: string;
  export default content;
}

declare module "prettier/standalone" {
  import prettier from "prettier";
  export default prettier;
}

declare module "prettier/parser-typescript" {
  const plugin: any;
  export default plugin;
}
