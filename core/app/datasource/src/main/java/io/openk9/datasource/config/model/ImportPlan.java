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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The outcome of matching a {@link ConfigPackage} against a target tenant: the
 * per-entity {@link PlannedAction}s in topological (dependency-first) order,
 * ready to be applied by the importer. This is a read-only preview; it performs
 * no writes.
 * <p>
 * Composite-key join entities ({@code EnrichPipelineItem}, {@code AclMapping})
 * carry no action here: they are rebuilt from their endpoints by the importer.
 */
public class ImportPlan {

	private final List<PlannedAction> actions;

	public ImportPlan(List<PlannedAction> actions) {
		this.actions = List.copyOf(actions);
	}

	/**
	 * The planned actions indexed by package handle.
	 */
	public Map<String, PlannedAction> byRef() {
		Map<String, PlannedAction> byRef = new LinkedHashMap<>();
		for (PlannedAction action : actions) {
			byRef.put(action.ref(), action);
		}
		return byRef;
	}

	/**
	 * How many actions carry the given kind (CREATE / OVERWRITE / SKIP).
	 */
	public long count(PlannedAction.Action kind) {
		return actions.stream().filter(action -> action.action() == kind).count();
	}

	/**
	 * The planned actions, in dependency order.
	 */
	public List<PlannedAction> getActions() {
		return actions;
	}

}
