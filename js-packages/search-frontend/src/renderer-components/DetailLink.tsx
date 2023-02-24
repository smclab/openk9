import React from "react";
import { css } from "styled-components/macro";

type DetailLinkProps = {
  href: string;
  children: React.ReactNode;
};
export function DetailLink({ href, children }: DetailLinkProps) {
  return (
    <a
      href={href}
      target="_blank"
      rel="noreferrer"
      css={css`
        font-size: 0.8em;
        margin-bottom: 8px;
        word-wrap: break-word;
        word-break: break-word;
        display: block;
        color: #c0272b;
      `}
    >
      {children}
    </a>
  );
}
