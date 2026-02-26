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

package io.openk9.datasource.pipeline.service.dto;

/**
 * Defines the scheduling strategy for pipeline execution during the ingestion process.
 * <p>
 * Possible scheduling types include:
 * <ul>
 *   <li>{@code ENRICH}: Schedule enrichment pipeline execution</li>
 *   <li>{@code EMBEDDING}: Schedule embedding vector generation</li>
 *   <li>{@code ENRICH_EMBEDDING}: Schedule sequential enrichment and embedding generation</li>
 * </ul>
 */
public enum SchedulingType {
	ENRICH,
	EMBEDDING,
	ENRICH_EMBEDDING
}
