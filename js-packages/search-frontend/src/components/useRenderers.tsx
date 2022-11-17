import { useQuery } from "react-query";
import { useOpenK9Client } from "./client";
import { groupBy, mapValues } from "lodash";
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
  const { data: renderers } = useQuery(
    ["renderers"],
    async () => {
      const templateIds = await client.getTemplatesByVirtualHost();
      const templates = (
        await Promise.all(
          templateIds.map((templateId) => client.loadTemplate(templateId)),
        )
      ).filter((template) => template !== null) as Array<Template<unknown>>;
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
    { suspense: true },
  );
  return renderers;
}
