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

export function SettingsIcon({ size = 24, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" className={className}>
      <path
        d="M9.489,4.99a4.5,4.5,0,1,0,4.5,4.5A4.5,4.5,0,0,0,9.489,4.99Zm-3,4.5a3,3,0,1,0,3-3A3,3,0,0,0,6.49,9.489Z"
        transform="translate(2.508 2.508)"
        fillRule="evenodd"
        fill="currentColor"
      />
      <path
        d="M21.84,9l1,.15A1.36,1.36,0,0,1,24,10.489v3.023a1.355,1.355,0,0,1-1.152,1.336L21.84,15a9.984,9.984,0,0,1-.759,1.837l.6.82a1.355,1.355,0,0,1-.132,1.762l-2.137,2.137a1.355,1.355,0,0,1-1.762.131l-.82-.6A9.993,9.993,0,0,1,15,21.84l-.154,1.008A1.352,1.352,0,0,1,13.506,24H10.483a1.355,1.355,0,0,1-1.335-1.153L9,21.84a10,10,0,0,1-1.837-.759l-.82.6a1.355,1.355,0,0,1-1.762-.131L2.442,19.417a1.353,1.353,0,0,1-.131-1.762l.6-.82A9.984,9.984,0,0,1,2.156,15l-1-.15A1.351,1.351,0,0,1,0,13.512V10.489a1.356,1.356,0,0,1,1.148-1.34L2.156,9A10.133,10.133,0,0,1,2.92,7.166l-.6-.82a1.355,1.355,0,0,1,.131-1.762L4.578,2.442a1.355,1.355,0,0,1,1.762-.131l.82.6A9.988,9.988,0,0,1,9,2.156l.15-1A1.348,1.348,0,0,1,10.483,0h3.023a1.353,1.353,0,0,1,1.341,1.148L15,2.156a10.682,10.682,0,0,1,1.837.759l.82-.6a1.355,1.355,0,0,1,1.762.131l2.137,2.137a1.355,1.355,0,0,1,.132,1.762l-.6.82A9.988,9.988,0,0,1,21.84,9Zm-1.4,4.476,1.843-.277V10.8l-1.843-.277a8.516,8.516,0,0,0-1.429-3.449l1.106-1.5-1.7-1.7-1.5,1.106a8.527,8.527,0,0,0-3.45-1.429l-.276-1.842h-2.4l-.277,1.842A8.529,8.529,0,0,0,7.069,4.982l-1.5-1.106-1.7,1.7,1.1,1.5a8.536,8.536,0,0,0-1.429,3.449L1.707,10.8v2.4l1.842.277a8.536,8.536,0,0,0,1.429,3.449l-1.1,1.5,1.7,1.7,1.5-1.106a8.529,8.529,0,0,0,3.448,1.429l.277,1.842h2.4l.276-1.842a8.528,8.528,0,0,0,3.45-1.429l1.5,1.106,1.7-1.7-1.106-1.5A8.516,8.516,0,0,0,20.439,13.474Z"
        fillRule="evenodd"
        fill="currentColor"
      />
    </svg>
  );
}
