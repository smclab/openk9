import { ResultRendererPlugin, Plugin } from "@openk9/rest-api";
import { useQuery } from "react-query";
import { useOpenK9Client } from "./client";

export function useRenderers() {
  const client = useOpenK9Client();
  const { data: renderers } = useQuery(
    ["renderers"],
    async () => {
      const pluginInfos = await client.getPlugins();
      const plugins = (
        await Promise.all(
          pluginInfos.map((pluginInfo) =>
            client
              .loadPlugin(
                pluginInfo.pluginId,
                pluginInfo.bundleInfo.lastModified,
              )
              .catch(() => null),
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
        byPriority.map(([type, { sidebarRenderer }]) => [
          type,
          sidebarRenderer,
        ]),
      );
      return { resultRenderers, sidebarRenderers };
    },
    { suspense: true },
  );
  return renderers;
}

export type Renderers = ReturnType<typeof useRenderers>; // TODO explicit type
