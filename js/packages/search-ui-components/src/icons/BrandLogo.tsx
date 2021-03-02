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

export function BrandLogo({ size = 128, className = "" }) {
  return (
    <svg width={size} height={size} className={className} viewBox="0 0 97 94">
      <g transform="translate(-273 -246)">
        <path
          d="M326.332,285.609a2.026,2.026,0,0,0,1.539-.645,2.111,2.111,0,0,0,.645-1.539,2.023,2.023,0,0,0-.645-1.535,2.115,2.115,0,0,0-1.539-.648,2.057,2.057,0,0,0-1.535.648,2.091,2.091,0,0,0-.648,1.535,2.057,2.057,0,0,0,.648,1.539,2.017,2.017,0,0,0,1.535.645"
          fill="currentColor"
        />
        <path
          d="M317.676-311.331a81.97,81.97,0,0,0-2.844-11.426,2.424,2.424,0,0,0-2.676-1.637,17.919,17.919,0,0,0-9.605,4.961c-2.953,2.9-5.062,7.414-6.75,11.48a4.319,4.319,0,0,0,.141,3.512,18.853,18.853,0,0,0,7.082,7.48,18.491,18.491,0,0,0,5.227,2.422c.535.156,1.086.289,1.645.4a23.015,23.015,0,0,0,3.711.379c.949.027,2.715.074,3.582-1.582a3.384,3.384,0,0,0,.277-.723C319-301.506,318.383-307.081,317.676-311.331Zm0,0"
          transform="translate(0 595.276)"
          fill="none"
          stroke="currentColor"
          strokeMiterlimit="10"
          strokeWidth="6"
        />
        <path
          d="M313.578-324.174c1.414.34,8.094,1.82,9.469,2.324,1.09.4,2.184.781,3.262,1.211,2.23.887,4.57,1.867,5.938,3.949.289.438.531.9.852,1.32.742.961,1.711,1,2.84,1.125l3.559.41c2.516.285,5.027.578,7.539.863.379.043.762.09,1.141.129a3.729,3.729,0,0,1,2.273,1.457,3.258,3.258,0,0,1,.82,2.461,19.486,19.486,0,0,1-8.984,16.031c-3.691,2.461-6.922,1.152-12.066,1.859a3.886,3.886,0,0,0-2.312,1.2,3.726,3.726,0,0,0-.785.969,3.133,3.133,0,0,0-.406,1.3v.109l.742,15.645a18.156,18.156,0,0,0,3.316,8.016,38.047,38.047,0,0,1,3.246,5.555,45.1,45.1,0,0,0,33-43.469,45.143,45.143,0,0,0-45.141-45.039,45.459,45.459,0,0,0-6.883.52,45.14,45.14,0,0,0-38.238,44.594c0,9.918,4.2,17.492,9.609,24.949-.18-.246,3.793-2.684,4.133-2.9a34.678,34.678,0,0,0,5.977-4.6,10.516,10.516,0,0,0,3.418-5.395c.09-.453.152-.91.2-1.367.082-.824.105-1.656.109-2.484"
          transform="translate(0 595.276)"
          fill="none"
          stroke="currentColor"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeMiterlimit="10"
          strokeWidth="6"
        />
      </g>
    </svg>
  );
}
