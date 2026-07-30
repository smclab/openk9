import React from "react";
import { css } from "styled-components";

type DetailLinkProps = {
  href: string;
  children: React.ReactNode;
};
export function DetailLink({ href, children }: DetailLinkProps) {
  return (
    <a
      className="openk9-embeddable-detail-link"
      href={href}
      target="_blank"
      rel="noreferrer"
      css={css`
        font-size: 0.8em;
        margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
        word-wrap: break-word;
        word-break: break-word;
        display: block;
        color: var(--openk9-embeddable-search--secondary-active-color, #c0272b);
      `}
    >
      {children}
    </a>
  );
}
