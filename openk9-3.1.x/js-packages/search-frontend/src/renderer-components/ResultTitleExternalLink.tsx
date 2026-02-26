import React, { Children } from "react";
import { faExternalLinkAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { ResultTitle } from "./ResultTitle";
import { css } from "styled-components";

type ResultTitleExternalLinkProps = { href: string; children: React.ReactNode };
export function ResultTitleExternalLink({
  href,
  children,
}: ResultTitleExternalLinkProps) {
  return (
    <div
      css={css`
        display: flex;
        align-items: baseline;
      `}
    >
      <ResultTitle>
        <a href={href} target="_blank">
          {children}
        </a>
      </ResultTitle>
      <a href={href} target="_blank">
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
