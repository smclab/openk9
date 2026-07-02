import React, { Children } from "react";
import { faExternalLinkAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { ResultTitle } from "./ResultTitle";
import { css } from "styled-components";
import { safeExternalUrl } from "./safeExternalUrl";

type ResultTitleExternalLinkProps = { href: string; children: React.ReactNode };
export function ResultTitleExternalLink({
  href,
  children,
}: ResultTitleExternalLinkProps) {
  const safeHref = safeExternalUrl(href);
  return (
    <div
      css={css`
        display: flex;
        align-items: baseline;
      `}
    >
      <ResultTitle>
        <a href={safeHref} target="_blank" rel="noopener noreferrer">
          {children}
        </a>
      </ResultTitle>
      <a href={safeHref} target="_blank" rel="noopener noreferrer">
        <FontAwesomeIcon
          icon={faExternalLinkAlt}
          css={css`
            margin-left: 0.5em;
          `}
        />
      </a>
    </div>
  );
}
