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
} from "../../../renderer-components";
import { GenericResultItem } from "../../../components/client";

type PdfDetailProps = {
  result: GenericResultItem<PdfResultItem>;
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
      <DetailLink href={result.source.document.url}>
        <HighlightableText result={result} path="document.url" />
      </DetailLink>
      {result.source.file.lastModifiedDate && (
        <DetailAttribute label="Last modified">
          {new Date(result.source.file.lastModifiedDate).toLocaleString()}
        </DetailAttribute>
      )}
      <div>
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
          }}
        >
          <DetailTextContent result={result} path="document.summary" />
        </div>
      )}
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
