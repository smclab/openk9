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

// Filtro per intervallo di date pensato per stare accanto al numero dei
// risultati: un pulsante compatto apre un popover con il calendario
// (`RangeCalendar`, selezione range inline), e una
// volta scelto l'intervallo il pulsante mostra "dal X al Y". Componente
// self-contained: riceve `calendarDate`/`onChange` dal widget (stato date in
// Main), quindi sa sempre l'intervallo attivo. Lascia invariato
// `DateRangePickerVertical`.
import React from "react";
// stili base del calendario (senza, il calendario renderizza senza layout)
import "react-day-picker/style.css";
import "./datePickers.css";
import { MonthCaptionProps, useDayPicker } from "react-day-picker";
import {
  endOfDay,
  endOfMonth,
  endOfWeek,
  endOfYear,
  format,
  isSameDay,
  setMonth,
  setYear,
  startOfDay,
  startOfMonth,
  startOfWeek,
  startOfYear,
} from "date-fns";
import { css } from "styled-components";
import { useTranslation } from "react-i18next";
import { SearchDateRange } from "../embeddable/Main";
import { FocusedInput } from "./dateRange";
import { RangeCalendar } from "./RangeCalendar";
import { useClickAway } from "./useClickAway";

const RED = "var(--openk9-embeddable-search--primary-color, #c0272b)";
const BORDER = "var(--openk9-embeddable-search--border-color, #ced4da)";
const INK = "var(--openk9-embeddable-search--primary-text-color, #1e1c21)";
const MUTED = "var(--openk9-embeddable-search--secondary-text-color, #6b7280)";
const RED_SOFT = `color-mix(in srgb, ${RED} 12%, #fff)`;
const RED_TINT = `color-mix(in srgb, ${RED} 22%, #fff)`;

const CalendarIcon = () => (
  <svg
    aria-hidden="true"
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
  >
    <rect
      x="3"
      y="5"
      width="18"
      height="16"
      rx="3"
      stroke="currentColor"
      strokeWidth="1.8"
    />
    <path
      d="M3 9h18M8 3v4M16 3v4"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
    />
  </svg>
);

const selectStyle = css`
  height: 28px;
  max-width: 96px;
  padding: 0 var(--openk9-embeddable-search--spacing-xs, 4px);
  border: 1px solid ${BORDER};
  border-radius: var(--openk9-embeddable-search--radius-sm, 8px);
  background: var(--openk9-embeddable-search--primary-background-color, #fff);
  color: ${INK};
  font-size: var(--openk9-embeddable-search--font-size-xs, 12px);
  font-weight: var(--openk9-embeddable-search--font-weight-semibold, 600);
  cursor: pointer;
  &:focus {
    outline: none;
    border-color: ${RED};
  }
`;

