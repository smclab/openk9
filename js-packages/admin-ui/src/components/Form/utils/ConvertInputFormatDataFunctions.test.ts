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
import { convertToBackEndFormatData, convertToInputFormat } from "./ConvertInputFormatDataFunctions";

process.env.TZ = "Europe/Rome";

describe("the partial reindex date field works in the browser's local time", () => {
  test("the last ingestion is shown in local time", () => {
    expect(convertToInputFormat("2026-09-29T15:25:17.252Z")).toBe("2026-09-29T17:25:17");
  });

  test("sending the proposed value back sends the last ingestion", () => {
    expect(convertToBackEndFormatData(convertToInputFormat("2026-09-29T15:25:17.252Z"))).toBe(
      "2026-09-29T15:25:17.000Z",
    );
  });

  test("the date picked in the field is the date sent", () => {
    expect(convertToBackEndFormatData("2026-10-01T10:00:00")).toBe("2026-10-01T08:00:00.000Z");
  });

  test("in winter the offset is one hour", () => {
    expect(convertToBackEndFormatData("2026-01-15T10:00:00")).toBe("2026-01-15T09:00:00.000Z");
  });

  test("a UTC evening is already the next day in local time", () => {
    expect(convertToInputFormat("2026-09-29T22:30:00Z")).toBe("2026-09-30T00:30:00");
  });

  test("a datasource never ingested proposes the epoch and can still be sent", () => {
    expect(convertToBackEndFormatData(convertToInputFormat(null))).toBe("1970-01-01T00:00:00.000Z");
  });
});
