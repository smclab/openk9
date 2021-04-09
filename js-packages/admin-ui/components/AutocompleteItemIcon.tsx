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

import ClayDropDown from "@clayui/drop-down";
import fuzzy from "fuzzy";
import React from "react";

export interface IProps extends React.ComponentProps<typeof ClayDropDown.Item> {
  innerRef?: React.Ref<HTMLAnchorElement>;

  /**
   * Match is the string that will be compared with value.
   */
  match?: string;

  /**
   * Value is the string that will be compared to the characters of
   * the match property.
   */
  value: string;

  icon?: JSX.Element;
}

const optionsFuzzy = { post: "</strong>", pre: "<strong>" };

export const AutocompleteItemIcon = React.forwardRef<HTMLLIElement, IProps>(
  ({ innerRef, icon, match = "", value, ...otherProps }: IProps, ref) => {
    const fuzzyMatch = fuzzy.match(match, value, optionsFuzzy);

    return (
      <ClayDropDown.Item {...otherProps} innerRef={innerRef} ref={ref}>
        <span className="inline-item inline-item-before">{icon}</span>

        {match && fuzzyMatch ? (
          <div
            dangerouslySetInnerHTML={{
              __html: fuzzyMatch.rendered,
            }}
          />
        ) : (
          value
        )}
      </ClayDropDown.Item>
    );
  },
);
