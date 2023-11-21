import React from "react";
import { useQuery } from "react-query";
import { css } from "styled-components/macro";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { SearchToken } from "./client";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { useOpenK9Client } from "./client";
import _ from "lodash";
import { resetFilterCalendar } from "./DateRangePicker";
const OverlayScrollbarsComponentDockerFix = OverlayScrollbarsComponent as any; // for some reason this component breaks build inside docker

type TabsProps = {
  tabs: Array<Tab>;
  selectedTabIndex: number;
  onSelectedTabIndexChange(index: number): void;
  onConfigurationChange: ConfigurationUpdateFunction;
  language: string;
  scrollMode?: boolean;
  onAction?(): void;
  speed?: number;
  distance?: number;
  step?: number;
};
function Tabs({
  tabs,
  selectedTabIndex,
  onSelectedTabIndexChange,
  onConfigurationChange,
  language,
  onAction,
  scrollMode = true,
  speed=25,
  distance=200,
  step=35,
}: TabsProps) {
  const elementRef = React.useRef(null);
  const [arrowDisable, setArrowDisable] = React.useState(true);
  const [arrowRightDisable, setArrowRightDisable] = React.useState(false);

  const handleHorizantalScroll = ({
    element,
    speed,
    distance,
    step,
  }: {
    element: any;
    speed: number;
    distance: number;
    step: number;
  }) => {
    let scrollAmount = 0;
    const slideTimer = setInterval(() => {
      element.scrollLeft += step;
      scrollAmount += Math.abs(step);

      if (scrollAmount >= distance) {
        clearInterval(slideTimer);
      }
      if (element.scrollLeft === 0) {
        setArrowDisable(true);
      } else {
        setArrowDisable(false);
      }
      if (element.scrollLeft > 91) {
        setArrowRightDisable(true);
      } else {
        setArrowRightDisable(false);
      }
    }, speed);
  };

  return !scrollMode ? (
    <div css={css`openk9-container-arrow-tabs`}>
      <nav
        className="openk9-nav-container-tabs"
        ref={elementRef}
        css={css`
          display: flex;
          overflow-x: hidden;
          white-space: nowrap;
          width: 90vw;
          margin-left: 15px;
        `}
      >
        <div
          className={
            "openk9-nav-container-left-button " + arrowRightDisable
              ? "disabled"
              : "not-disabled"
          }
          css={css`
            position: absolute;
          `}
        >
          {!arrowDisable && (
            <button
              className="openk9-button-left-tabs"
              css={css`
                padding: 8px 12px;
                border: 1px solid #80808082;
                background: #dbdbdb;
                opacity: 0.9;
                border-radius: 20px;
              `}
              onClick={() => {
                handleHorizantalScroll({
                  element: elementRef.current,
                  distance,
                  speed,
                  step: -step,
                });
              }}
            >
              {"<"}
            </button>
          )}
        </div>
        <div
          className={
            "openk9-nav-container-right-button " + arrowRightDisable
              ? "disabled"
              : "not-disabled"
          }
          css={css`
            position: absolute;
            right: 8px;
          `}
        >
          {!arrowRightDisable && (
            <button
              className="openk9-button-right-tabs"
              css={css`
                padding: 8px 12px;
                border: 1px solid #80808082;
                background: #dbdbdb;
                opacity: 0.9;
                border-radius: 20px;
                right: 0;
              `}
              onClick={() => {
                handleHorizantalScroll({
                  element: elementRef.current,
                  distance,
                  speed,
                  step,
                });
              }}
            >
              {">"}
            </button>
          )}
        </div>
        {tabs.map((tab, index) => {
          const isSelected = index === selectedTabIndex;
          const tabTraslation = translationTab({
            language: language,
            tabLanguages: tab.translationMap,
            defaultValue: tab.label,
          });
          return (
            <button
              className="openk9-single-tab-container"
              key={index}
              tabIndex={0}
              css={css`
                border: none;
                background: none;
              `}
            >
              <span
                key={index}
                className={
                  "openk9-single-tab " +
                  (isSelected ? "openk9-active-tab" : "openk9-not-active")
                }
                css={css`
                  white-space: nowrap;
                  padding: 8px 12px;
                  background: ${isSelected
                    ? "var(--openk9-embeddable-search--primary-background-tab-color)"
                    : "var(--openk9-embeddable-search--secondary-background-tab-color)"};
                  border-radius: 8px;
                  font: Helvetica Neue LT Std;
                  font-style: normal;
                  display: block;
                  color: ${isSelected
                    ? "var(--openk9-embeddable-search--primary-background-color)"
                    : "var(--openk9-embeddable-tabs--primary-color)"};
                  ${isSelected
                    ? "var(--openk9-embeddable-search--active-color)"
                    : "transparent"};
                  cursor: ${isSelected ? "" : "pointer"};
                  user-select: none;
                  :hover {
                    ${isSelected ? "" : "text-decoration: underline;"}
                  }
                `}
                onClick={() => {
                  onSelectedTabIndexChange(index);
                  onConfigurationChange({ filterTokens: [] });
                  if (onAction) onAction();
                  resetFilterCalendar();
                }}
              >
                {tabTraslation.toUpperCase()}
              </span>
            </button>
          );
        })}
      </nav>
    </div>
  ) : (
    <OverlayScrollbarsComponentDockerFix
      className="openk9-tabs-overlay-scrollbars"
      style={{
        position: "relative",
        overflowX: "auto",
        overflowY: "none",
        height: "60px",
      }}
    >
      <nav
        className="openk9-tabs-container-internal"
        css={css`
          position: absolute;
          display: flex;
          padding-top: 14px;
          padding: 8px 12px;
          width: fit-content;
          height: fit-content;
          gap: 16px;
          @media (max-width: 480px) {
            gap: 10px;
          }
        `}
      >
        {tabs.map((tab, index) => {
          const isSelected = index === selectedTabIndex;
          const tabTraslation = translationTab({
            language: language,
            tabLanguages: tab.translationMap,
            defaultValue: tab.label,
          });
          return (
            <button
              className="openk9-single-tab-container"
              key={index}
              tabIndex={0}
              css={css`
                border: none;
                background: none;
              `}
            >
              <span
                key={index}
                className={
                  "openk9-single-tab " +
                  (isSelected ? "openk9-active-tab" : "openk9-not-active")
                }
                css={css`
                  white-space: nowrap;
                  padding: 8px 12px;
                  background: ${isSelected
                    ? "var(--openk9-embeddable-search--primary-background-tab-color)"
                    : "var(--openk9-embeddable-search--secondary-background-tab-color)"};
                  border-radius: 8px;
                  font: Helvetica Neue LT Std;
                  font-style: normal;
                  display: block;
                  color: ${isSelected
                    ? "var(--openk9-embeddable-search--primary-background-color)"
                    : "var(--openk9-embeddable-tabs--primary-color)"};
                  ${isSelected
                    ? "var(--openk9-embeddable-search--active-color)"
                    : "transparent"};
                  cursor: ${isSelected ? "" : "pointer"};
                  user-select: none;
                  :hover {
                    ${isSelected ? "" : "text-decoration: underline;"}
                  }
                `}
                onClick={() => {
                  onSelectedTabIndexChange(index);
                  onConfigurationChange({ filterTokens: [] });
                  if (onAction) onAction();
                  resetFilterCalendar();
                }}
              >
                {tabTraslation.toUpperCase()}
              </span>
            </button>
          );
        })}
      </nav>
    </OverlayScrollbarsComponentDockerFix>
  );
}
export const TabsMemo = React.memo(Tabs);

