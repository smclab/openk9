import React from "react";
import { css } from "styled-components/macro";
import { myTheme } from "../utils/myTheme";
import { Logo } from "./Logo";

type PageLayoutProps = {
  dockbar: React.ReactNode;
  search: React.ReactNode;
  result: React.ReactNode;
  detail: React.ReactNode;
};
export function PageLayout({
  dockbar,
  search,
  result,
  detail,
}: PageLayoutProps) {
  return (
    <div
      css={css`
        width: 100vw;
        height: 100vh;
        display: grid;
        grid-template-columns: 50% 50%;
        grid-template-rows: auto auto 1fr;
        grid-template-areas:
          "dockbar dockbar"
          "search search"
          "result detail";
      `}
    >
      <div
        css={css`
          grid-area: search;
          background-color: ${myTheme.backgroundColor2};
          padding: 16px;
        `}
      >
        {search}
      </div>
      <div
        css={css`
          grid-area: dockbar;
          padding: 8px 16px;
          box-shadow: ${myTheme.separationBoxShadow};
          display: flex;
          align-items: center;
        `}
      >
        <div
          css={css`
            font-size: 20;
            color: ${myTheme.dockbarTextColor};
            display: flex;
            align-items: center;
          `}
        >
          <span
            css={css`
              color: ${myTheme.redTextColor};
              margin-right: 8px;
            `}
          >
            <Logo size={32} />
          </span>
          <span>Open</span>
          <span
            css={css`
              font-weight: 700;
            `}
          >
            K9
          </span>
        </div>
        <div
          css={css`
            flex-grow: 1;
            display: flex;
            justify-content: flex-end;
          `}
        >
          {dockbar}
        </div>
      </div>
      <div
        css={css`
          grid-area: result;
          overflow-y: auto;
        `}
      >
        {result}
      </div>
      <div
        css={css`
          grid-area: detail;
          overflow-y: auto;
          background-color: ${myTheme.backgroundColor2};
        `}
      >
        {detail}
      </div>
    </div>
  );
}
