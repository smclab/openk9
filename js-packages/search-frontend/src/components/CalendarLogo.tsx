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

export function CalendarLogo({ size }: { size?: string }) {
  return (
    <svg
      aria-hidden="true"
      className="openk9-calendar-logo"
      width={size ? size : "35"}
      height={size ? size : "35"}
      viewBox="0 0 24 24"
    >
      <path
        d="M0 0h24v24H0z"
        fill="white"
        className="openk9-calendar-logo-color"
      />
      <path
        className="openk9-calendar-logo-color-secondar"
        d="M20 3h-1V1h-2v2H7V1H5v2H4c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 18H4V8h16v13z"
        fill="#C0272B"
      />
    </svg>
  );
}

