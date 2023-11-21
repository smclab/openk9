import { t } from "i18next";
import { CreateLabel } from "./Filters";
import React from "react";
import { DeleteLogo } from "./DeleteLogo";
import { ConfigurationUpdateFunction } from "../embeddable/entry";
import { css } from "styled-components/macro";

export function RemoveFilters({
  onConfigurationChange,
}: {
  onConfigurationChange: ConfigurationUpdateFunction;
}) {
  return (
    <div className="openk9-remove-filters-container">
      <button
        className="openk9-remove-filters-button btn"
        onClick={() => {
          onConfigurationChange({ filterTokens: [] });
        }}
        css={css`
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 8px 12px;
        gap: 3px;
        background: #ffffff;
        border: 1px solid  var(--openk9-embeddable-search--secondary-active-color);
        border-radius: 20px;
        white-space: nowrap;
        cursor: pointer;
        color: var(--openk9-embeddable-search--secondary-active-color);
        `}
      >
        {t("remove-filters")}
        <DeleteLogo heightParam={8} widthParam={8} colorSvg={"#C0272B"} />
      </button>
    </div>
  );
}
