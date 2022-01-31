import { faFilePdf } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React from "react";
import { css } from "styled-components/macro";
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
                css={css`
                  width: 100%;
                  height: 50vh;
                  background-color: white;
                  border: 1px solid
                    var(--openk9-embeddable-search--border-color);
                  border-radius: 4px;
                `}
              />
            </ViewIfUrlOk>
          );
        })}
      </div>
      <DetailTextContent result={result} path="document.content" />
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