// selettori mese/anno nel caption del calendario: permettono di saltare
// rapidamente agli anni precedenti senza cliccare mese per mese.
function MonthCaption({
  calendarMonth,
  displayIndex,
  ...props
}: MonthCaptionProps) {
  const { t } = useTranslation();
  const { goToMonth } = useDayPicker();
  const month = calendarMonth.date;
  const thisYear = new Date().getFullYear();
  const years: number[] = [];
  for (let y = thisYear + 10; y >= thisYear - 100; y--) years.push(y);

  return (
    <div {...props}>
      <div
        css={css`
          display: flex;
          justify-content: center;
          gap: var(--openk9-embeddable-search--spacing-sm, 8px);
          /* lascia spazio alle frecce di navigazione ai lati del caption */
          padding: 0 34px;
        `}
      >
        <select
          aria-label={t("select-month") || "Mese"}
          value={month.getMonth()}
          onChange={(event) =>
            goToMonth(setMonth(month, Number(event.target.value)))
          }
          css={selectStyle}
        >
          {Array.from({ length: 12 }, (_, value) => (
            <option key={value} value={value}>
              {format(setMonth(month, value), "LLLL")}
            </option>
          ))}
        </select>
        <select
          aria-label={t("select-year") || "Anno"}
          value={month.getFullYear()}
          onChange={(event) =>
            goToMonth(setYear(month, Number(event.target.value)))
          }
          css={selectStyle}
        >
          {years.map((year) => (
            <option key={year} value={year}>
              {year}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}

export function DateRangeFilter({
  onChange,
  calendarDate,
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  language: string;
}) {
  const { t } = useTranslation();

  const [startDate, setStartDate] = React.useState<Date | null>(null);
  const [endDate, setEndDate] = React.useState<Date | null>(null);
  const [focusedInput, setFocusedInput] =
    React.useState<FocusedInput>("startDate");
  const [isOpen, setIsOpen] = React.useState(false);

  const rootRef = React.useRef<HTMLDivElement | null>(null);
  const triggerRef = React.useRef<HTMLButtonElement | null>(null);
  const popoverId = React.useId();

  useClickAway([rootRef], () => setIsOpen(false));

  // sincronizza con lo stato esterno (reset globale, ripristino da URL, ecc.)
  React.useEffect(() => {
    setStartDate(
      calendarDate?.startDate ? new Date(calendarDate.startDate) : null,
    );
    setEndDate(calendarDate?.endDate ? new Date(calendarDate.endDate) : null);
  }, [calendarDate?.startDate, calendarDate?.endDate]);

  const emit = React.useCallback(
    (nextStart: Date | null, nextEnd: Date | null) => {
      onChange({
        startDate: nextStart ? startOfDay(nextStart) : undefined,
        endDate: nextEnd ? endOfDay(nextEnd) : undefined,
        keywordKey: undefined,
      });
    },
    [onChange],
  );

  const handleDatesChange = ({
    startDate: nextStart,
    endDate: nextEnd,
  }: {
    startDate: Date | null;
    endDate: Date | null;
  }) => {
    setStartDate(nextStart);
    setEndDate(nextEnd);
    emit(nextStart, nextEnd);
  };

  const handleClear = () => {
    setStartDate(null);
    setEndDate(null);
    setFocusedInput("startDate");
    emit(null, null);
  };

  const hasSelection = Boolean(startDate || endDate);
  const fmt = (day: Date) => format(day, "dd/MM/yyyy");
  const triggerLabel =
    startDate && endDate
      ? `${fmt(startDate)} – ${fmt(endDate)}`
      : startDate
      ? `${t("from-date") || "Dal"} ${fmt(startDate)}`
      : endDate
      ? `${t("to-date") || "Al"} ${fmt(endDate)}`
      : t("filter-by-date") || "Filtra per data";

  const phrases = {
    chooseAvailableStartDate: (date: string) =>
      `${t("start-day") || "Data inizio"}, ${date}`,
    chooseAvailableEndDate: (date: string) =>
      `${t("end-day") || "Data fine"}, ${date}`,
  };

  const applyPreset = (nextStart: Date, nextEnd: Date) => {
    setStartDate(nextStart);
    setEndDate(nextEnd);
    setFocusedInput("startDate");
    emit(nextStart, nextEnd);
  };

  // filtri rapidi mostrati come chip sotto il calendario
  const presets: {
    key: string;
    label: string;
    range: () => [Date, Date];
  }[] = [
    {
      key: "today",
      label: t("today") || "Oggi",
      range: () => [startOfDay(new Date()), endOfDay(new Date())],
    },
    {
      key: "this-week",
      label: t("this-week") || "Questa settimana",
      range: () => [startOfWeek(new Date()), endOfWeek(new Date())],
    },
    {
      key: "this-month",
      label: t("this-month") || "Questo mese",
      range: () => [startOfMonth(new Date()), endOfMonth(new Date())],
    },
    {
      key: "this-year",
      label: t("this-year") || "Quest'anno",
      range: () => [startOfYear(new Date()), endOfYear(new Date())],
    },
  ];

  return (
    <div
      ref={rootRef}
      className="openk9-date-range-filter"
      css={css`
        position: relative;
        display: inline-flex;
        align-items: center;
        gap: var(--openk9-embeddable-search--spacing-sm, 8px);
      `}
      onKeyDown={(event) => {
        if (event.key === "Escape" && isOpen) {
          setIsOpen(false);
          triggerRef.current?.focus();
        }
      }}
    >
      <button
        ref={triggerRef}
        type="button"
        aria-haspopup="dialog"
        aria-expanded={isOpen}
        aria-controls={popoverId}
        onClick={() => setIsOpen((prev) => !prev)}
        css={css`
          display: inline-flex;
          align-items: center;
          gap: var(--openk9-embeddable-search--spacing-sm, 8px);
          max-width: 260px;
          height: 36px;
          padding: 0 var(--openk9-embeddable-search--spacing-md, 12px);
          border-radius: var(--openk9-embeddable-search--radius-md, 12px);
          cursor: pointer;
          font-size: var(--openk9-embeddable-search--font-size-sm, 14px);
          font-weight: var(
            --openk9-embeddable-search--font-weight-semibold,
            600
          );
          border: 1px solid ${hasSelection ? RED_TINT : BORDER};
          background: ${hasSelection ? RED_SOFT : "#fff"};
          color: ${hasSelection ? RED : INK};
          transition: border-color 120ms ease, background 120ms ease,
            color 120ms ease;
          &:hover {
            border-color: ${RED};
            color: ${RED};
          }
          @media (max-width: 1024px) {
            max-width: 190px;
            height: 32px;
            padding: 0 var(--openk9-embeddable-search--spacing-sm, 8px);
            gap: var(--openk9-embeddable-search--spacing-sm, 8px);
            font-size: var(--openk9-embeddable-search--font-size-xs, 12px);
          }
        `}
      >
        <span
          css={css`
            display: inline-flex;
            flex-shrink: 0;
            color: ${hasSelection ? RED : MUTED};
          `}
        >
          <CalendarIcon />
        </span>
        <span
          css={css`
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          `}
        >
          {triggerLabel}
        </span>
      </button>

      {hasSelection && (
        <button
          type="button"
          aria-label={t("remove-dates") || "Rimuovi date"}
          onClick={handleClear}
          css={css`
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            width: 28px;
            height: 28px;
            border-radius: var(--openk9-embeddable-search--radius-sm, 8px);
            border: none;
            background: transparent;
            color: ${MUTED};
            font-size: var(--openk9-embeddable-search--font-size-md, 16px);
            line-height: var(--openk9-embeddable-search--line-height-none, 1);
            cursor: pointer;
            &:hover {
              color: ${RED};
              background: ${RED_SOFT};
            }
          `}
        >
          &#x2715;
        </button>
      )}

      {isOpen && (
        <div
          id={popoverId}
          role="dialog"
          aria-label={t("filter-by-date") || "Filtra per data"}
          css={css`
            position: absolute;
            top: calc(100% + var(--openk9-embeddable-search--spacing-sm));
            right: 0;
            z-index: 100;
            background: var(
              --openk9-embeddable-search--primary-background-color,
              #fff
            );
            border: 1px solid ${BORDER};
            border-radius: var(--openk9-embeddable-search--radius-md, 12px);
            box-shadow: 0 16px 40px -14px rgba(0, 0, 0, 0.3);
            padding: var(--openk9-embeddable-search--spacing-md, 12px);

            /* --- tema del calendario sul brand rosso (scoped) --- */
            .rdp-day:not(.rdp-hidden) {
              border: 1px solid #eef0f2;
              color: ${INK};
            }
            .rdp-day:not(.rdp-hidden):hover {
              background: #f1f3f5;
              border-color: #e5e7eb;
              color: ${INK};
            }
            .rdp-range_middle {
              background: ${RED_SOFT} !important;
              border-color: ${RED_TINT} !important;
              color: ${RED} !important;
            }
            .rdp-hovered_span,
            .rdp-hovered_span:hover {
              background: ${RED_SOFT} !important;
              border-color: ${RED_TINT} !important;
              color: ${RED} !important;
            }
            .rdp-selected,
            .rdp-selected:hover,
            .rdp-selected:active {
              background: ${RED} !important;
              border-color: ${RED} !important;
              color: #fff !important;
            }
            .rdp-today {
              font-weight: var(
                --openk9-embeddable-search--font-weight-bold,
                700
              );
            }
            .rdp-month_caption {
              color: ${INK};
              font-size: var(--openk9-embeddable-search--font-size-md, 16px);
              padding-top: var(--openk9-embeddable-search--spacing-lg, 16px);
              /* 38px with react-dates, less the 20px weekday row */
              padding-bottom: 18px;
            }
            .rdp-weekday {
              color: ${MUTED};
            }
            .rdp-button_previous,
            .rdp-button_next {
              border-color: ${BORDER};
              border-radius: var(--openk9-embeddable-search--radius-sm, 8px);
            }
            .rdp-button_previous:focus,
            .rdp-button_previous:hover,
            .rdp-button_next:focus,
            .rdp-button_next:hover {
              border-color: ${RED};
            }
            /* frecce spinte ai bordi, così non toccano i select mese/anno */
            .rdp-button_previous,
            .rdp-button_next {
              padding: var(--openk9-embeddable-search--spacing-xs, 4px)
                var(--openk9-embeddable-search--spacing-sm, 8px);
              top: 15px;
            }
            .rdp-button_previous {
              left: 2px;
            }
            .rdp-button_next {
              right: 2px;
            }
          `}
        >
          <div
            css={css`
              display: flex;
              align-items: center;
              justify-content: flex-end;
              margin-bottom: var(--openk9-embeddable-search--spacing-sm, 8px);
            `}
          >
            <button
              type="button"
              aria-label={t("close") || "Chiudi"}
              onClick={() => {
                setIsOpen(false);
                triggerRef.current?.focus();
              }}
              css={css`
                display: inline-flex;
                align-items: center;
                justify-content: center;
                flex-shrink: 0;
                width: 28px;
                height: 28px;
                border: none;
                background: transparent;
                color: ${MUTED};
                font-size: var(--openk9-embeddable-search--font-size-lg, 18px);
                line-height: var(
                  --openk9-embeddable-search--line-height-none,
                  1
                );
                cursor: pointer;
                border-radius: var(--openk9-embeddable-search--radius-sm, 8px);
                &:hover {
                  color: ${RED};
                }
              `}
            >
              &#x2715;
            </button>
          </div>

          <RangeCalendar
            startDate={startDate}
            endDate={endDate}
            onDatesChange={handleDatesChange}
            focusedInput={focusedInput}
            onFocusChange={(next) => setFocusedInput(next || "startDate")}
            numberOfMonths={1}
            minimumNights={0}
            keepOpenOnDateSelect={false}
            daySize={34}
            initialVisibleMonth={startDate || endDate || new Date()}
            components={{ MonthCaption }}
            phrases={phrases}
          />

          {/* filtri rapidi */}
          <div
            css={css`
              display: flex;
              flex-wrap: wrap;
              gap: var(--openk9-embeddable-search--spacing-sm, 8px);
              margin-top: var(--openk9-embeddable-search--spacing-md, 12px);
              padding-top: var(--openk9-embeddable-search--spacing-md, 12px);
              border-top: 1px solid #eef0f2;
            `}
          >
            {presets.map((preset) => {
              const [presetStart, presetEnd] = preset.range();
              const active = Boolean(
                startDate &&
                  endDate &&
                  isSameDay(startDate, presetStart) &&
                  isSameDay(endDate, presetEnd),
              );
              return (
                <button
                  key={preset.key}
                  type="button"
                  aria-pressed={active}
                  onClick={() => applyPreset(presetStart, presetEnd)}
                  css={css`
                    border: 1px solid ${active ? RED : "transparent"};
                    background: ${active ? RED : "#f4f5f7"};
                    color: ${active ? "#fff" : INK};
                    border-radius: var(
                      --openk9-embeddable-search--radius-pill,
                      999px
                    );
                    padding: var(--openk9-embeddable-search--spacing-sm, 8px)
                      var(--openk9-embeddable-search--spacing-lg, 16px);
                    font-size: var(
                      --openk9-embeddable-search--font-size-xs,
                      12px
                    );
                    font-weight: var(
                      --openk9-embeddable-search--font-weight-semibold,
                      600
                    );
                    cursor: pointer;
                    transition: background 120ms ease, color 120ms ease,
                      border-color 120ms ease;
                    &:hover {
                      background: ${active ? RED : RED_SOFT};
                      color: ${active ? "#fff" : RED};
                      border-color: ${active ? RED : RED_TINT};
                    }
                  `}
                >
                  {preset.label}
                </button>
              );
            })}
          </div>

          {hasSelection && (
            <div
              css={css`
                display: flex;
                justify-content: flex-end;
                margin-top: var(--openk9-embeddable-search--spacing-sm, 8px);
              `}
            >
              <button
                type="button"
                onClick={handleClear}
                css={css`
                  border: none;
                  background: none;
                  color: ${RED};
                  font-size: var(
                    --openk9-embeddable-search--font-size-sm,
                    14px
                  );
                  font-weight: var(
                    --openk9-embeddable-search--font-weight-semibold,
                    600
                  );
                  cursor: pointer;
                  padding: var(--openk9-embeddable-search--spacing-xs, 4px)
                    var(--openk9-embeddable-search--spacing-xs, 4px);
                `}
              >
                {t("remove-dates") || "Cancella"}
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default DateRangeFilter;
