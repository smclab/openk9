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
import { selectableTypes } from "./exportTypes";

describe("the registry decides what can be exported", () => {
  test("a type the backend marks unselectable is not offered", () => {
    expect(
      selectableTypes([
        { name: "DATASOURCE", selectable: true },
        { name: "ACL_MAPPING", selectable: false },
        { name: "ENRICH_PIPELINE_ITEM", selectable: false },
      ]),
    ).toEqual(["DATASOURCE"]);
  });

  test("the order the backend declares is the order the tab shows", () => {
    expect(
      selectableTypes([
        { name: "PLUGIN_DRIVER", selectable: true },
        { name: "DATASOURCE", selectable: true },
      ]),
    ).toEqual(["PLUGIN_DRIVER", "DATASOURCE"]);
  });

  test("an entry with no name is skipped", () => {
    expect(selectableTypes([{ selectable: true }, { name: "BUCKET", selectable: true }])).toEqual(["BUCKET"]);
  });
});
