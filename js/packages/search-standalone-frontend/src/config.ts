import {
  ResultRenderersType,
  SidebarRenderersType,
} from "@openk9/search-ui-components";

export interface Config {
  querySourceBarShortcuts: { id: string; text: string }[];
  resultRenderers: ResultRenderersType;
  sidebarRenderers: SidebarRenderersType;
}

export const config: Config = (window as any).config;
