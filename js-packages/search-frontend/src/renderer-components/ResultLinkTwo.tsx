import React from "react";
import { css } from "styled-components";
import { truncatedLineStyle } from "./truncatedLineStyle";

type ResultLinkProps = {
  href: string;
  children: React.ReactNode;
  title?: string;
};
export function ResultLinkTwo({
  href,
  title = "Link Documento",
  children,
}: ResultLinkProps) {
  return (
    <a
      href={href}
      target="_blank"
      rel="noreferrer"
      css={css`
        display: flex;
        justify-content: center;
        align-items: center;
        padding: var(--openk9-embeddable-search--spacing-xs, 4px)
          var(--openk9-embeddable-search--spacing-sm, 8px);
        gap: var(--openk9-embeddable-search--spacing-xs, 4px);
        height: 21px;
        width: 108px;
        background: var(
          --openk9-embeddable-search--primary-background-color,
          #ffffff
        );
        border: 1px solid
          var(
            --openk9-embeddable-search--primary-subtle-background-color,
            #f9edee
          );
        background: var(
          --openk9-embeddable-search--primary-subtle-background-color,
          #f9edee
        );
        border-radius: var(--openk9-embeddable-search--radius-xl, 20px);
        margin-top: var(--openk9-embeddable-search--spacing-xl, 20px);
        cursor: pointer;
        white-space: nowrap;
        text-decoration: none;
      `}
    >
      <div
        css={css`
          color: red;
          margin-bottom: var(--openk9-embeddable-search--spacing-lg, 16px);
          font-weight: var(--openk9-embeddable-search--font-weight-medium, 500);
          font-size: var(--openk9-embeddable-search--font-size-sm, 14px);
          display: block;
          margin-block-start: 1em;
          margin-block-end: 1em;
          margin-inline-start: 0px;
          margin-inline-end: 0px;
        `}
      >
        <div
          css={css`
            display: flex;
            align-items: baseline;
            color: var(
              --openk9-embeddable-search--secondary-active-color,
              #c0272b
            );
          `}
        >
          {title}
        </div>
      </div>
    </a>
  );
}
