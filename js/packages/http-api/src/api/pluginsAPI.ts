import { PluginInfo, Plugin } from "../types";
import { apiBaseUrl, apiBaseUrlStatic } from "./common";

export async function getPlugins(): Promise<PluginInfo[]> {
  const request = await fetch(`${apiBaseUrl}/plugin`);
  const response: PluginInfo[] = await request.json();
  return response;
}

export async function loadPlugin<E>(id: string): Promise<Plugin<E>> {
  const jsURL = `${apiBaseUrlStatic}/plugins/${id}/static/build/index.js`;
  // @ts-ignore
  const code = await import(/* webpackIgnore: true */ jsURL);
  const plugin = code.plugin as Plugin<E>;
  return plugin;
}
