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
import { SelectedValue } from "./AutocompleteDropdown";

/**
 * Appends a value to an ordered selection: the order is the order of choice.
 * An empty value, or one already chosen, leaves the selection as it is.
 */
export function addToOrderedSelection(selection: SelectedValue[], value: SelectedValue): SelectedValue[] {
  if (!value.id || selection.some((item) => item.id === value.id)) {
    return selection;
  }

  return [...selection, value];
}

/** Removes a value from an ordered selection, keeping the order of the others. */
export function removeFromOrderedSelection(selection: SelectedValue[], id: string): SelectedValue[] {
  return selection.filter((item) => item.id !== id);
}
