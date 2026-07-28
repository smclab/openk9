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
// (`DayPickerRangeController` di react-dates, selezione range inline), e una
// volta scelto l'intervallo il pulsante mostra "dal X al Y". Componente
// self-contained: riceve `calendarDate`/`onChange` dal widget (stato date in
// Main), quindi sa sempre l'intervallo attivo. Lascia invariato
// `DateRangePickerVertical`.
import React from "react";
import "react-dates/initialize";
// stili base di react-dates (senza, il calendario renderizza senza layout)
import "react-dates/lib/css/_datepicker.css";
import { DayPickerRangeController, FocusedInputShape } from "react-dates";
import { DateRangePickerPhrases } from "react-dates/lib/defaultPhrases";
import moment, { Moment } from "moment";
import { css } from "styled-components";
import { useTranslation } from "react-i18next";
import { SearchDateRange } from "../embeddable/Main";
import { mappingNameLanguage } from "./CalendarModal";
import { useClickAway } from "./useClickAway";
import "moment/locale/de";
import "moment/locale/it";
import "moment/locale/es";
import "moment/locale/fr";

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

export function DateRangeFilter({
  onChange,
  calendarDate,
  language,
}: {
  onChange(value: SearchDateRange): void;
  calendarDate: SearchDateRange;
  language: string;
}) {
  const { t } = useTranslation();
  moment.locale(mappingNameLanguage(language));

  const [startDate, setStartDate] = React.useState<Moment | null>(null);
  const [endDate, setEndDate] = React.useState<Moment | null>(null);
  const [focusedInput, setFocusedInput] =
    React.useState<FocusedInputShape>("startDate");
  const [isOpen, setIsOpen] = React.useState(false);

  const rootRef = React.useRef<HTMLDivElement | null>(null);
  const triggerRef = React.useRef<HTMLButtonElement | null>(null);
  const popoverId = React.useId();

  useClickAway([rootRef], () => setIsOpen(false));

  // sincronizza con lo stato esterno (reset globale, ripristino da URL, ecc.)
  React.useEffect(() => {
    setStartDate(
      calendarDate?.startDate ? moment(calendarDate.startDate) : null,
    );
    setEndDate(calendarDate?.endDate ? moment(calendarDate.endDate) : null);
  }, [calendarDate?.startDate, calendarDate?.endDate]);

  const emit = React.useCallback(
    (nextStart: Moment | null, nextEnd: Moment | null) => {
      onChange({
        startDate: nextStart
          ? nextStart.clone().startOf("day").toDate()
          : undefined,
        endDate: nextEnd ? nextEnd.clone().endOf("day").toDate() : undefined,
        keywordKey: undefined,
      });
    },
    [onChange],
  );

  const handleDatesChange = ({
    startDate: nextStart,
    endDate: nextEnd,
  }: {
    startDate: Moment | null;
    endDate: Moment | null;
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
  const fmt = (day: Moment) => day.format("DD/MM/YYYY");
  const triggerLabel =
    startDate && endDate
      ? `${fmt(startDate)} – ${fmt(endDate)}`
      : startDate
      ? `${t("from-date") || "Dal"} ${fmt(startDate)}`
      : endDate
      ? `${t("to-date") || "Al"} ${fmt(endDate)}`
      : t("filter-by-date") || "Filtra per data";

  const phrases = {
    ...DateRangePickerPhrases,
    chooseAvailableStartDate: ({ date }: { date: string }) =>
      `${t("start-day") || "Data inizio"}, ${date}`,
    chooseAvailableEndDate: ({ date }: { date: string }) =>
      `${t("end-day") || "Data fine"}, ${date}`,
  };

  // selettori mese/anno nel caption del calendario: permettono di saltare
  // rapidamente agli anni precedenti senza cliccare mese per mese.
  const thisYear = moment().year();
  const years: number[] = [];
  for (let y = thisYear + 10; y >= thisYear - 100; y--) years.push(y);

  const selectStyle = css`
    height: 28px;
    max-width: 96px;
    padding: 0 4px;
    border: 1px solid ${BORDER};
    border-radius: 8px;
    background: #fff;
    color: ${INK};
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    &:focus {
      outline: none;
      border-color: ${RED};
    }
  `;

  const renderMonthElement = ({
    month,
    onMonthSelect,
    onYearSelect,
  }: {
    month: Moment;
    onMonthSelect: (currentMonth: Moment, newMonthVal: string) => void;
    onYearSelect: (currentMonth: Moment, newYearVal: string) => void;
  }) => (
    <div
      css={css`
        display: flex;
        justify-content: center;
        gap: 8px;
        /* lascia spazio alle frecce di navigazione ai lati del caption */
        padding: 0 34px;
      `}
    >
      <select
        aria-label={t("select-month") || "Mese"}
        value={month.month()}
        onChange={(event) => onMonthSelect(month, event.target.value)}
        css={selectStyle}
      >
        {moment.months().map((label, value) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </select>
      <select
        aria-label={t("select-year") || "Anno"}
        value={month.year()}
        onChange={(event) => onYearSelect(month, event.target.value)}
        css={selectStyle}
      >
        {years.map((year) => (
          <option key={year} value={year}>
            {year}
          </option>
        ))}
      </select>
    </div>
  );

  const applyPreset = (nextStart: Moment, nextEnd: Moment) => {
    setStartDate(nextStart);
    setEndDate(nextEnd);
    setFocusedInput("startDate");
    emit(nextStart, nextEnd);
  };

  // filtri rapidi mostrati come chip sotto il calendario
  const presets: {
    key: string;
    label: string;
    range: () => [Moment, Moment];
  }[] = [
    {
      key: "today",
      label: t("today") || "Oggi",
      range: () => [moment().startOf("day"), moment().endOf("day")],
    },
    {
      key: "this-week",
      label: t("this-week") || "Questa settimana",
      range: () => [moment().startOf("week"), moment().endOf("week")],
    },
    {
      key: "this-month",
      label: t("this-month") || "Questo mese",
      range: () => [moment().startOf("month"), moment().endOf("month")],
    },
    {
      key: "this-year",
      label: t("this-year") || "Quest'anno",
      range: () => [moment().startOf("year"), moment().endOf("year")],
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
        gap: 6px;
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
          gap: 8px;
          max-width: 260px;
          height: 36px;
          padding: 0 12px;
          border-radius: 10px;
          cursor: pointer;
          font-size: 13px;
          font-weight: 600;
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
            padding: 0 8px;
            gap: 6px;
            font-size: 12px;
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
            border-radius: 8px;
            border: none;
            background: transparent;
            color: ${MUTED};
            font-size: 16px;
            line-height: 1;
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
            top: calc(100% + 8px);
            right: 0;
            z-index: 100;
            background: #fff;
            border: 1px solid ${BORDER};
            border-radius: 12px;
            box-shadow: 0 16px 40px -14px rgba(0, 0, 0, 0.3);
            padding: 12px;

            /* --- tema react-dates sul brand rosso (scoped) --- */
            .CalendarDay__default {
              border: 1px solid #eef0f2;
              color: ${INK};
            }
            .CalendarDay__default:hover {
              background: #f1f3f5;
              border-color: #e5e7eb;
              color: ${INK};
            }
            .CalendarDay__selected_span {
              background: ${RED_SOFT} !important;
              border-color: ${RED_TINT} !important;
              color: ${RED} !important;
            }
            .CalendarDay__hovered_span,
            .CalendarDay__hovered_span:hover {
              background: ${RED_SOFT} !important;
              border-color: ${RED_TINT} !important;
              color: ${RED} !important;
            }
            .CalendarDay__selected,
            .CalendarDay__selected:hover,
            .CalendarDay__selected:active {
              background: ${RED} !important;
              border-color: ${RED} !important;
              color: #fff !important;
            }
            .CalendarDay__today {
              font-weight: 700;
            }
            .CalendarMonth_caption {
              color: ${INK};
              font-size: 15px;
              padding-top: 15px;
              padding-bottom: 38px;
            }
            .DayPicker_weekHeader {
              color: ${MUTED};
            }
            .DayPickerNavigation_button__default {
              border-color: ${BORDER};
              border-radius: 8px;
            }
            .DayPickerNavigation_button__default:focus,
            .DayPickerNavigation_button__default:hover {
              border-color: ${RED};
            }
            /* frecce spinte ai bordi, così non toccano i select mese/anno */
            .DayPickerNavigation_button__horizontalDefault {
              padding: 4px 8px;
              top: 15px;
            }
            .DayPickerNavigation_leftButton__horizontalDefault {
              left: 2px;
            }
            .DayPickerNavigation_rightButton__horizontalDefault {
              right: 2px;
            }
          `}
        >
          <div
            css={css`
              display: flex;
              align-items: center;
              justify-content: flex-end;
              margin-bottom: 8px;
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
                font-size: 18px;
                line-height: 1;
                cursor: pointer;
                border-radius: 8px;
                &:hover {
                  color: ${RED};
                }
              `}
            >
              &#x2715;
            </button>
          </div>

          <DayPickerRangeController
            startDate={startDate}
            endDate={endDate}
            onDatesChange={handleDatesChange}
            focusedInput={focusedInput}
            onFocusChange={(next) => setFocusedInput(next || "startDate")}
            numberOfMonths={1}
            daySize={34}
            minimumNights={0}
            noBorder
            hideKeyboardShortcutsPanel
            isOutsideRange={() => false}
            initialVisibleMonth={() => startDate || endDate || moment()}
            renderMonthElement={renderMonthElement}
            phrases={phrases}
          />

          {/* filtri rapidi */}
          <div
            css={css`
              display: flex;
              flex-wrap: wrap;
              gap: 6px;
              margin-top: 10px;
              padding-top: 10px;
              border-top: 1px solid #eef0f2;
            `}
          >
            {presets.map((preset) => {
              const [presetStart, presetEnd] = preset.range();
              const active = Boolean(
                startDate &&
                  endDate &&
                  startDate.isSame(presetStart, "day") &&
                  endDate.isSame(presetEnd, "day"),
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
                    border-radius: 999px;
                    padding: 6px 14px;
                    font-size: 12.5px;
                    font-weight: 600;
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
                margin-top: 8px;
              `}
            >
              <button
                type="button"
                onClick={handleClear}
                css={css`
                  border: none;
                  background: none;
                  color: ${RED};
                  font-size: 13px;
                  font-weight: 600;
                  cursor: pointer;
                  padding: 4px 2px;
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
