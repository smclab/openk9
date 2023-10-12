import { CreateLabel } from "./Filters";
import React from "react";
import { SearchToken } from "./client";
import { DeleteLogo } from "./DeleteLogo";
import { css } from "styled-components/macro";
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
    <div style={{ display: "flex", alignItems: "center" }}>
      <div
        className="openk9-tabs-overlay-scrollbars"
        style={{
          overflowX: "auto",
          height: "50px",
          width: "91%",
        }}
      >
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
            `}
          >
            {searchQuery.map((selectToken, index) => {
              return (
                <React.Fragment key={index}>
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
            onClick={() => onConfigurationChange({ filterTokens: [] })}
          >
            {t("remove-filters")}
          </button>
        </div>
      )}
    </div>
  );
}
