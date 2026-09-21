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
import type { ConfigEntityType, ConfigEntityTypeResponseDto } from "openapi-generated";

/**
 * The configuration types the export tab offers, read from
 * `/v1/config/entity-types` and kept in the order the backend declares them.
 *
 * Two of them are filtered out: `ACL_MAPPING` and `ENRICH_PIPELINE_ITEM` are join
 * entities with a composite key, and the exporter adds them by itself once both
 * ends of the relationship are selected, so naming one as a seed means nothing.
 * Which they are is the backend's to know, which is why the list is asked for
 * rather than written down here.
 *
 * The cast is the one place this file trusts the server: `ConfigEntityType` is a
 * TypeScript union erased at runtime, generated from the very spec that serves
 * this endpoint. A name the two disagree on means the client is stale and has to
 * be regenerated, not that the value needs re-checking here.
 */
export function selectableTypes(entityTypes: ReadonlyArray<ConfigEntityTypeResponseDto>): ConfigEntityType[] {
  return entityTypes
    .filter((entityType) => entityType.name && entityType.selectable !== false)
    .map((entityType) => entityType.name as ConfigEntityType);
}
