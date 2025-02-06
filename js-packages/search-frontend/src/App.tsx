import React from "react";
import { OpenK9 } from "./embeddable/entry";
import { css } from "styled-components/macro";
import { Logo } from "./components/Logo";
import "./index.css";
import "./app.css";
import { MaintenancePage } from "./components/MaintenancePage";
import "./ScrollBar.css";
import "./components/dataRangePicker.css";
import { CalendarMobileSvg } from "./svgElement/CalendarMobileSvg";
import { FilterHorizontalSvg } from "./svgElement/FilterHorizontalSvg";
import moment from "moment";
import { DeleteLogo } from "./components/DeleteLogo";
import { useTranslation } from "react-i18next";
import { debounce } from "lodash";
import { PreviewSvg } from "./svgElement/PreviewSvg";

export const openk9 = new OpenK9({
  enabled: true,
  searchAutoselect: true,
  searchReplaceText: true,
  isActiveSkeleton: true,
  memoryResults: false,
  useGenerativeApi: true,
});

export function App() {
  const serviceStatus = useServiceStatus();
  if (serviceStatus === "down") {
    return <MaintenancePage />;
  }

  const [isVisibleFilters, setIsVisibleFilters] = React.useState(false);
  const [numberOfResults, setNumberOfResults] = React.useState(false);
  const [isVisibleSearchMobile, setIsVisibleSearchMobile] =
    React.useState(false);
  const [isVisibleCalendar, setIsVisibleCalendar] = React.useState(false);
  const [startDate, setStartDate] = React.useState<any | null>(null);
  const [endDate, setEndDate] = React.useState<any | null>(null);
  const [focusedInput, setFocusedInput] = React.useState(null);
  const [isClickReset, setIsClickReset] = React.useState(false);
  const [isPanelVisible, setIsPanelVisible] = React.useState(true);
  const [searchText, setSearchText] = React.useState<string | null | undefined>(
    undefined,
  );
  React.useEffect(() => {
    document.body.classList.toggle(
      "no-scroll",
      isVisibleFilters || isVisibleSearchMobile || isVisibleCalendar,
    );
    return () => document.body.classList.remove("no-scroll");
  }, [isVisibleFilters, isVisibleSearchMobile, isVisibleCalendar]);

  const { t } = useTranslation();

  const handleClick = () => {
    if (window.innerWidth < 480) {
      setIsVisibleSearchMobile(true);
    }
  };
  const debouncedUpdateSearch = debounce((search) => {
    const text = search?.[0]?.values?.[0] || undefined;
    setSearchText(text);
  }, 200);

  const debouncedNumberOfResults = debounce((search) => {
    setNumberOfResults(search);
  }, 200);

  React.useEffect(() => {
    openk9.addEventListener("queryStateChange", (newConfig) => {
      debouncedUpdateSearch(newConfig.searchTokens);
      debouncedNumberOfResults(newConfig.numberOfResults);
    });
  }, [openk9]);

  React.useEffect(() => {
    if (isVisibleFilters || isVisibleSearchMobile || isVisibleCalendar) {
      document.body.classList.add("no-scroll");
    } else {
      document.body.classList.remove("no-scroll");
    }
    return () => {
      document.body.classList.remove("no-scroll");
    };
  }, [isVisibleFilters, isVisibleCalendar, isVisibleSearchMobile]);

  return (
    <div
      id="openk9-body"
      className="openk9-body"
      css={css`
        background-color: var(
          --openk9-embeddable-search--secondary-background-color
        );
        width:100vw;
        height: 100vh;
        display: grid;
        grid-column-gap: 30px;
        padding:  50px ;
        padding-block:0px;
        box-sizing: border-box;
        grid-template-columns: 1fr 2.5fr 1.8fr;
        grid-template-rows: auto auto auto auto 1fr;
        grid-template-areas:
          "dockbar dockbar dockbar"
          "search search search"
          "tabs tabs tabs"
          "filters panel panel"
          "filters result detail";
        padding: 20px;

        @media (max-width: 480px) {
          padding: 0;
          grid-template-columns: 1fr;
          grid-template-rows: auto auto auto auto 1fr;
          grid-template-areas:
            "dockbar"
            "search"
            "tabs"
            "panel"
            "result";
        }

        @media (min-width: 481px) and (max-width: 768px) {
         padding-block:0;
        padding-inline:20px;
          grid-template-columns: 1fr;
          grid-template-rows: auto auto auto auto 1fr;
          grid-template-areas:
            "dockbar"
            "search"
            "tabs"
            "panel"
            "result";
        }

        @media (min-width: 769px) and (max-width: 1024px) {
          grid-template-columns: 1fr 2fr;
          grid-template-rows: auto auto auto auto 1fr;
          grid-template-areas:
            "dockbar dockbar"
            "search search"
            "tabs tabs"
            "filters panel"
            "filters result";
            "result";

        }

         @media (min-width: 1024px) and (max-width: 1280px) {
        grid-template-columns: 1.2fr 2.3fr 1.8fr;
        grid-template-rows: auto auto auto auto 1fr;

        }
      `}
    >
      <div
        className="openk9-navbar"
        css={css`
          grid-area: dockbar;
          padding: 8px 50px;
          margin: 0 -50px;
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
          display: flex;
          align-items: center;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );

          @media (max-width: 480px) {
            margin: 0 0px;
            padding: 8px 20px;
          }
        `}
      >
        <div
          className="openk9-navbar-container-logo"
          css={css`
            font-size: 20px;
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
          />
          <div
            className="openk9-navbar-change-language"
            ref={(element) =>
              openk9.updateConfiguration({ changeLanguage: element })
            }
          />
        </div>
      </div>
      <div
        css={css`
          grid-area: search;
          padding-top: 16px;
          padding-bottom: 8px;
          @media (max-width: 480px) {
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

            @media (max-width: 768px) {
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
            onClick={handleClick}
            className="openk9-update-configuration-search"
            ref={(element) => openk9.updateConfiguration({ search: element })}
          />
          <div
            css={css`
              width: 100%;
              display: flex;
              gap: 1%;
              flex-wrap: wrap;

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
                padding: 8px 12px;
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
              `}
            >
              <div
                css={css`
                  display: flex;
                  align-items: center;
                  gap: 12px;
                  flex: 1;
                `}
                onClick={() => setIsVisibleCalendar(true)}
              >
                <CalendarMobileSvg />
                <div
                  css={css`
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    flex: 1;
                  `}
                  onClick={() => setIsVisibleCalendar(true)}
                >
                  <div
                    css={css`
                      white-space: nowrap;
                      color: #495057;
                      font-size: 14px;
                    `}
                  >
                    {startDate === null
                      ? "Seleziona una data"
                      : moment(startDate).format("DD MMM YYYY")}
                  </div>
                  {endDate !== null && (
                    <div
                      css={css`
                        height: 20px;
                        width: 1px;
                        background-color: #ced4da;
                      `}
                    />
                  )}
                  <div
                    css={css`
                      white-space: nowrap;
                      color: #495057;
                      font-size: 14px;
                    `}
                  >
                    {endDate === null
                      ? ""
                      : moment(endDate).format("DD MMM YYYY")}
                  </div>
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
                    margin-left: 10px;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                  `}
                >
                  <DeleteLogo />
                </div>
              )}
            </div>
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
              onClick={() => setIsVisibleFilters(true)}
            >
              <FilterHorizontalSvg />
            </button>
            {searchText !== undefined && (
              <div
                css={css`
                  padding-block: 8px;
                  @media (min-width: 480px) {
                    display: none;
                  }
                `}
              >
                {/* <button
                  onClick={() => setIsPanelVisible(!isPanelVisible)}
                  css={css`
                    justify-self: center;
                    border: 1px solid red;
                    border-radius: 20px;
                    background: red;
                    border: 1px solid red;
                    border-radius: 20px;
                    color: white;
                    gap: 10px;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    padding: 6px 10px;
                    @media (min-width: 480px) {
                      display: none;
                    }
                  `}
                >
                  {isPanelVisible ? (
                    <>
                      Chiudi <Logo />
                    </>
                  ) : (
                    <>
                      <Logo />
                    </>
                  )}
                </button> */}
              </div>
            )}
          </div>
        </div>
      </div>

      <div
        ref={(element) => openk9.updateConfiguration({ tabs: element })}
        className="openk9-container-tabs"
        css={css`
          grid-area: tabs;
          overflow: auto;
          padding-top: 8px;
          padding-bottom: 16px;
          height: max-content;
          @media (max-width: 480px) {
            display: none;
          }
        `}
      />

      <div
        className="openk9-filters-mobile-container openk9-box"
        ref={(element) =>
          openk9.updateConfiguration({
            filtersMobileLiveChange: {
              element,
              isVisible: isVisibleFilters,
              setIsVisible: setIsVisibleFilters,
              viewTabs: true,
            },
          })
        }
      />
      <div
        className="openk9-detail-container-title box-title"
        css={css`
          grid-area: filters;
          width: 100%;
          background: #fafafa;
          display: flex;
          flex-direction: column;
          gap: 3px;
          box-sizing: border-box;
          border-radius: 8px;
          height: auto;
          overflow: auto;
          @media (max-width: 767px) {
            display: none;
          }
        `}
      >
        <div
          className="openk9-icon-and-title-filters"
          css={css`
            display: flex;
            gap: 5px;
            padding: 8px 16px;
            margin-top: 5px;
          `}
        >
          <div>
            <PreviewSvg />
          </div>
          <h2
            id="title-preview-openk9"
            tabIndex={0}
            className="openk9-detail-class-title"
            css={css`
              font-style: normal;
              font-weight: 700;
              font-size: 18px;
              height: 18px;
              line-height: 22px;
              align-items: center;
              color: #3f3f46;
              margin: 0;
            `}
          >
            {t("filters")}
          </h2>
        </div>
        <div
          css={css`
            padding: 16px;
          `}
          ref={(element) =>
            openk9.updateConfiguration({
              dataRangePickerVertical: { element },
            })
          }
        ></div>
        <div
          className="openk9-filters-container openk9-box"
          ref={(element) =>
            openk9.updateConfiguration({
              filtersConfigurable: { element },
            })
          }
          css={css`
            display: flex;
            height: max-content;
            flex-direction: column-reverse;
            background-color: var(
              --openk9-embeddable-search--primary-background-color
            );
            border-radius: 8px;

            @media (max-width: 480px) {
              display: none;
            }

            @media (min-width: 481px) and (max-width: 768px) {
              display: none;
            }
          `}
        ></div>
      </div>

      {searchText !== undefined && (
        <div
          css={css`
            grid-area: panel;
            display: flex;
            flex-direction: column;
            @media (max-width: 480px) {
              margin-inline: 5%;
            }
          `}
        >
          <div
            css={css`
              background: white;
              display: flex;
              justify-content: flex-end;
              border-top-left-radius: 10px;
              border-top-right-radius: 10px;
              border-bottom-left-radius: ${!isPanelVisible ? "10px" : "unset"};
              border-bottom-right-radius: ${!isPanelVisible ? "10px" : "unset"};
            `}
          >
            <button
              onClick={() => setIsPanelVisible(!isPanelVisible)}
              css={css`
                justify-self: center;
                border: 1px solid red;
                background: red;
                border: 1px solid red;
                border-radius: 8px;
                padding: 8px;
                color: white;
                gap: 10px;
                cursor: pointer;
                display: flex;
                align-items: center;
                margin: 10px;
                @media (max-width: 768px) {
                  display: none;
                }
              `}
            >
              {isPanelVisible ? (
                <>
                  Chiudi Risposta Generata <Logo />
                </>
              ) : (
                <>
                  Apri Risposta Generata <Logo />
                </>
              )}
            </button>
          </div>
          <div
            ref={(element) =>
              openk9.updateConfiguration({
                generateResponse: element,
              })
            }
            css={css`
              color: black;
              display: ${isPanelVisible ? "block" : "none"};
            `}
          ></div>
        </div>
      )}

      <div
        className="openk9-results-container openk9-box"
        ref={(element) =>
          openk9.updateConfiguration({
            resultList: {
              element,
              changeOnOver: false,
            },
          })
        }
        css={css`
          grid-area: result;
          margin-top: ${searchText !== undefined ? "10px" : "unset"};
          overflow: auto;
          display: flex;
          flex-direction: column;
          gap: 10px;
          height: auto;

          @media (max-width: 480px) {
            padding-inline: 16px;
            gap: 10px;
          }
        `}
      >
        {
          <div
            css={css`
              display: flex;
              justify-content: space-between;
              background-color: white;
              align-items: center;
              border-radius: 8px;
              @media (max-width: 480px) {
                flex-direction: column;
                align-items: flex-start;
                padding: 16px;
              }
            `}
          >
            <div
              css={css`
                padding: 16px;
                font-weight: 700;
                width: 100%;
                @media (max-width: 480px) {
                  padding: 0;
                  padding-bottom: 8px;
                }
              `}
            >
              Numero di risultati:
              <span
                css={css`
                  color: var(--openk9-embeddable-search--primary-color);
                  margin-left: 5px;
                `}
              >
                {numberOfResults}
              </span>
            </div>
            <div
              ref={(element) =>
                openk9.updateConfiguration({
                  sortResults: { element },
                })
              }
            ></div>
          </div>
        }
      </div>

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
      />

      <div
        className="openk9-results-container openk9-box"
        ref={(element) =>
          openk9.updateConfiguration({
            calendarMobile: {
              element,
              isVisible: isVisibleCalendar,
              setIsVisible: setIsVisibleCalendar,
              startDate,
              setStartDate,
              endDate,
              setEndDate,
              focusedInput,
              setFocusedInput,
              isCLickReset: isClickReset,
              setIsCLickReset: setIsClickReset,
            },
          })
        }
      />

      <div
        className="openk9-preview-container openk9-box"
        ref={(element) => openk9.updateConfiguration({ details: element })}
        css={css`
          grid-area: detail;
          overflow-y: auto;
          overflow-x: hidden;
          background-color: var(
            --openk9-embeddable-search--primary-background-color
          );
          height: auto;
          border-radius: 8px;
          border: 1px solid var(--openk9-embeddable-search--border-color);
          margin-top: ${searchText !== undefined ? "20px" : "unset"};

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
      />

      <div
        ref={(element) => openk9.updateConfiguration({ detailMobile: element })}
      />
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
