import { CreateLabel } from "./Filters";
import React from "react";
import { SearchToken } from "./client";
import { DeleteLogo } from "./DeleteLogo";
import { css } from "styled-components/macro";

export function ActiveFilter({
  searchQuery,
  onRemoveFilterToken,
}: {
  searchQuery: SearchToken[];
  onRemoveFilterToken: (searchToken: SearchToken) => void;
}) {
  return (
    <div
      className="openk9-container-active-filters"
      css={css`
        display: flex;
        gap: 10px;
      `}
    >
      {searchQuery.map((selectToken, index) => (
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
            `}
          >
            {selectToken.values?.[0]}
            <span
              css={css`
                cursor: pointer;
              `}
              onClick={() => onRemoveFilterToken(selectToken)}
            >
              <DeleteLogo heightParam={10} widthParam={10} colorSvg="#BC0012" />
            </span>
          </button>
          {/* <CreateLabel
            label={selectToken.values?.[0] || ""}
            action={() => onRemoveFilterToken(selectToken)}
          /> */}
        </div>
      ))}
    </div>
  );
}
