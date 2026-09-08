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
import { exportGroups, typesOfGroups } from "./exportTypeGroups";

/**
 * The compiler already rejects a `ConfigEntityType` that no group claims. What it
 * cannot see is the other half of the invariant: that every group the grid
 * renders actually carries types, and that the selection maps onto them.
 */
describe("export type groups", () => {
  test("every group carries at least one type", () => {
    const empty = exportGroups.filter((group) => group.types.length === 0);
    expect(empty.map((group) => group.id)).toEqual([]);
  });

  test("no type is claimed by two groups", () => {
    const types = exportGroups.flatMap((group) => group.types);
    expect(types).toHaveLength(new Set(types).size);
  });

  test("no selection asks for no type, so the backend exports the whole tenant", () => {
    expect(typesOfGroups(new Set())).toEqual([]);
  });

  test("a selection asks for exactly the types of the groups picked", () => {
    const [first, , third] = exportGroups;
    expect(typesOfGroups(new Set([first.id, third.id])).sort()).toEqual([...first.types, ...third.types].sort());
  });

  test("selecting every group asks for every type", () => {
    const all = typesOfGroups(new Set(exportGroups.map((group) => group.id)));
    expect(all.sort()).toEqual(exportGroups.flatMap((group) => group.types).sort());
  });
});
