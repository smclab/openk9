import React from "react";
import { OpenK9 } from "./embeddable/entry";
import { css } from "styled-components/macro";
import { Logo } from "./components/Logo";
import "./index.css";
import "./app.css";
import { MaintenancePage } from "./components/MaintenancePage";
import "./ScrollBar.css";
import { FilterHorizontalSvg } from "./svgElement/FilterHorizontalSvg";
import "./components/dataRangePicker.css";
import { CalendarMobileSvg } from "./svgElement/CalendarMobileSvg";
import moment from "moment";
import { DeleteLogo } from "./components/DeleteLogo";
import { useTranslation } from "react-i18next";
import { ChangeLanguage } from "./components/ChangeLanguage";
export const openk9 = new OpenK9({
  enabled: true,
  searchAutoselect: false,
  searchReplaceText: false,
});

export function App() {
  const serviceStatus = useServiceStatus();
  if (serviceStatus === "down") {
    return <MaintenancePage />;
  }
  const [isVisibleFilters, setIsVisibleFilters] = React.useState(false);
  const [isVisibleSearchMobile, setIsVisibleSearchMobile] =
    React.useState(false);
  const [isVisibleCalendar, setIsVisibleCalendar] = React.useState(false);

  React.useState(false);
  const handleClick = (event: any) => {
    const elementWidth = window.innerWidth;
    if (elementWidth < 480) {
      setIsVisibleSearchMobile(true);
    }
  };
  const [startDate, setStartDate] = React.useState<any | null>(null);
  const [endDate, setEndDate] = React.useState<any | null>(null);
  const [focusedInput, setFocusedInput] = React.useState(null);
  const [isCLickReset, setIsClickReset] = React.useState(false);

  const { t } = useTranslation();

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
          overflow: hidden;
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
          css={css`
            flex-grow: 1;
            display: flex;
            gap: 10px;
            justify-content: flex-end;
            align-items: center;
          `}
        >
          <div
            className="openk9-navbar-login"
            ref={(element) => openk9.updateConfiguration({ login: element })}
          ></div>
          <div
            className="openk9-navbar-change-language"
            ref={(element) =>
              openk9.updateConfiguration({ changeLanguage: element })
            }
          ></div>{" "}
        </div>
      </div>
      <div
        ref={(element) => openk9.updateConfiguration({ tabs: element })}
        className="openk9-container-tabs"
        css={css`
          grid-area: tabs;
          padding: 8px 16px 0px 0px;
          margin-bottom: -16px;
          @media (max-width: 480px) {
            display: none;
          }
        `}
      ></div>
      <div
        css={css`
          grid-area: search;
          padding: 16px 0px 16px 0px;
          @media (min-width: 377px) and (max-width: 480px) {
            padding-inline: 16px;
          }
        `}
      >
        <div
          css={css`
            display: flex;
            gap: 10px;
            width: 100%;
            justify-content: center;
            align-items: baseline;
            @media (max-width: 480px) {
              flex-direction: column;
            }
          `}
        >
          <div
            css={css`
              display: flex;
              gap: 18px;
              align-items: flex-end;
              width: 100%;
            `}
          >
            <div
              css={css`
                width: 100%;
              `}
              onClick={handleClick}
              className="openk9-update-configuration"
              ref={(element) =>
                openk9.updateConfiguration({
                  searchConfigurable: {
                    btnSearch: true,
                    isShowSyntax: true,
                    element,
                  },
                })
              }
            ></div>
            <button
              css={css`
                padding: 6px 10px;
                border: 1px solid var(--openk9-embeddable-search--border-color);
                background: white;
                border-radius: 50px;
                @media (min-width: 480px) {
                  display: none;
                }
              `}
              onClick={() => {
                setIsVisibleFilters(true);
              }}
            >
              <FilterHorizontalSvg />
            </button>
          </div>
          <div
            css={css`
              @media (max-width: 480px) {
                display: none;
              }
            `}
            className="openk9-update-configuration"
            ref={(element) =>
              openk9.updateConfiguration({
                dataRangePicker: {
                  element: element,
                  start: startDate,
                  end: endDate,
                },
              })
            }
          ></div>
          <div
            css={css`
              width: 100%;
              @media (min-width: 480px) {
                display: none;
              }
            `}
            className="openk9-update-configuration"
          >
            <div
              css={css`
                display: flex;
                background-color: white;
                border-radius: 50px;
                border: 1px solid #ced4da;
                justify-content: space-between;
                align-items: center;
              `}
            >
              <div
                css={css`
                  padding: ${startDate === null ? " 7px 10px;" : "7px 0px"};
                  display: flex;
                  max-width: 100px;
                  gap: ${startDate === null ? "43px" : "23px"};
                `}
              >
                <CalendarMobileSvg />
                <div
                  onClick={() => {
                    setIsVisibleCalendar(true);
                  }}
                  css={css`
                    white-space: nowrap;
                  `}
                >
                  {startDate === null
                    ? t("start-day")
                    : moment(startDate).format("DD MMMM YYYY")}
                </div>
                <div
                  css={css`
                    border: 1px solid #ced4da;
                  `}
                ></div>
                <div
                  css={css`
                    white-space: nowrap;
                  `}
                  onClick={() => {
                    setIsVisibleCalendar(true);
                  }}
                >
                  {endDate === null
                    ? t("end-day")
                    : moment(endDate).format("DD MMMM YYYY")}
                </div>
              </div>
              {startDate !== null && (
                <div
                  onClick={() => {
                    setStartDate(null);
                    setEndDate(null);
                    setIsClickReset(true);
                  }}
                  css={css`
                    margin-right: 10px;
                  `}
                >
                  <DeleteLogo />
                </div>
              )}
            </div>
          </div>
        </div>
        <div
          className="openk9-container-active-filters"
          css={css`
            padding-top: 10px;
            @media (min-width: 480px) {
              display: none;
            }
          `}
        >
          <div
            className="openk9-filters-container openk9-box"
            ref={(element) =>
              openk9.updateConfiguration({ activeFilters: element })
            }
          />
        </div>
      </div>
      <div
        className="openk9-results-container openk9-box"
        ref={(element) =>
          openk9.updateConfiguration({
            filtersMobileLiveChange: {
              element: element,
              isVisible: isVisibleFilters,
              setIsVisible: setIsVisibleFilters,
              viewTabs: true,
            },
          })
        }
      ></div>
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
        className="openk9-results-container openk9-box"
        ref={(element) =>
          openk9.updateConfiguration({
            searchMobile: {
              search: element,
              isVisible: isVisibleSearchMobile,
              setIsVisible: setIsVisibleSearchMobile,
            },
          })
        }
      ></div>
      <div
        className="openk9-results-container openk9-box"
        ref={(element) =>
          openk9.updateConfiguration({
            calendarMobile: {
              element: element,
              isVisible: isVisibleCalendar,
              setIsVisible: setIsVisibleCalendar,
              startDate: startDate,
              setStartDate: setStartDate,
              endDate: endDate,
              setEndDate: setEndDate,
              focusedInput: focusedInput,
              setFocusedInput: setFocusedInput,
              isCLickReset: isCLickReset,
              setIsCLickReset: setIsClickReset,
            },
          })
        }
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
