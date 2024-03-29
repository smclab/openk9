import { t } from "i18next";
import { CreateLabel } from "./Filters";
import React from "react";
import { DeleteLogo } from "./DeleteLogo";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { css } from "styled-components/macro";
import { CircleDelete } from "../svgElement/CircleDelete";
import { SelectionsAction } from "./useSelections";

export function RemoveFilters({
  onConfigurationChange,
  selectionsDispatch,
}: {
  onConfigurationChange: ConfigurationUpdateFunction;
  selectionsDispatch: React.Dispatch<SelectionsAction>;
}) {
  return (
    <div className="openk9-remove-filters-container">
      <button
        className="openk9-remove-filters-button btn"
        onClick={() => {
          onConfigurationChange({ filterTokens: [] });
          selectionsDispatch({type:"reset-filters"})
        }}
        
        css={css`
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 8px 12px;
          gap: 3px;
          background: #ffffff;
          border: 1px solid
            var(--openk9-embeddable-search--secondary-active-color);
          border-radius: 20px;
          white-space: nowrap;
          cursor: pointer;
          color: var(--openk9-embeddable-search--secondary-active-color);
        `}
      >
        <CircleDelete />
        Cancella tutti i filtri
      </button>
    </div>
  );
}
