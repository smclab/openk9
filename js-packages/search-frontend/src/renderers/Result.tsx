import React from "react";
import { css } from "styled-components/macro";
import { WebResult } from "./WebResult";
import { ResultDTO } from "../utils/remote-data";
import { myTheme } from "../utils/myTheme";

type ResultProps = {
  result: ResultDTO;
  onDetail(result: ResultDTO | null): void;
};
export function Result({ result, onDetail }: ResultProps) {
  return (
    <div
      css={css`
        padding: 0px 16px 16px 16px;
      `}
    >
      <div
        css={css`
          border: 1px solid ${myTheme.searchBarBorderColor};
          border-radius: 4px;
        `}
        onMouseEnter={() => onDetail(result)}
      >
        {(() => {
          if (result.source.documentTypes.includes("web")) {
            return <WebResult result={result} />;
          }
          return (
            <pre
              css={css`
                height: 100px;
                overflow: hidden;
              `}
            >
              {JSON.stringify(result, null, 2)}
            </pre>
          );
        })()}
      </div>
    </div>
  );
}
export const ResultMemo = React.memo(Result);
