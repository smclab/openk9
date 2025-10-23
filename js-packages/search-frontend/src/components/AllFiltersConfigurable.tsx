import { faChevronDown, faChevronUp } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { isEqualWith } from "lodash";
import React, { Dispatch, SetStateAction } from "react";
import { remappingLanguage, SearchDateRange } from "../embeddable/Main";
import { ChangeLanguage, LanguageItem } from "./ChangeLanguage";
import { SearchToken } from "./client";
import { FiltersMemo, FiltersProps } from "./Filters";
import { SortResultListMemo } from "./SortResultList";
import { useTranslation } from "react-i18next";
import { DataRangePickerVertical } from "./DateRangePickerVertical";
import { css } from "styled-components/macro";

type TypeFilter = "filters" | "sort" | "language" | "calendar";
export type TypeAllFilters =
  | {
      filter: TypeFilter;
      sort: number;
    }[]
  | null
  | undefined;

function CollapsableFilterCategoryLocal({
  title,
  description,
  children,
}: {
  title: string;
  description?: string;
  children: React.ReactNode;
}) {
  const [isOpen, setIsOpen] = React.useState(true);

  return (
    <div
      className="openk9-filter-category"
      css={css`
        padding-inline: 16px;
        padding-bottom: 16px;
      `}
    >
      <div
        className="openk9-filter-category-title"
        css={css`
          user-select: none;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 8px;
          padding: 6px 0;
          padding-bottom: 8px;
          border-bottom: 1px solid var(--openk9-embeddable-search--border-color);
        `}
      >
        <legend
          className="legend-filters"
          css={css`
            display: flex;
            align-items: center;
            gap: 8px;
            :first-letter {
              text-transform: uppercase;
            }
          `}
        >
          <strong
            className="name-category-filter"
            css={css`
              font-size: 14px;
              letter-spacing: 0.2px;
              color: var(--openk9-embeddable-search--secondary-text-color);
            `}
          >
            {title}
          </strong>
        </legend>
        <button
          className={`openk9-collapsable-filters ${
            isOpen
              ? "openk9-dropdown-filters-open"
              : "openk9-dropdown-filters-close"
          }`}
          aria-label="Collassa filtro"
          aria-expanded={isOpen ? "true" : "false"}
          css={css`
            background: transparent;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 8px;
            padding: 6px 8px;
            cursor: pointer;
            transition: transform 120ms ease, background-color 120ms ease,
              border-color 120ms ease;
            &:hover {
              background: rgba(0, 0, 0, 0.03);
            }
            &:active {
              transform: translateY(1px);
            }
          `}
          onClick={() => setIsOpen((prev) => !prev)}
        >
          <FontAwesomeIcon
            icon={isOpen ? faChevronUp : faChevronDown}
            style={{
              color: "var(--openk9-embeddable-search--secondary-icon-color)",
              cursor: "pointer",
            }}
          />
        </button>
      </div>
      {description && (
        <div
          className="openk9-filter-category-description"
          css={css`
            font-size: 12px;
            color: var(--openk9-embeddable-search--secondary-text-color);
            margin-bottom: 8px;
          `}
        >
          {description}
        </div>
      )}

      {isOpen && (
        <div
          className="openk9-filter-category-content"
          css={css`
            padding-top: 16px;
          `}
        >
          {children}
        </div>
      )}
    </div>
  );
}

