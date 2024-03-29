import React from "react";
import { css } from "styled-components/macro";
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
        padding: 4px 8px;
        gap: 4px;
        height: 21px;
        width: 108px;
        background: #ffffff;
        border: 1px solid #f9edee;
        background: #f9edee;
        border-radius: 20px;
        margin-top: 20px;
        cursor: pointer;
        white-space: nowrap;
        text-decoration: none;
      `}
    >
      <div
        css={css`
          color: red;
          margin-bottom: 14px;
          font-weight: 500;
          font-size: 13px;
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
            color: #c0272b;
          `}
        >
          {title}
        </div>
      </div>
    </a>
  );
}