export type Tab = {
  label: string;
  tokens: Array<SearchToken>;
  translationMap: { [key: string]: string };
};

export function useTabTokens(): Array<Tab> {
  const client = useOpenK9Client();
  const tabsByVirtualHostQuery = useQuery(
    ["tabs-by-virtualhost"] as const,
    ({ queryKey }) => {
      return client.getTabsByVirtualHost();
    },
  );
  return tabsByVirtualHostQuery.data ?? [];
}

export function translationTab({
  language,
  tabLanguages,
  defaultValue,
}: {
  language: string;
  tabLanguages: { [key: string]: string };
  defaultValue: string;
}): string {
  const desiredKey = "label." + language;
  if (tabLanguages && tabLanguages.hasOwnProperty(desiredKey)) {
    return tabLanguages[desiredKey];
  }
  return defaultValue;
}

//utile nel caso bisogna tradurre i tab da inviare nella search query
export function translationTabValue({
  language,
  tabLanguages,
  defaultValue,
  selectedTabIndex,
}: {
  language: string;
  tabLanguages: { [key: string]: string };
  defaultValue: SearchToken[];
  selectedTabIndex: number;
}): SearchToken[] {
  if (selectedTabIndex === 0) return defaultValue;
  const desiredKey = "label." + language;
  let tabClick: SearchToken[] | null = null;
  if (tabLanguages && tabLanguages.hasOwnProperty(desiredKey)) {
    tabClick = _.cloneDeep(defaultValue);
    tabClick[0] = {
      ...tabClick[0],
      values: [tabLanguages[desiredKey]],
    };
  }

  return tabClick || defaultValue;
}
