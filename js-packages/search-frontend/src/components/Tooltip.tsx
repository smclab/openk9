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
import { css } from "styled-components";

type TooltipProps = {
  children: React.ReactNode;
  description: string;
};
export function Tooltip({ children, description }: TooltipProps) {
  const [isOpen, setIsOpen] = React.useState(false);
  return (
    <div
      css={css`
        position: relative;
      `}
      onMouseEnter={() => setIsOpen(true)}
      onMouseLeave={() => setIsOpen(false)}
    >
      {children}
      {isOpen && (
        <div
          className="openk9-tooltip"
          css={css`
            position: absolute;
            z-index: 1;
            right: 0px;
            padding: 8px 16px;
            background-color: var(
              --openk9-embeddable-search--secondary-background-color
            );
            width: 200px;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 4px;
          `}
        >
          {description}
        </div>
      )}
    </div>
  );
}

