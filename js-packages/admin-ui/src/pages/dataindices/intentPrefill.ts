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
import type { DataindexData } from "./SaveDataindex";

/** The fields of a new dataIndex that the intent of its datasource fills in. */
export type IntentFields = Pick<
  DataindexData,
  | "knnIndex"
  | "chunkType"
  | "chunkWindowSize"
  | "embeddingJsonConfig"
  | "embeddingDocTypeFieldId"
  | "docTypeIds"
  | "settings"
>;

const INTENT_FIELDS: ReadonlyArray<keyof IntentFields> = [
  "knnIndex",
  "chunkType",
  "chunkWindowSize",
  "embeddingJsonConfig",
  "embeddingDocTypeFieldId",
  "docTypeIds",
  "settings",
];

const same = (a: unknown, b: unknown) => JSON.stringify(a) === JSON.stringify(b);

/**
 * Picks the fields to fill from the intent of the chosen datasource. A field is
 * filled only while the user has not touched it: empty, at its default, or still
 * at what the previously chosen datasource filled in. Choosing another datasource
 * therefore replaces the previous intent, and what the user typed stays.
 */
export function fieldsToPrefill({
  current,
  intent,
  defaults,
  previous,
}: {
  current: IntentFields;
  intent: IntentFields;
  defaults: IntentFields;
  previous: IntentFields | null;
}): Partial<IntentFields> {
  const changes: Partial<IntentFields> = {};

  const fill = <K extends keyof IntentFields>(field: K) => {
    const value = current[field];
    const untouched =
      value == null || same(value, defaults[field]) || (previous !== null && same(value, previous[field]));
    if (untouched && !same(value, intent[field])) changes[field] = intent[field];
  };
  INTENT_FIELDS.forEach(fill);

  return changes;
}
