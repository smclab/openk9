import React from "react";
import { GenericResultItem } from "@openk9/http-api";
import { Result } from "./Result";
import { PluginResultRenderes } from "@openk9/search-ui-components";
import { OpenK9UITemplates } from "../api";
import { EmbedElement } from "./EmbedElement";

type ResultPageProps = {
  results: GenericResultItem<unknown>[];
  templates: OpenK9UITemplates;
  onDetail: (result: GenericResultItem<unknown>) => void;
  renderers: PluginResultRenderes;
};
function ResultPage({
  renderers,
  results,
  onDetail,
  templates,
}: ResultPageProps) {
  return (
    <React.Fragment>
      {results.map((result) => {
        const cusomizedResult = templates.result?.({
          result,
          setDetail: onDetail,
        });
        return cusomizedResult ? (
          <React.Fragment key={result.source.id}>
            <EmbedElement element={cusomizedResult} />
          </React.Fragment>
        ) : (
          <div
            key={result.source.id}
            style={{ marginLeft: "16px", marginRight: "16px" }}
          >
            <Result
              result={result}
              resultRenderers={renderers.resultRenderers}
              onSelect={() => onDetail(result)}
            />
          </div>
        );
      })}
    </React.Fragment>
  );
}
export const ResultPageMemo = React.memo(ResultPage);