function FactoryFilter(
  filter: TypeFilter,
  filtersSelect: SearchToken[] | null | undefined,
  onSelect: Dispatch<SetStateAction<SearchToken[] | null | undefined>>,
  lang: {
    lang: string | null | undefined;
    setLang(lang: string | null | undefined): void;
    languages?: LanguageItem[] | undefined;
  },
  sort: {
    sort:
      | {
          field: string;
          type: "asc" | "desc";
        }
      | null
      | undefined;
    setSort: Dispatch<
      SetStateAction<
        | {
            field: string;
            type: "asc" | "desc";
          }
        | null
        | undefined
      >
    >;
  },
  filterDefault: FiltersProps,
  calendar: {
    calendarDate: SearchDateRange;
    setCalendarSelected: React.Dispatch<React.SetStateAction<SearchDateRange>>;
    translationLabel?:
      | {
          labelStart?: string;
          labelEnd?: string;
          placeholderStart?: string;
          placeholderEnd?: string;
          errorFormatData?: string;
          errorSelectData?: string;
        }
      | undefined;
  },
) {
  switch (filter) {
    case "calendar":
      return (
        <CollapsableFilterCategoryLocal title="Calendar">
          <DataRangePickerVertical
            language={lang.lang || "it"}
            calendarDate={calendar.calendarDate}
            onChange={(e) => {
              calendar.setCalendarSelected(e);
            }}
            translationLabel={calendar.translationLabel}
          />
        </CollapsableFilterCategoryLocal>
      );
    case "language":
      return (
        <CollapsableFilterCategoryLocal title="Language">
          <ChangeLanguage
            activeLanguage={lang.lang || "it"}
            setChangeLanguage={(language) => {
              lang.setLang(language);
            }}
            languages={lang.languages}
          />
        </CollapsableFilterCategoryLocal>
      );
    case "sort":
      return (
        <CollapsableFilterCategoryLocal title="Sort">
          <SortResultListMemo
            language={lang.lang || undefined}
            setSortResult={sort.setSort}
          />
        </CollapsableFilterCategoryLocal>
      );
    case "filters":
      return (
        <>
          <span className="openk9-Filters-informations">Filters</span>
          <FiltersMemo
            {...filterDefault}
            searchQuery={filtersSelect || []}
            onAddFilterToken={(token) => onSelect((t) => [...(t ?? []), token])}
            onRemoveFilterToken={(token) =>
              onSelect((t) =>
                t?.filter((i) => !isEqualWith(i.values, token.values)),
              )
            }
            dynamicFilters={false}
          />
        </>
      );
    default:
      return null;
  }
}

export default function AllFilters({
  filtersUse,
  defaultLanguage,
  filterDefault,
  calendar,
}: {
  filtersUse: TypeAllFilters;
  defaultLanguage: string;
  filterDefault: FiltersProps & {
    setAllFilters(searchTokens: SearchToken[]): void;
    setLanguageSelected(language: string): void;
    setCalendarSelected: React.Dispatch<React.SetStateAction<SearchDateRange>>;
    setSortSelected(sort: { field: string; type: "asc" | "desc" } | null): void;
    languages: LanguageItem[] | undefined;
    defaultFilter: SearchToken[] | null | undefined;
  };
  calendar: {
    calendarDate: SearchDateRange;
    onChange(date: SearchDateRange): void;
    translationLabel?:
      | {
          labelStart?: string;
          labelEnd?: string;
          placeholderStart?: string;
          placeholderEnd?: string;
          errorFormatData?: string;
          errorSelectData?: string;
        }
      | undefined;
  };
}) {
  const [searchToken, setSearchToken] = React.useState<
    Array<SearchToken> | null | undefined
  >(filterDefault.defaultFilter || []);
  const [lang, setLang] = React.useState<string | null | undefined>(
    defaultLanguage,
  );
  const [calendarToken, setCalendarToken] = React.useState<SearchDateRange>({
    endDate: undefined,
    startDate: undefined,
    keywordKey: undefined,
  });
  const { i18n } = useTranslation();
  const [sort, setSort] = React.useState<
    | {
        field: string;
        type: "asc" | "desc";
      }
    | null
    | undefined
  >(null);

  const orderedFilters = React.useMemo(
    () => (filtersUse ? [...filtersUse].sort((a, b) => a.sort - b.sort) : []),
    [filtersUse],
  );

  return (
    <>
      {orderedFilters?.map((filter) =>
        FactoryFilter(
          filter.filter,
          searchToken,
          setSearchToken,
          { lang, setLang, languages: filterDefault.languages },
          { sort, setSort },
          filterDefault,
          {
            calendarDate: calendar.calendarDate,
            setCalendarSelected: setCalendarToken,
            translationLabel: calendar.translationLabel,
          },
        ),
      )}
      <button
        className="Openk9-all-filters-save"
        onClick={() => {
          filterDefault.setAllFilters(searchToken || []);
          filterDefault.setLanguageSelected(lang || "it");
          i18n.changeLanguage(remappingLanguage({ language: lang || "it" }));
          filterDefault.setSortSelected(sort || null);
          calendar.onChange(calendarToken);
        }}
        css={css`
          background: var(--openk9-embeddable-search--primary-color, #0078d4);
          color: #fff;
          border: none;
          border-radius: 8px;
          padding: 10px 24px;
          font-size: 16px;
          font-weight: 600;
          cursor: pointer;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          transition: background 0.2s, box-shadow 0.2s;
          margin-top: 16px;
          &:hover {
            background: var(
              --openk9-embeddable-search--primary-light-color,
              #005fa3
            );
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
          }
          &:active {
            background: var(
              --openk9-embeddable-search--primary-color-active,
              #004377
            );
          }
        `}
      >
        Save
      </button>
    </>
  );
}
