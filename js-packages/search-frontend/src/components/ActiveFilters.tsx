import { CreateLabel } from "./Filters";
import React from "react";
import { SearchToken } from "./client";
import { DeleteLogo } from "./DeleteLogo";
import { css } from "styled-components";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { capitalize } from "lodash";
import { useTranslation } from "react-i18next";
import { height } from "@fortawesome/free-solid-svg-icons/faFileAlt";

const OverlayScrollbarsComponentDockerFix = OverlayScrollbarsComponent as any; // for some reason this component breaks build inside docker
export function ActiveFilter({
  searchQuery,
  onRemoveFilterToken,
  onConfigurationChange,
  actioneRemoveFilters,
  callbackRemoveFilter,
}: {
  searchQuery: SearchToken[];
  onRemoveFilterToken: (searchToken: SearchToken) => void;
  onConfigurationChange: ConfigurationUpdateFunction;
  actioneRemoveFilters?: () => void;
  callbackRemoveFilter?: () => void;
}) {
  const { t } = useTranslation();
  if (searchQuery.length === 0) return null;
  const filterSearchQuery = searchQuery.filter(
    (search) => "goToSuggestion" in search,
  );
  const activeFilters = filterSearchQuery.length;
  if (activeFilters === 0) return null;
  return (
    <div>
      <h2
        className="openk9-filters-active-information"
        css={css`
          @media (max-width: 769px) {
            font-size: 20px;
          }
        `}
      >
        {t("active-filters")}:{" "}
        <span
          className="openk9-number-filters-active"
          css={css`
            color: #d6012e;
          `}
        >
          {countTotalValues(filterSearchQuery)}
        </span>
      </h2>
      <div
        className="openk9-active-container-box"
        css={css`
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          justify-content: space-between;
        `}
      >
        <div className="openk9-tabs-overlay-scrollbars">
          <div
            className="openk9-active-filters-chop-container"
            css={css`
              display: flex;
            `}
          >
            <div
              className="openk9-container-active-filters"
              css={css`
                display: flex;
                gap: 10px;
                width: 100%;
                flex-wrap: wrap;
              `}
            >
              {searchQuery.map((selectToken, index) => {
                if ("goToSuggestion" in selectToken) {
                  return selectToken.values.map((value, valueIndex) => (
                    <React.Fragment key={valueIndex}>
                      <div className="openk9-container-active-filter">
                        <button
                          className="openk9-active-filter"
                          onClick={() => {
                            onRemoveFilterToken({
                              ...selectToken,
                              values: [value],
                            });
                            if (callbackRemoveFilter) callbackRemoveFilter();
                          }}
                          css={css`
                            border: 1px solid red;
                            background: inherit;
                            padding: 3px 12px;
                            border-radius: 50px;
                            color: #bc0012;
                            font-weight: 700;
                            line-height: 24px;
                            display: flex;
                            align-items: center;
                            gap: 10px;
                            white-space: nowrap;
                            cursor: pointer;
                          `}
                        >
                          <span
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
                            rimuovi il filtro
                          </span>
                          {capitalize(value)}
                          <span>
                            <DeleteLogo
                              heightParam={10}
                              widthParam={10}
                              colorSvg="#BC0012"
                            />
                          </span>
                        </button>
                      </div>
                    </React.Fragment>
                  ));
                }
                return null;
              })}
            </div>
          </div>
        </div>
        {searchQuery.length !== 0 && (
          <div>
            <button
              className="openk9-active-filter"
              css={css`
                border: none;
                background: inherit;
                padding: 3px 12px;
                border-radius: 50px;
                text-decoration: underline;
                line-height: 24px;
                display: flex;
                align-items: center;
                gap: 10px;
                white-space: nowrap;
                cursor: pointer;
              `}
              onClick={() => {
                onConfigurationChange({ filterTokens: [] });
                if (actioneRemoveFilters) actioneRemoveFilters();
              }}
            >
              {t("remove-filters")}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

function countTotalValues(arr: SearchToken[]): number {
  return arr.reduce((total, token) => {
    if (token.values && token.values.length > 0) {
      total += token.values.length;
    }
    return total;
  }, 0);
}
