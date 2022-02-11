import {
  LoginInfo,
  getPlugins,
  loadPlugin,
  ResultRendererPlugin,
  Plugin,
} from "@openk9/rest-api";
import { useQuery } from "react-query";

export function useRenderers(loginInfo: LoginInfo | null) {
  const { data: renderers } = useQuery(["renderers", loginInfo], () => {
    return loadRenderers(loginInfo);
  });
  return renderers;
}

export type Renderers = ReturnType<typeof useRenderers>; // TODO explicit type

async function loadRenderers(loginInfo: LoginInfo | null) {
  const pluginInfos = await getPlugins(loginInfo);
  const plugins = (
    await Promise.all(
      pluginInfos.map((pluginInfo) =>
        loadPlugin(
          pluginInfo.pluginId,
          pluginInfo.bundleInfo.lastModified,
        ).catch(() => null),
      ),
    )
  ).filter((plugin) => plugin !== null) as Plugin<unknown>[];
  const resultRendererPluginServices = plugins
    ?.flatMap((p) => p.pluginServices)
    .filter(
      (ps) => ps.type === "RESULT_RENDERER",
    ) as ResultRendererPlugin<unknown>[];
  const resultTypes = [
    ...new Set(resultRendererPluginServices.map((ps) => ps.resultType)),
  ];
  const byPriority = resultTypes.map((type) => {
    return [
      type,
      resultRendererPluginServices
        .filter((ps) => ps.resultType === type)
        .sort((a, b) => (a.priority || -1) - (b.priority || -1))[0],
    ] as const;
  });
  const resultRenderers = Object.fromEntries(
    byPriority.map(([type, { resultRenderer }]) => [type, resultRenderer]),
  );
  const sidebarRenderers = Object.fromEntries(
    byPriority.map(([type, { sidebarRenderer }]) => [type, sidebarRenderer]),
  );
  return { resultRenderers, sidebarRenderers };
}
