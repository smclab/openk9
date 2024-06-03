import React from "react";
import { useQuery } from "react-query";
import { css } from "styled-components/macro";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { SearchToken, SortField } from "./client";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { useOpenK9Client } from "./client";
import _ from "lodash";
import { resetFilterCalendar } from "./DateRangePicker";
import { SelectionsAction } from "./useSelections";
import { Options } from "./SortResults";
import CustomSkeleton from "./Skeleton";
const OverlayScrollbarsComponentDockerFix = OverlayScrollbarsComponent as any; // for some reason this component breaks build inside docker

type TabsProps = {
  tabs: Array<Tab>;
  selectedTabIndex: number;
  onSelectedTabIndexChange(index: number): void;
  language: string;
  scrollMode?: boolean;
  onAction?(): void;
  speed?: number;
  distance?: number;
  step?: number;
  reset?: {
    filters: boolean;
    calendar: boolean;
    sort: boolean;
    search: boolean;
  };
  readMessageScreenReader?: boolean;
  textLabelScreenReader?: string;
  resetFilter: () => void;
  resetSort: () => void;
  selectionsDispatch: React.Dispatch<SelectionsAction>;
};
function Tabs({
  tabs,
  selectedTabIndex,
  onSelectedTabIndexChange,
  language,
  onAction,
  scrollMode = true,
  speed = 10,
  distance = 700,
  step = 30,
  reset,
  resetFilter,
  resetSort,
  selectionsDispatch,
  readMessageScreenReader,
  textLabelScreenReader,
}: TabsProps) {
  const elementRef = React.useRef(null);
  const [arrowDisable, setArrowDisable] = React.useState(true);
  const [arrowRightDisable, setArrowRightDisable] = React.useState(false);
  let scrollAmount = 0;
  const handleHorizantalScroll = ({
    element,
    step,
  }: {
    element: any;
    speed: number;
    distance: number;
    step: number;
  }) => {
    element.scrollLeft += step;
    scrollAmount += Math.abs(step);
    if (element.scrollLeft === 0) {
      setArrowDisable(true);
    } else {
      setArrowDisable(false);
    }
  };

  return !scrollMode ? (
    <div css={css`openk9-container-arrow-tabs`}>
      <h2
        id="title-tabs-openk9"
        className= {`${readMessageScreenReader && "visually-hidden title-tabs-openk9"} title-tabs-openk9`}
        css={css`
          border: 0;
          padding: 0;
          margin: 0;
          position: absolute !important;
          height: 1px;
          width: 1px;
          overflow: hidden;
          clip: rect(
            1px 1px 1px 1px
          ); /* IE6, IE7 - a 0 height clip, off to the bottom right of the visible 1px box */
          clip: rect(
            1px,
            1px,
            1px,
            1px
          ); /*maybe deprecated but we need to support legacy browsers */
          clip-path: inset(50%);
          white-space: nowrap;
        `}
      >
        {textLabelScreenReader || "filtri e argomenti"}
      </h2>
      <ul
        className="openk9-nav-container-tabs"
        ref={elementRef}
        role="list"
        css={css`
          display: flex;
          padding: 0;
          overflow-x: hidden;
          white-space: nowrap;
          width: 90vw;
          margin-left: 15px;
          list-style-type: none;
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
            <li role="listitem" aria-labelledby="title-tabs-openk9">
              <button
                className={`openk9-single-tab-container`}
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
                    if (reset) {
                      if (reset.filters) resetFilter();
                      if (reset?.calendar) resetFilterCalendar();
                      if (reset?.search)
                        selectionsDispatch({
                          type: "reset-search",
                        });
                      if (reset?.sort) resetSort();
                    }
                    if (onAction) onAction();
                  }}
                >
                  {tabTraslation.toUpperCase()}
                </span>
              </button>
            </li>
          );
        })}
      </ul>
    </div>
  ) : (
    <React.Fragment>
      <h2
        id="title-tabs-openk9"
        className="visually-hidden"
        css={css`
          border: 0;
          padding: 0;
          margin: 0;
          position: absolute !important;
          height: 1px;
          width: 1px;
          overflow: hidden;
          clip: rect(
            1px 1px 1px 1px
          ); /* IE6, IE7 - a 0 height clip, off to the bottom right of the visible 1px box */
          clip: rect(
            1px,
            1px,
            1px,
            1px
          ); /*maybe deprecated but we need to support legacy browsers */
          clip-path: inset(50%);
          white-space: nowrap;
        `}
      >
        {textLabelScreenReader || "filtri e argomenti"}
      </h2>
      <ul
        className="openk9-tabs-container-internal"
        role="list"
        css={css`
          display: flex;
          padding-top: 14px;
          padding: 8px 12px;
          width: fit-content;
          height: fit-content;
          gap: 16px;
          list-style-type: none;
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
            <li role="listitem" aria-labelledby="title-tabs-openk9">
              <button
                className="openk9-single-tab-container"
                key={index}
                tabIndex={0}
                css={css`
                  border: none;
                  background: none;
                `}
                onClick={() => {
                  onSelectedTabIndexChange(index);
                  if (reset) {
                    if (reset.filters) resetFilter();
                    if (reset?.calendar) resetFilterCalendar();
                    if (reset?.search)
                      selectionsDispatch({
                        type: "reset-search",
                      });
                    if (reset?.sort) resetSort();
                  }
                  if (onAction) onAction();
                }}
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
                >
                  {tabTraslation.toUpperCase()}
                </span>
              </button>
            </li>
          );
        })}
      </ul>
    </React.Fragment>
  );
}
export const TabsMemo = React.memo(Tabs);

export type Tab = {
  label: string;
  tokens: Array<SearchToken>;
  translationMap: { [key: string]: string };
  sortings: Options;
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

export default function TabsSkeleton() {
  return (
    <div
      css={css`
        padding: 8px 16px;
      `}
    >
      <CustomSkeleton
        height="32px"
        counter={3}
        position="row"
        width="100px"
        gap="10px"
      />
    </div>
  );
}
