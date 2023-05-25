import React from "react";
import { OpenK9 } from "./embeddable/entry";
import { css } from "styled-components/macro";
import { Logo } from "./components/Logo";
import "./index.css";
import "./app.css";
import { MaintenancePage } from "./components/MaintenancePage";
import "./ScrollBar.css";

export const openk9 = new OpenK9({
  enabled: true,
  searchAutoselect: true,
  searchReplaceText: true,
});

export function App() {
  const serviceStatus = useServiceStatus();
  if (serviceStatus === "down") {
    return <MaintenancePage />;
  }
  return (
    <div
      className="openk9-body"
      css={css`
        background-color: var(
          --openk9-embeddable-search--secondary-background-color
        );
        width: 100vw;
        height: 100vh;
        display: grid;
        grid-column-gap: 30px;
        padding-bottom: 30px;
        padding-left: 50px;
        padding-right: 50px;
        box-sizing: border-box;
        @media (max-width: 480px) {
          padding-left: 0px;
          padding-right: 0px;
          grid-template-columns: 1fr;
          grid-template-rows: auto auto auto 1fr;
          grid-template-areas:
            "dockbar"
            "tabs"
            "search"
            "result";
        }
        @media (min-width: 481px) and (max-width: 768px) {
          grid-template-columns: 1fr;
          grid-template-rows: auto auto auto 1fr;
          grid-template-areas:
            "dockbar"
            "tabs"
            "search"
            "result";
        }
        @media (min-width: 769px) and (max-width: 1024px) {
          grid-template-columns: 1fr 2fr;
          grid-template-rows: auto auto auto 1fr;
          grid-template-areas:
            "dockbar dockbar"
            "tabs tabs"
            "search search"
            "filters result";
        }
        grid-template-columns: 1fr 2.5fr 1.8fr;
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
        className="openk9-container-tabs"
        css={css`
          grid-area: tabs;
          padding: 8px 16px 0px 0px;
          margin-bottom: -16px;
        `}
      ></div>
      <div
        className="openk9-navbar"
        css={css`
          grid-area: dockbar;
          padding: 8px 50px;
          margin: 0px -50px;
          box-shadow: 0 1px 2px 0 rgb(0 0 0 / 10%);
          display: flex;
          align-items: center;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );
          @media (max-width: 480px) {
            margin: 0px -30px;
          }
        `}
      >
        <div
          className="openk9-navbar-container-logo"
          css={css`
            font-size: 20;
            color: #1e1c21;
            display: flex;
            align-items: center;
          `}
        >
          <span
            className="openk9-navbar-logo"
            css={css`
              color: var(--openk9-embeddable-search--primary-color);
              margin-right: 8px;
            `}
          >
            <Logo size={32} />
          </span>
          <span>Open</span>
          <span
            className="openk9-navbar-name-logo"
            css={css`
              font-weight: 700;
            `}
          >
            K9
          </span>
        </div>
        <div
          className="openk9-navbar-login"
          ref={(element) => openk9.updateConfiguration({ login: element })}
          css={css`
            flex-grow: 1;
            display: flex;
            justify-content: flex-end;
          `}
        ></div>
      </div>
      <div
        className="openk9-filters-container openk9-box"
        ref={(element) => openk9.updateConfiguration({ filters: element })}
        css={css`
          grid-area: filters;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );
          border-radius: 8px;
          border: 1px solid var(--openk9-embeddable-search--border-color);
          @media (max-width: 480px) {
            display: none;
          }
          @media (min-width: 481px) and (max-width: 768px) {
            display: none;
          }
        `}
      ></div>
      <div
        className="openk9-results-container openk9-box"
        ref={(element) => openk9.updateConfiguration({ results: element })}
        css={css`
          grid-area: result;
          overflow-y: auto;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );
          border-radius: 8px;
          border: 1px solid var(--openk9-embeddable-search--border-color);
        `}
      ></div>
      <div
        className="openk9-preview-container openk9-box"
        ref={(element) => openk9.updateConfiguration({ details: element })}
        css={css`
          grid-area: detail;
          overflow-y: auto;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );
          border-radius: 8px;
          border: 1px solid var(--openk9-embeddable-search--border-color);
          @media (max-width: 480px) {
            display: none;
          }
          @media (min-width: 481px) and (max-width: 768px) {
            display: none;
          }
          @media (min-width: 769px) and (max-width: 1024px) {
            display: none;
          }
        `}
      ></div>
      <div
        className="openk9-update-configuration"
        ref={(element) => openk9.updateConfiguration({ search: element })}
        css={css`
          grid-area: search;
          padding: 16px 0px 16px 0px;
        `}
      ></div>
      <div
        className="openk9-results-container openk9-box"
        ref={(element) => openk9.updateConfiguration({ detailMobile: element })}
      ></div>
    </div>
  );
}

function useServiceStatus() {
  const [serviceStatus, setServiceStatus] = React.useState<"up" | "down">("up");
  React.useEffect(() => {
    openk9.client.getServiceStatus().then(setServiceStatus);
  }, []);
  return serviceStatus;
}
