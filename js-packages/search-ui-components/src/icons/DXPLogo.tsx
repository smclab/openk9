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

export function DXPLogo({ size = 128, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 96 96" className={className}>
      <path
        transform="translate(23.452 10.8) scale(4)"
        d="m1.5706 2.2183c2.3004-3.2261 8.6964-2.7772 12.147 0.33664 6.5083 5.8631-1.9357 20.31-9.8466 14.531-0.19643-0.1402-0.39279-0.2805-0.58916-0.4488-2.4967-2.1601-4.5166-5.2179-2.3845-13.017 0.14026-0.53301 0.39273-0.98185 0.67332-1.4026z"
        clipRule="evenodd"
        fill="#1AA0E8"
        fillRule="evenodd"
      ></path>
      <path
        transform="translate(8.4 17.152) scale(4)"
        d="m5.3334 0.63015c6.6485-2.7211 14.98 3.8152 8.5 11.081-1.5148 1.6831-3.8713 3.1699-6.1997 3.7871-1.6832 0.4489-3.3663 0.4208-4.6848-0.3366-3.17-1.7674-3.9555-8.0512-1.5149-11.418 1.1221-1.5149 2.4687-2.5248 3.8993-3.1139z"
        clipRule="evenodd"
        fill="#0B63CE"
        fillRule="evenodd"
      ></path>
      <path
        transform="translate(23.452 17.152) scale(4)"
        d="m10.071 11.711c6.4802-7.2658-1.8515-13.802-8.5001-11.081-0.28053 0.4208-0.53301 0.86964-0.67327 1.4026-2.132 7.7987-0.11221 10.856 2.3845 13.017 0.19637 0.1683 0.39274 0.3085 0.58911 0.4488 2.3284-0.6172 4.6848-2.104 6.1997-3.7871z"
        fill="#134194"
      ></path>
    </svg>
  );
}
