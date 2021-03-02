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

export function ChatIcon({ size = 24, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" className={className}>
      <path d="M12.8,2H3.2A1.2,1.2,0,0,0,2.006,3.2L2,14l2.4-2.4h8.4A1.2,1.2,0,0,0,14,10.4V3.2A1.2,1.2,0,0,0,12.8,2ZM4.4,6.2h7.2V7.4H4.4Zm4.8,3H4.4V8H9.2Zm2.4-3.6H4.4V4.4h7.2Z" />
    </svg>
  );
}
