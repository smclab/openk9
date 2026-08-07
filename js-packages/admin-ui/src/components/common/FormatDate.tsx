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
import React from "react";
import { useTranslation } from "react-i18next";

type DateValue = string | number | Date | null | undefined;

/**
 * Returns a date formatter bound to the language currently selected in the
 * interface. It is a hook so that components re-render when the language
 * changes, instead of keeping the dates formatted with the previous locale.
 */
export function useFormatDate() {
  const { i18n } = useTranslation();

  return React.useMemo(() => {
    const dateTimeFormatter = new Intl.DateTimeFormat(i18n.resolvedLanguage, {
      dateStyle: "medium",
      timeStyle: "medium",
    });
    return (value: DateValue) => (value ? dateTimeFormatter.format(new Date(value)) : "");
  }, [i18n.resolvedLanguage]);
}

