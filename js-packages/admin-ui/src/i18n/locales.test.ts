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
import { supportedLanguages } from "./index";

// Every file under locales/, keyed by language code: a language added later
// is checked without touching this test.
const locales: Record<string, unknown> = Object.fromEntries(
  Object.entries(import.meta.glob<unknown>("./locales/*.json", { eager: true, import: "default" })).map(
    ([path, content]) => [path.replace(/^.*\/(\w+)\.json$/, "$1"), content],
  ),
);

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function flatten(value: unknown, prefix = ""): Map<string, string> {
  const entries = new Map<string, string>();
  if (!isRecord(value)) return entries;
  for (const [key, child] of Object.entries(value)) {
    if (isRecord(child)) {
      for (const [childPath, text] of flatten(child, `${prefix}${key}.`)) entries.set(childPath, text);
    } else {
      entries.set(`${prefix}${key}`, String(child));
    }
  }
  return entries;
}

// What a translation must carry over untouched: {{placeholders}} and <tags>.
function markers(text: string): string[] {
  const placeholders = text.match(/\{\{[^}]*\}\}/g) ?? [];
  const tags = text.match(/<\/?[a-zA-Z0-9]+>/g) ?? [];
  return [...placeholders, ...tags].sort();
}

const english = flatten(locales.en);
const translations = Object.keys(locales).filter((language) => language !== "en");

describe("every panel language has the same strings as English", () => {
  test("each locale file is a supported language, and each supported language has a file", () => {
    expect(Object.keys(locales).sort()).toEqual([...supportedLanguages].sort());
  });

  test.each(translations)("%s has exactly the keys of en.json", (language) => {
    const keys = flatten(locales[language]);
    const missing = [...english.keys()].filter((key) => !keys.has(key));
    const extra = [...keys.keys()].filter((key) => !english.has(key));
    expect({ missing, extra }).toEqual({ missing: [], extra: [] });
  });

  test.each(translations)("%s keeps the placeholders and tags of en.json", (language) => {
    const keys = flatten(locales[language]);
    const changed = [...english].flatMap(([key, text]) => {
      const translated = keys.get(key);
      return translated !== undefined && markers(translated).join() !== markers(text).join()
        ? [{ key, expected: markers(text), actual: markers(translated) }]
        : [];
    });
    expect(changed).toEqual([]);
  });
});
