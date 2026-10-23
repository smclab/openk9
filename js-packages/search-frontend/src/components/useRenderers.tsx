import { queryOptions, useQuery, useQueryClient } from "@tanstack/react-query";
import { useOpenK9Client } from "./client";
import groupBy from "lodash/groupBy";
import mapValues from "lodash/mapValues";
import { DetailRendererProps, ResultRendererProps, Template } from "./client";

export type Renderers = {
  resultRenderers: {
    [x: string]: React.FC<ResultRendererProps<unknown>>;
  };
  detailRenderers: {
    [x: string]: React.FC<DetailRendererProps<unknown>>;
  };
};

export function useRenderers(): Renderers | undefined {
  const client = useOpenK9Client();
  const queryClient = useQueryClient();
  const options = queryOptions({
    queryKey: ["renderers"],
    queryFn: async () => {
      const templateIds = await client.getTemplatesByVirtualHost();
      const templates = (
        await Promise.all(
          templateIds.map((templateId) => client.loadTemplate(templateId)),
        )
      ).filter((template) => {
        return template !== null;
      }) as Array<Template<unknown>>;
      const byResultType = groupBy(
        templates,
        (template) => template.resultType,
      );
      const byPriority = mapValues(byResultType, (templates) =>
        templates.sort((a, b) => (a.priority || -1) - (b.priority || -1)),
      );
      const resultRenderers = mapValues(byPriority, ([{ result }]) => result);
      const detailRenderers = mapValues(byPriority, ([{ detail }]) => detail);
      return { resultRenderers, detailRenderers };
    },
    // what react-query 3 did with suspense: true
    staleTime: 1000,
    throwOnError: true,
  });
  const query = useQuery(options);
  // like suspense: true in react-query 3: suspend while nothing can be shown, send a failure to the error boundary
  if (query.isLoading) {
    const state = queryClient.getQueryState(options.queryKey);
    if (state?.status === "error") throw state.error;
    throw queryClient.query(options).catch(() => undefined);
  }
  return query.data;
}
