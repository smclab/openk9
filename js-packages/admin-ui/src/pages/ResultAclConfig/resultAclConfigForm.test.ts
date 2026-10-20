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
import { describe, expect, test } from "vitest";
import { toResultAclConfigDto, toSelectableFieldOptions } from "./resultAclConfigForm";

const TITLE = { id: "1", name: "web.title" };
const CONTENT = { id: "2", name: "web.content" };

describe("the page offers the selectable and the visible fields", () => {
  test("a visible field is offered although the selectable fields query leaves it out", () => {
    expect(toSelectableFieldOptions([CONTENT], [TITLE])).toEqual([
      { value: "2", label: "web.content" },
      { value: "1", label: "web.title" },
    ]);
  });

  test("a field in both lists is offered once", () => {
    expect(toSelectableFieldOptions([TITLE], [TITLE])).toEqual([{ value: "1", label: "web.title" }]);
  });

  test("missing lists and entries are skipped", () => {
    expect(toSelectableFieldOptions(null, [null, TITLE])).toEqual([{ value: "1", label: "web.title" }]);
  });
});

describe("the page saves the whole configuration", () => {
  test("a disabled mode is sent, so a full update does not keep it on", () => {
    expect(toResultAclConfigDto({ enabled: false, superUserRoles: [], visibleFields: [] })).toHaveProperty(
      "enabled",
      false,
    );
  });

  test("the roles and the ids of the visible fields are sent", () => {
    const dto = toResultAclConfigDto({
      enabled: true,
      superUserRoles: ["auditor", "compliance"],
      visibleFields: [
        { value: "1", label: "web.title" },
        { value: "2", label: "web.content" },
      ],
    });

    expect(dto.enabled).toBe(true);
    expect(dto.superUserRoles).toEqual(["auditor", "compliance"]);
    expect(dto.visibleFieldIds).toEqual([1, 2]);
  });

  test("empty lists are sent, so removing every role or field clears them", () => {
    const dto = toResultAclConfigDto({ enabled: true, superUserRoles: [], visibleFields: [] });

    expect(dto.superUserRoles).toEqual([]);
    expect(dto.visibleFieldIds).toEqual([]);
  });

  test("a name is always sent, since the server requires one", () => {
    expect(toResultAclConfigDto({ enabled: false, superUserRoles: [], visibleFields: [] }).name).not.toBe("");
  });
});
