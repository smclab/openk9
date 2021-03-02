import { createAsset } from "use-asset";
import { loadPlugin } from "@openk9/http-api";

export const pluginLoader = createAsset(async (id) => {
  const plugin = await loadPlugin(id);
  return plugin;
});
