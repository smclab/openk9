import { CreateLabel } from "./Filters";
import React from "react";
import { SearchToken } from "./client";
import { DeleteLogo } from "./DeleteLogo";
import { css } from "styled-components/macro";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { capitalize } from "lodash";
import { useTranslation } from "react-i18next";

const OverlayScrollbarsComponentDockerFix = OverlayScrollbarsComponent as any; // for some reason this component breaks build inside docker
export function ActiveFilter({
  searchQuery,
  onRemoveFilterToken,
  onConfigurationChange,
}: {
  searchQuery: SearchToken[];
  onRemoveFilterToken: (searchToken: SearchToken) => void;
  onConfigurationChange: ConfigurationUpdateFunction;
}) {
const { t } = useTranslation();
  if (searchQuery.length === 0) return null;
  const activeFilters = searchQuery.filter(
    (search) => "goToSuggestion" in search,
  ).length;
  if (activeFilters === 0) return null;
  return (
    <OverlayScrollbarsComponentDockerFix
      className="openk9-tabs-overlay-scrollbars"
      style={{
        position: "relative",
        overflowX: "auto",
        height: "40px",
      }}
    >
      <div
        className="openk9-active-filters-chop-container"
        css={css`
          display: flex;
          justify-content: space-between;
          width: 100%;
          position: absolute;
        `}
      >
        <div
          className="openk9-container-active-filters"
          css={css`
            display: flex;
            gap: 10px;
            width: 100%;
          `}
        >
          {searchQuery.map((selectToken, index) => {
            return (
              <React.Fragment>
                {"goToSuggestion" in selectToken && (
                  <div key={index} className="openk9-container-active-filter">
                    <button
                      className="openk9-active-filter"
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
                      `}
                    >
                      {capitalize(selectToken.values?.[0])}
                      <span
                        css={css`
                          cursor: pointer;
                        `}
                        onClick={() => onRemoveFilterToken(selectToken)}
                      >
                        <DeleteLogo
                          heightParam={10}
                          widthParam={10}
                          colorSvg="#BC0012"
                        />
                      </span>
                    </button>
                  </div>
                )}
              </React.Fragment>
            );
          })}
        </div>

        {searchQuery.length !== 0 && (
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
              @media (max-width: 480px) {
                display: none;
              }
            `}
            onClick={() => onConfigurationChange({ filterTokens: [] })}
          >
            {t("remove-filters")}
          </button>
        )}
      </div>
    </OverlayScrollbarsComponentDockerFix>
  );
}
