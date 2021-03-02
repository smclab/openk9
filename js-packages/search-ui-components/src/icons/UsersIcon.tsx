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

export function UsersIcon({ size = 24, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" className={className}>
      <path
        d="M7.714,2.714A2.714,2.714,0,1,0,2.652,4.076,5.1,5.1,0,0,0,1.419,5.109,5.889,5.889,0,0,0,0,9,1,1,0,0,0,2,9,3.327,3.327,0,0,1,5,5.429,2.717,2.717,0,0,0,7.714,2.714Zm4.867,6.219A6.068,6.068,0,0,1,14.288,10.3,7.114,7.114,0,0,1,16,15a1,1,0,0,1-2,0c0-2.6-1.794-4.714-4-4.714S6,12.4,6,15a1,1,0,0,1-2,0,7.114,7.114,0,0,1,1.712-4.7A6.066,6.066,0,0,1,7.419,8.933a3.143,3.143,0,1,1,5.162,0ZM10,6a1.143,1.143,0,1,0,1.144,1.143A1.144,1.144,0,0,0,10,6ZM5,2a.714.714,0,1,0,.715.714A.715.715,0,0,0,5,2Z"
        fillRule="evenodd"
        fill="currentColor"
      />
    </svg>
  );
}
