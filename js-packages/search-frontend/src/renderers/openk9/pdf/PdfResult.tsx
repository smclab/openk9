import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFilePdf } from "@fortawesome/free-solid-svg-icons/faFilePdf";
import { GenericResultItem } from "../../../components/client";
import { PdfResultItem } from "./PdfItem";
import {
  ResultContainer,
  ResultTitle,
  HighlightableText,
  ResultLink,
  ResultTextContent,
  FileIcon,
} from "../../../renderer-components";
import { ResultTitleTwo } from "../../../renderer-components/ResultTitleTwo";
import { ResultContainerTwo } from "../../../renderer-components/ResultContainerTwo";
import { ResultLinkTwo } from "../../../renderer-components/ResultLinkTwo";
import { MoreDetailCard } from "../../../renderer-components/MoreDetailCard";
import { ResultTextContentTwo } from "../../../renderer-components/ResultTextContentTwo";
import { css } from "styled-components";

type PdfResultProps = { result: GenericResultItem<PdfResultItem> };
export function PdfResult({ result }: PdfResultProps) {
  const lastEdit = new Date(result.source.file.lastModifiedDate)
    .toLocaleString()
    .replace(",", "");
  return (
    <ResultContainerTwo>
      <ResultTitleTwo>
        <HighlightableText result={result} path="document.title" />
      </ResultTitleTwo>
      {"document.content" in result.highlight ? (
        <div
          css={css`
            margin-top: var(--openk9-embeddable-search--spacing-sm, 8px);
            max-width: 100%;
            line-height: var(--openk9-embeddable-search--line-height-none, 1);
            max-height: 6em;
            overflow: hidden;
            text-overflow: ellipsis;
            word-wrap: break-word;
            word-break: break-word;
            font-weight: var(
              --openk9-embeddable-search--font-weight-regular,
              400
            );
            font-size: var(--openk9-embeddable-search--font-size-md, 16px);
            color: #71717a;
          `}
        >
          <HighlightableText result={result} path="document.content" />
        </div>
      ) : (
        <div
          css={css`
            margin-top: var(--openk9-embeddable-search--spacing-sm, 8px);
            max-width: 100%;
            line-height: var(--openk9-embeddable-search--line-height-none, 1);
            max-height: 6em;
            overflow: hidden;
            text-overflow: ellipsis;
            word-wrap: break-word;
            word-break: break-word;
            font-weight: var(
              --openk9-embeddable-search--font-weight-regular,
              400
            );
            font-size: var(--openk9-embeddable-search--font-size-md, 16px);
            color: #71717a;
          `}
        >
          <ResultTextContentTwo result={result} path="document.summary" />
        </div>
      )}
      <MoreDetailCard
        icon={<FontAwesomeIcon icon={faFilePdf} />}
        date={lastEdit}
      />
      {result?.source?.document?.url ? (
        <ResultLinkTwo
          href={result.source.document?.url}
          title="Link Documento"
        >
          <HighlightableText result={result} path="document.url" />
        </ResultLinkTwo>
      ) : null}
    </ResultContainerTwo>
  );
}
