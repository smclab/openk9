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

export function HomeIcon({ size = 24, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" className={className}>
      <path
        d="M645.787,876.887v7.04a1.131,1.131,0,0,1-.96.96h-5.653v-5.653H635.44v5.653h-5.653a1.131,1.131,0,0,1-.96-.96v-7.04h0l8.533-7.04,8.427,7.04Zm3.2-1.067-.96,1.067c-.107.107-.213.107-.32.213h0a.392.392,0,0,1-.32-.107l-10.24-8.533-10.24,8.533a.392.392,0,0,1-.32.107c-.107,0-.213-.107-.32-.213l-.96-1.067a.392.392,0,0,1-.107-.32c0-.107.107-.214.213-.32l10.667-8.853a1.942,1.942,0,0,1,1.173-.427,1.457,1.457,0,0,1,1.173.427l3.627,2.987v-2.88a.377.377,0,0,1,.427-.427h2.88a.377.377,0,0,1,.427.427v5.973l3.2,2.667c.106.107.106.213.213.32C649.093,875.607,649.093,875.713,648.987,875.82Z"
        transform="translate(-625.2 -863.393)"
        fill="currentColor"
      />
    </svg>
  );
}
