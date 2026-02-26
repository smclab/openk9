/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { t } from "i18next";
import { css } from "styled-components";
import { resetFiltersType } from "../embeddable/entry";
import { CircleDelete } from "../svgElement/CircleDelete";

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
  configurableLabel,
  removeAriaLabel,
  callback,
}: {
  itemsRemove?: resetFiltersType;
  configurableLabel?: string | null | undefined;
  removeAriaLabel?: string | null | undefined;
  reset?: {
    calendar?(): void;
    filters?(): void;
    sort?(): void;
    search?(): void;
    language?(): void;
  };
  callback?: () => void;
}) {
  return (
    <button
      className="openk9-remove-filters-button btn"
      aria-label={removeAriaLabel ?? t("removeFilters") ?? ""}
      onClick={() => {
        handleReset(itemsRemove, reset);
        if (callback) callback();
      }}
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
      {configurableLabel ? configurableLabel : t("removeFilters")}
    </button>
  );
}

