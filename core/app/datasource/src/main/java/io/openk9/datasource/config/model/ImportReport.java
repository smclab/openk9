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

package io.openk9.datasource.config.model;

import java.util.List;
import java.util.Map;

/**
 * The result of an import request, whether previewed or applied. The plan-derived
 * fields (actions, counts, {@code missingReferences}, {@code secretsToReenter},
 * {@code blockingErrors}) are always populated so a dry-run returns the same
 * picture the apply would produce; {@code applied} tells whether writes were
 * committed and {@code resolvedIds} carries the {@code handle -> target id} map
 * only when they were.
 *
 * @param dryRun            the requested intent: {@code true} previews,
 *                          {@code false} applies
 * @param applied           whether the writes were committed
 * @param mode              how existing entities are resolved
 * @param actions           the per-entity planned action, in dependency order
 * @param created           number of entities that would be / were created
 * @param overwritten       number of entities that would be / were overwritten
 * @param skipped           number of entities left untouched
 * @param missingReferences references in the package pointing outside it
 * @param secretsToReenter  redacted secrets on created entities the operator must
 *                          re-enter (on overwrite the target's secret survives)
 * @param blockingErrors    reasons the import cannot proceed (e.g. a cycle)
 * @param resolvedIds       package handle to the target-tenant id it resolved to,
 *                          populated only on apply
 */
public record ImportReport(
	boolean dryRun,
	boolean applied,
	ImportMode mode,
	List<PlannedAction> actions,
	int created,
	int overwritten,
	int skipped,
	List<MissingReference> missingReferences,
	List<SecretToReenter> secretsToReenter,
	List<String> blockingErrors,
	Map<String, Long> resolvedIds) {

	public ImportReport {
		actions = List.copyOf(actions);
		missingReferences = List.copyOf(missingReferences);
		secretsToReenter = List.copyOf(secretsToReenter);
		blockingErrors = List.copyOf(blockingErrors);
		resolvedIds = Map.copyOf(resolvedIds);
	}

	/**
	 * A reference in the package whose target handle is not itself in the package
	 * (the dangling edge a shallow export leaves behind).
	 *
	 * @param fromRef      the handle of the entity holding the reference
	 * @param fromKey      the natural key of that entity
	 * @param relationship the reference field name
	 * @param targetHandle the missing target handle
	 */
	public record MissingReference(
		String fromRef, String fromKey, String relationship,
		String targetHandle) {}

	/**
	 * A created entity whose secrets were redacted at export and stripped on
	 * create, so the operator has to re-enter them afterwards.
	 *
	 * @param ref    the handle of the entity
	 * @param key    the natural key of the entity
	 * @param type   the entity type
	 * @param fields the redacted field paths to re-enter
	 */
	public record SecretToReenter(
		String ref, String key, ConfigEntityType type, List<String> fields) {

		public SecretToReenter {
			fields = List.copyOf(fields);
		}

	}

}
