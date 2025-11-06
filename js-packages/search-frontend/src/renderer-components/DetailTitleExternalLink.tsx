import React, { Children } from "react";
import { faExternalLinkAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { DetailTitle } from "./DetailTitle";
import { css } from "styled-components/macro";

type DetailTitleExternalLinkProps = { href: string; children: React.ReactNode };
export function DetailTitleExternalLink({
  href,
  children,
}: DetailTitleExternalLinkProps) {
  return (
    <div
      css={css`
        display: flex;
        align-items: baseline;
      `}
    >
      <DetailTitle>
        <a href={href} target="_blank">
          {children}
        </a>
      </DetailTitle>
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
