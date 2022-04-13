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

export function DataSourceIcon({ size = 24, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 14 16" className={className}>
      <path
        d="M12.557,13A1.444,1.444,0,0,0,14,11.556V4.444A1.444,1.444,0,0,0,12.557,3H8.938c-.24,0-.81-.913-.979-1.259l0-.008C7.563.919,7.119,0,6.222,0h-4.8A1.474,1.474,0,0,0,0,1.519V11.556A1.444,1.444,0,0,0,1.443,13H6v1H1a1,1,0,0,0,0,2H13a1,1,0,0,0,0-2H8V13ZM1.984,1.7A.747.747,0,0,1,2,1.625H6.166a4.216,4.216,0,0,1,.5.853c.078.169.162.347.25.522H1.984ZM2,5v6H12V5Z"
        fillRule="evenodd"
        fill="currentColor"
      />
    </svg>
  );
}
