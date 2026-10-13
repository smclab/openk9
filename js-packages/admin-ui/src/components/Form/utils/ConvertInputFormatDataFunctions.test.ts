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
import { convertToBackEndFormatData, convertToInputFormat } from "./ConvertInputFormatDataFunctions";

// 2026.1.x has no Vitest and type-checks the tests with the Jest globals, which
// cannot pin the timezone: these checks hold in any timezone, and before the fix
// they failed in every zone but UTC.
describe("the partial reindex date field works in the browser's local time", () => {
  test("sending the proposed value back sends the last ingestion", () => {
    expect(convertToBackEndFormatData(convertToInputFormat("2026-09-29T15:25:17.252Z"))).toBe(
      "2026-09-29T15:25:17.000Z",
    );
  });

  test("in winter too", () => {
    expect(convertToBackEndFormatData(convertToInputFormat("2026-01-15T09:30:45.000Z"))).toBe(
      "2026-01-15T09:30:45.000Z",
    );
  });

  test("a datasource never ingested proposes the epoch and can still be sent", () => {
    expect(convertToBackEndFormatData(convertToInputFormat(null))).toBe("1970-01-01T00:00:00.000Z");
  });
});
