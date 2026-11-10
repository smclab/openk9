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
import { fieldsToPrefill, IntentFields } from "./intentPrefill";

const DEFAULTS: IntentFields = {
  knnIndex: false,
  chunkType: "CHUNK_TYPE_CHARACTER_TEXT_SPLITTER",
  chunkWindowSize: 0,
  embeddingJsonConfig: "{}",
  embeddingDocTypeFieldId: null,
  docTypeIds: [],
  settings: "{}",
};

const DATASOURCE_A: IntentFields = {
  knnIndex: true,
  chunkType: "CHUNK_TYPE_SEMANTIC_SPLITTER",
  chunkWindowSize: 2,
  embeddingJsonConfig: '{"model":"a"}',
  embeddingDocTypeFieldId: { id: "11", name: "web.content" },
  docTypeIds: [1, 2],
  settings: '{\n  "index": {}\n}',
};

// a datasource without a vector index, so most of its intent is the defaults
const DATASOURCE_B: IntentFields = { ...DEFAULTS, docTypeIds: [3] };

describe("a new dataIndex starts from the intent of its datasource", () => {
  test("the fields still at their default take the intent", () => {
    expect(fieldsToPrefill({ current: DEFAULTS, intent: DATASOURCE_A, defaults: DEFAULTS, previous: null })).toEqual(
      DATASOURCE_A,
    );
  });

  test("what the user typed is kept", () => {
    const current = { ...DEFAULTS, chunkWindowSize: 5, docTypeIds: [9] };

    const changes = fieldsToPrefill({ current, intent: DATASOURCE_A, defaults: DEFAULTS, previous: null });

    expect(changes).not.toHaveProperty("chunkWindowSize");
    expect(changes).not.toHaveProperty("docTypeIds");
    expect(changes.knnIndex).toBe(true);
  });

  test("an emptied field counts as untouched", () => {
    const current = { ...DEFAULTS, chunkType: null, embeddingJsonConfig: null };

    expect(fieldsToPrefill({ current, intent: DATASOURCE_A, defaults: DEFAULTS, previous: null })).toMatchObject({
      chunkType: DATASOURCE_A.chunkType,
      embeddingJsonConfig: DATASOURCE_A.embeddingJsonConfig,
    });
  });
});

describe("choosing another datasource", () => {
  test("replaces what the previous one filled in, back to the default where the new one says nothing", () => {
    expect(
      fieldsToPrefill({ current: DATASOURCE_A, intent: DATASOURCE_B, defaults: DEFAULTS, previous: DATASOURCE_A }),
    ).toEqual(DATASOURCE_B);
  });

  test("keeps what the user changed after the previous one", () => {
    const current = { ...DATASOURCE_A, chunkWindowSize: 5 };

    const changes = fieldsToPrefill({ current, intent: DATASOURCE_B, defaults: DEFAULTS, previous: DATASOURCE_A });

    expect(changes).not.toHaveProperty("chunkWindowSize");
    expect(changes.knnIndex).toBe(false);
  });
});
