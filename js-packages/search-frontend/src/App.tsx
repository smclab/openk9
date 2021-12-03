import React from "react";
import "./index.css";
import { OpenK9 } from "./embeddable/entry";
import { css } from "styled-components/macro";
import { myTheme } from "./utils/myTheme";
import { Logo } from "./components/Logo";

export function App() {
  return (
    <div
      css={css`
        width: 100vw;
        height: 100vh;
        display: grid;
        grid-template-columns: 50% 50%;
        grid-template-rows: auto auto auto 1fr;
        grid-template-areas:
          "dockbar dockbar"
          "tabs tabs"
          "search search"
          "result detail";
      `}
    >
      <div
        ref={(element) => (OpenK9.search = element)}
        css={css`
          grid-area: search;
          background-color: ${myTheme.backgroundColor2};
          padding: 16px;
        `}
      ></div>
      <div
        ref={(element) => (OpenK9.tabs = element)}
        css={css`
          grid-area: tabs;
          background-color: ${myTheme.backgroundColor2};
          padding: 8px 16px 0px 16px;
          margin-bottom: -16px;
        `}
      ></div>
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
          ref={(element) => (OpenK9.login = element)}
          css={css`
            flex-grow: 1;
            display: flex;
            justify-content: flex-end;
          `}
        ></div>
      </div>
      <div
        ref={(element) => (OpenK9.results = element)}
        css={css`
          grid-area: result;
          overflow-y: auto;
        `}
      ></div>
      <div
        ref={(element) => (OpenK9.details = element)}
        css={css`
          grid-area: detail;
          overflow-y: auto;
          background-color: ${myTheme.backgroundColor2};
        `}
      ></div>
    </div>
  );
}
