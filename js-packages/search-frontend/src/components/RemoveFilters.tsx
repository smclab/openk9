import { t } from "i18next";
import { CreateLabel } from "./Filters";
import React from "react";
import { DeleteLogo } from "./DeleteLogo";
import {
  ConfigurationUpdateFunction,
  resetFiltersType,
} from "../embeddable/entry";
import { css } from "styled-components/macro";
import { CircleDelete } from "../svgElement/CircleDelete";
import { SelectionsAction } from "./useSelections";

function handleReset(
  itemsRemove?: resetFiltersType,
  reset?: {
    calendar?(): void;
    filters?(): void;
    sort?(): void;
    search?(): void;
    language?(): void;
  },
) {
  if (!reset) return;
  const items =
    itemsRemove && itemsRemove.length > 0 ? itemsRemove : ["filters"];
  items.forEach((item) => {
    if (item === "calendar" && reset.calendar) reset.calendar();
    if (item === "filters" && reset.filters) reset.filters();
    if (item === "sort" && reset.sort) reset.sort();
    if (item === "search" && reset.search) reset.search();
    if (item === "language" && reset.language) reset.language();
  });
}

export function RemoveFilters({
  itemsRemove,
  reset,
}: {
  itemsRemove?: resetFiltersType;
  reset?: {
    calendar?(): void;
    filters?(): void;
    sort?(): void;
    search?(): void;
    language?(): void;
  };
}) {
  return (
    <button
      className="openk9-remove-filters-button btn"
      aria-label={t("removeFilters") ?? ""}
      onClick={() => handleReset(itemsRemove, reset)}
      css={css`
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 8px 12px;
        gap: 3px;
        background: #ffffff;
        border: 1px solid
          var(--openk9-embeddable-search--secondary-active-color);
        white-space: nowrap;
        cursor: pointer;
        border-radius: 8px;
        color: var(--openk9-embeddable-search--secondary-active-color);
      `}
    >
      <CircleDelete aria-hidden="true" />
      {t("removeFilters")}
    </button>
  );
}
