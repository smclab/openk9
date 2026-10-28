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
import { describe, expect, it } from "vitest";
import { addToOrderedSelection, removeFromOrderedSelection } from "./orderedSelection";

const title = { id: "1", name: "title" };
const category = { id: "2", name: "category" };
const tags = { id: "3", name: "tags" };

describe("ordered selection", () => {
  it("keeps the order of choice, not the order of the ids", () => {
    const selection = addToOrderedSelection(addToOrderedSelection([], category), title);

    expect(selection).toEqual([category, title]);
  });

  it("ignores an empty value and a value already chosen", () => {
    const selection = [title, category];

    expect(addToOrderedSelection(selection, { id: "", name: "" })).toBe(selection);
    expect(addToOrderedSelection(selection, title)).toBe(selection);
  });

  it("removes a value and keeps the order of the others", () => {
    expect(removeFromOrderedSelection([title, category, tags], "2")).toEqual([title, tags]);
  });
});
