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

type PdfResultProps = { result: GenericResultItem<PdfResultItem> };
export function PdfResult({ result }: PdfResultProps) {
  return (
    <ResultContainerTwo>
      <ResultTitleTwo>
        <HighlightableText result={result} path="document.title" />
      </ResultTitleTwo>
      {"document.content" in result.highlight ? (
        <div
          style={{
            marginTop: "8px",
            maxWidth: "100%",
            lineHeight: "1em",
            maxHeight: "6em",
            overflow: "hidden",
            textOverflow: "ellipsis",
            wordWrap: "break-word",
            wordBreak: "break-word",
            fontWeight: "400",
            fontSize: "15px",
            color: "#71717A",
          }}
        >
          <HighlightableText result={result} path="document.content" />
        </div>
      ) : (
        <div
          style={{
            marginTop: "8px",
            maxWidth: "100%",
            lineHeight: "1em",
            maxHeight: "6em",
            overflow: "hidden",
            textOverflow: "ellipsis",
            wordWrap: "break-word",
            wordBreak: "break-word",
            fontWeight: "400",
            fontSize: "15px",
            color: "#71717A",
          }}
        >
          <ResultTextContentTwo result={result} path="document.summary" />
        </div>
      )}
      <MoreDetailCard icon={<FontAwesomeIcon icon={faFilePdf} />} />
      <ResultLinkTwo href={result.source.document.url}>
        <HighlightableText result={result} path="document.url" />
      </ResultLinkTwo>
    </ResultContainerTwo>
  );
}
