import { PdfResultItem } from "./PdfItem";
import { faFilePdf } from "@fortawesome/free-solid-svg-icons/faFilePdf";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  DetailAttribute,
  DetailContainer,
  DetailIconContainer,
  DetailLink,
  DetailTextContent,
  DetailTitle,
  HighlightableText,
  ResultTextContentTwo,
  DetailTextContentTwo,
  MoreDetailCard,
  ResultLinkTwo,
} from "../../../renderer-components";
import { GenericResultItem } from "../../../components/client";
import { css } from "styled-components";

type PdfDetailProps = {
  result: GenericResultItem<PdfResultItem>;
};
export function PdfDetail({ result }: PdfDetailProps) {
  const lastEdit = new Date(result.source.file.lastModifiedDate)
    .toLocaleString()
    .replace(",", " |");
  return (
    <DetailContainer>
      <DetailTitle fontSize="19px" fontweigth="600">
        <HighlightableText result={result} path="document.title" />
      </DetailTitle>
      {"document.content" in result.highlight ? (
        <div
          css={css`
            margin-top: 8px;
            max-width: 100%;
            line-height: 1em;
            max-height: 18em;
            overflow: hidden;
            text-overflow: ellipsis;
            word-wrap: break-word;
            word-break: break-word;
          `}
        >
          <HighlightableText result={result} path="document.content" />
        </div>
      ) : (
        <div
          css={css`
            margin-top: 8px;
            max-width: 100%;
            line-height: 1em;
            max-height: 6em;
            overflow: hidden;
            text-overflow: ellipsis;
            word-wrap: break-word;
            word-break: break-word;
            font-style: normal;
            font-weight: 400;
            font-size: 15px;
            color: #71717a;
          `}
        >
          <DetailTextContent result={result} path="document.summary" />
        </div>
      )}
      <MoreDetailCard
        icon={<FontAwesomeIcon icon={faFilePdf} />}
        date={lastEdit}
      />
      {result.source.document.url ? (
        <div
          css={css`
            margin-top: 10px;
            margin-left: 5px;
          `}
        >
          <ResultLinkTwo
            href={result.source.document.url}
            title="Link Documento"
          >
            <HighlightableText result={result} path="document.url" />
          </ResultLinkTwo>
        </div>
      ) : null}
    </DetailContainer>
  );
}
