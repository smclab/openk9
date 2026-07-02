import React, { Children } from "react";
import { faExternalLinkAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { DetailTitle } from "./DetailTitle";
import { css } from "styled-components";
import { safeExternalUrl } from "./safeExternalUrl";

type DetailTitleExternalLinkProps = { href: string; children: React.ReactNode };
export function DetailTitleExternalLink({
  href,
  children,
}: DetailTitleExternalLinkProps) {
  const safeHref = safeExternalUrl(href);
  return (
    <div
      css={css`
        display: flex;
        align-items: baseline;
      `}
    >
      <DetailTitle>
        <a href={safeHref} target="_blank" rel="noopener noreferrer">
          {children}
        </a>
      </DetailTitle>
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
