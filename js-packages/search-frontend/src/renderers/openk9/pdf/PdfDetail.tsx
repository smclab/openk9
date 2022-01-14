import { faFilePdf } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { css } from "styled-components/macro";
import { myTheme } from "../../../components/myTheme";
import { DetailAttribute } from "../../../renderer-components/DetailAttribute";
import { DetailContainer } from "../../../renderer-components/DetailContainer";
import { DetailIconContainer } from "../../../renderer-components/DetailIconContainer";
import { DetailLink } from "../../../renderer-components/DetailLink";
import { DetailTextContent } from "../../../renderer-components/DetailTextContent";
import { DetailTitle } from "../../../renderer-components/DetailTitle";
import { HighlightableText } from "../../../renderer-components/HighlightableText";
import { PdfResultItem } from "./PdfItem";

type PdfDetailProps = {
  result: PdfResultItem;
};
export function PdfDetail({ result }: PdfDetailProps) {
  return (
    <DetailContainer>
      <DetailIconContainer>
        <FontAwesomeIcon icon={faFilePdf} />
      </DetailIconContainer>
      <DetailTitle>
        <HighlightableText result={result} path="document.title" />
      </DetailTitle>
      <DetailLink>
        <HighlightableText result={result} path="document.url" />
      </DetailLink>
      {result.source.file?.lastModifiedDate && (
        <DetailAttribute label="Last modified">
          {new Date(result.source.file.lastModifiedDate).toLocaleString()}
        </DetailAttribute>
      )}
      <div>
        {result.source.resources.binaries.map((binary) => {
          return (
            <iframe
              key={binary.id}
              title={binary.id}
              src={`/api/searcher/resources/${result.source.datasourceId}/${result.source.id}/${binary.id}`}
              css={css`
                width: 100%;
                height: 50vh;
                border: 1px solid ${myTheme.searchBarBorderColor};
                border-radius: 4px;
              `}
            />
          );
        })}
      </div>
      <DetailTextContent result={result} path="document.content" />
    </DetailContainer>
  );
}
