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
import type { ConfigPackage } from "openapi-generated";
import { configFileName, parseConfigPackage, redactedEntities } from "./configPackage";

const VALID: ConfigPackage = {
  schemaVersion: "1.0",
  metadata: { sourceVirtualHost: "demo.openk9.localhost" },
  entities: [{ ref: "bucket:1", type: "BUCKET", key: "default", redactedFields: ["jsonConfig.apiKey"] }],
};

/**
 * The upload is validated in the browser because the backend answers an invalid
 * package with a Problem the operator cannot always act on — and a file that is
 * not a package should never be sent at all.
 */
describe("parseConfigPackage", () => {
  test("accepts a package with a schema version and entities", () => {
    const result = parseConfigPackage(JSON.stringify(VALID));
    expect(result.ok && result.parsed.schemaVersion).toBe("1.0");
    expect(result.ok && result.parsed.entities).toHaveLength(1);
  });

  test("refuses a file that is not JSON", () => {
    expect(parseConfigPackage("not json at all")).toEqual({ ok: false, error: "The file is not valid JSON." });
  });

  test("refuses valid JSON that is not a package", () => {
    expect(parseConfigPackage("[1, 2, 3]").ok).toBe(false);
    expect(parseConfigPackage(JSON.stringify({ ...VALID, schemaVersion: "" })).ok).toBe(false);
  });

  test("refuses a package with no entities to import", () => {
    expect(parseConfigPackage(JSON.stringify({ ...VALID, entities: [] })).ok).toBe(false);
  });

  test("refuses entities without a type or a key", () => {
    expect(parseConfigPackage(JSON.stringify({ ...VALID, entities: [{ ref: "bucket:1" }] })).ok).toBe(false);
  });
});

describe("the package as it is shown", () => {
  test("lists the redacted fields per entity", () => {
    expect(redactedEntities(VALID.entities ?? [])).toEqual([
      { type: "BUCKET", key: "default", fields: ["jsonConfig.apiKey"] },
    ]);
  });

  test("names the download after the source host and the schema version", () => {
    expect(configFileName(VALID)).toBe("openk9-config-demo.openk9.localhost-v1.0.json");
  });

  test("falls back when the host is blank and sanitises what it keeps", () => {
    expect(configFileName({ schemaVersion: "1.0", metadata: { sourceVirtualHost: "  " } })).toBe(
      "openk9-config-tenant-v1.0.json",
    );
    expect(configFileName({ schemaVersion: "1.0", metadata: { sourceVirtualHost: "a/b c" } })).toBe(
      "openk9-config-a-b-c-v1.0.json",
    );
  });
});
