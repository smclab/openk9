import React from "react";
import ReactDOMClient from "react-dom/client";
import { OpenK9 } from "@openk9/search-frontend";
import { css } from "styled-components/macro";
import "./index.css";
import "./app.css";

import { plugin } from "openk9-plugin-to-be-tested";

const openk9 = new OpenK9({
  enabled: true,
  searchAutoselect: true,
});
openk9.client.debugPluginsOverride = [plugin];

const element = document.getElementById("root") as HTMLElement;
const root = ReactDOMClient.createRoot(element);
root.render(<App />);

function App() {
  return (
    <div
      css={css`
        width: 100vw;
        height: 100vh;
        background-color: var(
          --openk9-embeddable-search--secondary-background-color
        );
        display: grid;
        grid-column-gap: 16px;
        padding-bottom: 16px;
        padding-left: 16px;
        padding-right: 16px;
        box-sizing: border-box;
        @media (min-width: 320px) and (max-width: 480px) {
          grid-template-columns: 1fr;
          grid-template-rows: auto auto auto 1fr 0px 0px;
          grid-template-areas:
            "dockbar"
            "tabs"
            "search"
            "result"
            "filters"
            "detail";
        }
        @media (min-width: 481px) and (max-width: 768px) {
          grid-template-columns: 1fr;
          grid-template-rows: auto auto auto 1fr 0px 0px;
          grid-template-areas:
            "dockbar"
            "tabs"
            "search"
            "result"
            "filters"
            "detail";
        }
        @media (min-width: 769px) and (max-width: 1024px) {
          grid-template-columns: 1fr 2fr;
          grid-template-rows: auto auto auto 1fr 0px;
          grid-template-areas:
            "dockbar dockbar"
            "tabs tabs"
            "search search"
            "filters result"
            "detail detail";
        }
        grid-template-columns: 1fr 2fr 2fr;
        grid-template-rows: auto auto auto 1fr;
        grid-template-areas:
          "dockbar dockbar dockbar"
          "tabs tabs tabs"
          "search search search"
          "filters result detail";
      `}
    >
      <div
        ref={(element) => openk9.updateConfiguration({ tabs: element })}
        css={css`
          grid-area: tabs;
          padding: 8px 16px 0px 16px;
          margin-bottom: -16px;
        `}
      ></div>
      <div
        css={css`
          grid-area: dockbar;
          padding: 8px 16px;
          margin: 0px -16px;
          box-shadow: 0 1px 2px 0 rgb(0 0 0 / 10%);
          display: flex;
          align-items: center;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );
        `}
      >
        <div
          css={css`
            font-size: 20;
            color: #1e1c21;
            display: flex;
            align-items: center;
          `}
        >
          <span
            css={css`
              color: var(--openk9-embeddable-search--primary-color);
              margin-right: 8px;
            `}
          ></span>
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
          ref={(element) => openk9.updateConfiguration({ login: element })}
          css={css`
            flex-grow: 1;
            display: flex;
            justify-content: flex-end;
          `}
        ></div>
      </div>
      <div
        ref={(element) => openk9.updateConfiguration({ filters: element })}
        css={css`
          grid-area: filters;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );
          border-radius: 4px;
          border: 1px solid var(--openk9-embeddable-search--border-color);
        `}
      ></div>
      <div
        ref={(element) => openk9.updateConfiguration({ results: element })}
        css={css`
          grid-area: result;
          overflow-y: auto;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );
          border-radius: 4px;
          border: 1px solid var(--openk9-embeddable-search--border-color);
        `}
      ></div>
      <div
        ref={(element) => openk9.updateConfiguration({ details: element })}
        css={css`
          grid-area: detail;
          overflow-y: auto;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );
          border-radius: 4px;
          border: 1px solid var(--openk9-embeddable-search--border-color);
        `}
      ></div>
      <div
        ref={(element) => openk9.updateConfiguration({ search: element })}
        css={css`
          grid-area: search;
          padding: 16px 0px 16px 0px;
        `}
      ></div>
    </div>
  );
}
