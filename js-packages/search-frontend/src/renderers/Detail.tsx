import React from "react";
import { css } from "styled-components/macro";
import { WebDetail } from "./WebDetail";
import { ResultDTO } from "../utils/remote-data";

type DetailProps = {
  result: ResultDTO;
};
function Detail({ result }: DetailProps) {
  return (
    <div
      css={css`
        padding: 8px 16px;
      `}
    >
      {(() => {
        if (result.source.documentTypes.includes("web")) {
          return <WebDetail result={result} />;
        }
        return <pre css={css``}>{JSON.stringify(result, null, 2)}</pre>;
      })()}
    </div>
  );
}
export const DetailMemo = React.memo(Detail);
