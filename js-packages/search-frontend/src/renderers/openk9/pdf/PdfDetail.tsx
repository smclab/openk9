import React from "react";
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
import { css } from "styled-components/macro";

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
      <div style={{ marginTop: "10px", marginLeft: "5px" }}>
        <ResultLinkTwo href={result.source.document.url}>
          <HighlightableText result={result} path="document.url" />
        </ResultLinkTwo>
      </div>
      <div style={{ marginTop: "15px" }}>
        {result.source.resources.binaries.map((binary) => {
          const url = `/api/searcher/resources/${result.source.datasourceId}/${result.source.id}/${binary.id}`;
          return (
            <ViewIfUrlOk key={binary.id} url={url}>
              <iframe
                title={binary.id}
                src={url}
                style={{
                  width: "100%",
                  height: "50vh",
                  backgroundColor: "white",
                  borderRadius: "4px",
                  border:
                    "1px solid var(--openk9-embeddable-search--border-color)",
                }}
              />
            </ViewIfUrlOk>
          );
        })}
      </div>
    </DetailContainer>
  );
}

function ViewIfUrlOk({
  url,
  children,
}: {
  url: string;
  children: React.ReactNode;
}) {
  const [isOk, setIsOk] = React.useState(false);
  React.useEffect(() => {
    fetch(url).then((response) => {
      if (response.ok) setIsOk(true);
    });
    return () => setIsOk(false);
  }, [url]);
  return <React.Fragment>{isOk ? children : null}</React.Fragment>;
}
