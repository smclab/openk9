import React from "react";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "./truncatedLineStyle";

type ResultLinkProps = { href: string; children: React.ReactNode };
export function ResultLink({ href, children }: ResultLinkProps) {
  return (
    <a
      className="openk9-embeddable--result-link"
      href={href}
      target="_blank"
      rel="noreferrer"
      css={css`
        font-size: 0.8em;
        display: block;
        margin-bottom: 8px;
        ${truncatedLineStyle}
      `}
    >
      {children}
    </a>
  );
}
