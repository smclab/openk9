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

export function DeleteLogo({
  widthParam = 14,
  heightParam = 28,
  colorSvg = "#949494",
}) {
  return (
    <svg
      className="openk9-delete-logo"
      aria-hidden={true}
      width={widthParam}
      height={heightParam}
      viewBox="0 0 14 14"
      fill="none"
    >
      <path
        className="openk9-delete-logo-color"
        d="M14 1.41L12.59 0L7 5.59L1.41 0L0 1.41L5.59 7L0 12.59L1.41 14L7 8.41L12.59 14L14 12.59L8.41 7L14 1.41Z"
        fill={colorSvg}
      />
    </svg>
  );
}

