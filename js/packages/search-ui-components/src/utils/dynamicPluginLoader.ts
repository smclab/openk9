import { createAsset } from "use-asset";
import {
  loadPlugin,
  PluginInfo,
  ResultRenderersType,
  SidebarRenderersType,
} from "@openk9/http-api";

export const pluginLoader = createAsset(async (id) => {
  const plugin = await loadPlugin(id);
  return plugin;
});

export function getPluginResultRenderers(pluginInfos: PluginInfo[]) {
  const plugins = pluginInfos
    .map((pI) => pluginLoader.read(pI.pluginId))
    .filter(Boolean);

  let resultRenderersPlugins: ResultRenderersType<any> = {};
  plugins.forEach((plugin) => {
    resultRenderersPlugins = {
      ...resultRenderersPlugins,
      ...plugin.dataSourceRenderingInterface?.resultRenderers,
    };
  });

  return resultRenderersPlugins;
}

export function getPluginSidebarRenderers(pluginInfos: PluginInfo[]) {
  const plugins = pluginInfos
    .map((pI) => pluginLoader.read(pI.pluginId))
    .filter(Boolean);

  let sidebarRenderersPlugins: SidebarRenderersType<any> = {};
  plugins.forEach((plugin) => {
    sidebarRenderersPlugins = {
      ...sidebarRenderersPlugins,
      ...plugin.dataSourceRenderingInterface?.sidebarRenderers,
    };
  });

  return sidebarRenderersPlugins;
}
