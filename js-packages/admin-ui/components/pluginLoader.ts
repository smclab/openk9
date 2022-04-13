import { createAsset } from "use-asset";
import { client } from "./client";

/**
 * This object is to be used when loading plugins in a React Suspense environment.
 * Remember to add a <Suspense /> wrapper, or it will crash.
 *
 * @example
 * const plugin = pluginLoader.read("plugin-id");
 * console.log(plugin.displayName);
 */
 export const pluginLoader = createAsset(
  async (id: string, lastModified: number) => {
    const plugin = await client.loadPlugin(id, lastModified);
    return plugin;
  },
);