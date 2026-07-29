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

/**
 * Reading and writing the configuration package on the browser side.
 *
 * The uploaded file is checked here before any request is sent: the backend
 * answers an invalid package with a bodiless 400, so a message the user can act
 * on has to be produced locally.
 */
import type { ConfigEntity, ConfigPackage } from "openapi-generated";

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isConfigEntity(value: unknown): value is ConfigEntity {
  return isRecord(value) && typeof value.type === "string" && typeof value.key === "string";
}

export type ParsedPackage = {
  /** A package that carries a schema version and at least one entity. */
  value: ConfigPackage;
  entities: ConfigEntity[];
  schemaVersion: string;
};

export type ParseResult = { ok: true; parsed: ParsedPackage } | { ok: false; error: string };

/**
 * Validates the shape the import endpoint requires: a schema version and a
 * non-empty entity list. The version itself is not matched against a constant —
 * the backend owns the supported range and rejects what it cannot apply.
 */
export function parseConfigPackage(text: string): ParseResult {
  let content: unknown;
  try {
    content = JSON.parse(text);
  } catch {
    return { ok: false, error: "The file is not valid JSON." };
  }

  if (!isRecord(content)) {
    return { ok: false, error: "The file does not contain a configuration package." };
  }

  const { schemaVersion, entities } = content;

  if (typeof schemaVersion !== "string" || schemaVersion.length === 0) {
    return { ok: false, error: "The package has no schemaVersion: it is not an OpenK9 configuration export." };
  }

  if (!Array.isArray(entities) || entities.length === 0) {
    return { ok: false, error: "The package contains no entities to import." };
  }

  if (!entities.every(isConfigEntity)) {
    return { ok: false, error: "The package contains entities without a type or a key." };
  }

  return { ok: true, parsed: { value: content, entities, schemaVersion } };
}

export type RedactedEntity = {
  type: string;
  key: string;
  fields: string[];
};

/**
 * The secrets to type in again after an import. The backend replaces them with
 * a placeholder on export and lists them per entity in `redactedFields`, so the
 * list is derived from the package that was just applied.
 */
export function redactedEntities(entities: ConfigEntity[]): RedactedEntity[] {
  return entities.flatMap((entity) => {
    const fields = entity.redactedFields ?? [];
    if (fields.length === 0 || entity.type === undefined || entity.key === undefined) {
      return [];
    }
    return [{ type: entity.type, key: entity.key, fields }];
  });
}

/** Everything outside this set is replaced, so the name stays a usable file name. */
const UNSAFE_IN_FILE_NAME = /[^a-zA-Z0-9.-]/g;

/**
 * A file name carrying the source tenant and the schema version. Both parts fall
 * back when blank — `??` alone would let an empty string through — and both are
 * sanitised, since they come from the package rather than from this code.
 */
export function configFileName({ metadata, schemaVersion }: ConfigPackage): string {
  const host = metadata?.sourceVirtualHost?.trim() || "tenant";
  const version = schemaVersion?.trim() || "unknown";
  return `openk9-config-${host.replace(UNSAFE_IN_FILE_NAME, "-")}-v${version.replace(UNSAFE_IN_FILE_NAME, "-")}.json`;
}

/** Hands the package to the browser as a download. */
export function downloadConfigPackage(configPackage: ConfigPackage): void {
  const blob = new Blob([JSON.stringify(configPackage, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = configFileName(configPackage);
  anchor.click();
  URL.revokeObjectURL(url);
}
