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

export function TenantsIcon({ size = 24, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" className={className}>
      <path
        d="M4,16l9-3,3-9L7,7Z"
        transform="translate(2 2)"
        fill="currentColor"
      />
      <path
        d="M12,0A12,12,0,0,1,24,12,12,12,0,0,1,12,24,12,12,0,0,1,0,12,12,12,0,0,1,12,0ZM3,12a9,9,0,1,0,9-9A9.008,9.008,0,0,0,3,12Z"
        fillRule="evenodd"
        fill="currentColor"
      />
    </svg>
  );
}
