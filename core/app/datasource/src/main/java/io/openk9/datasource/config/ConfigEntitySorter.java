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

package io.openk9.datasource.config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openk9.datasource.config.model.ConfigEntity;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Orders the entities of a configuration package so that every referenced
 * entity precedes the entities that reference it (dependencies first). On
 * import this guarantees that, when an entity is applied, all the entities it
 * points to already exist and can be resolved.
 * <p>
 * The package only carries outgoing (owning-side) references, so the resulting
 * graph is acyclic: the self-reference of {@code DocTypeField.parent} and the
 * composite-key joins ({@code EnrichPipelineItem}, {@code AclMapping}) are
 * ordered naturally by a Kahn sort over the package instances. A genuine cycle
 * is reported rather than silently mis-ordered.
 */
@ApplicationScoped
public class ConfigEntitySorter {

	public List<ConfigEntity> sort(List<ConfigEntity> entities) {

		Map<String, ConfigEntity> byRef = indexByRef(entities);

		Map<String, Integer> indegree = new LinkedHashMap<>();
		Map<String, Set<String>> dependents = new HashMap<>();

		for (ConfigEntity entity : entities) {
			String ref = entity.getRef();
			Set<String> dependencies = dependencies(entity, byRef);
			indegree.put(ref, dependencies.size());
			for (String dependency : dependencies) {
				dependents
					.computeIfAbsent(dependency, key -> new LinkedHashSet<>())
					.add(ref);
			}
		}

		Deque<String> ready = new ArrayDeque<>();
		for (ConfigEntity entity : entities) {
			if (indegree.get(entity.getRef()) == 0) {
				ready.add(entity.getRef());
			}
		}

		List<ConfigEntity> ordered = new ArrayList<>(entities.size());
		while (!ready.isEmpty()) {
			String ref = ready.poll();
			ordered.add(byRef.get(ref));
			for (String dependent : dependents.getOrDefault(ref, Set.of())) {
				int remaining = indegree.get(dependent) - 1;
				indegree.put(dependent, remaining);
				if (remaining == 0) {
					ready.add(dependent);
				}
			}
		}

		if (ordered.size() != byRef.size()) {
			throw new CyclicDependencyException(unresolved(indegree));
		}

		return ordered;
	}

	private static Map<String, ConfigEntity> indexByRef(List<ConfigEntity> entities) {
		Map<String, ConfigEntity> byRef = new LinkedHashMap<>();
		for (ConfigEntity entity : entities) {
			String ref = entity.getRef();
			if (ref == null) {
				throw new IllegalArgumentException(
					"ConfigEntity without a ref handle cannot be ordered");
			}
			if (byRef.put(ref, entity) != null) {
				throw new IllegalArgumentException(
					"Duplicate ConfigEntity handle: " + ref);
			}
		}
		return byRef;
	}

	private static Set<String> dependencies(
		ConfigEntity entity, Map<String, ConfigEntity> byRef) {

		Set<String> dependencies = new LinkedHashSet<>();

		Map<String, List<String>> references = entity.getReferences();
		if (references == null) {
			return dependencies;
		}

		String ref = entity.getRef();
		for (List<String> handles : references.values()) {
			if (handles == null) {
				continue;
			}
			for (String handle : handles) {
				if (handle != null
					&& !handle.equals(ref)
					&& byRef.containsKey(handle)) {

					dependencies.add(handle);
				}
			}
		}

		return dependencies;
	}

	private static List<String> unresolved(Map<String, Integer> indegree) {
		List<String> unresolved = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : indegree.entrySet()) {
			if (entry.getValue() > 0) {
				unresolved.add(entry.getKey());
			}
		}
		return unresolved;
	}

	/**
	 * Raised when the package references form a cycle and no valid import order
	 * exists. Aborts the (transactional) import.
	 */
	public static class CyclicDependencyException extends RuntimeException {

		private final List<String> involvedRefs;

		public CyclicDependencyException(List<String> involvedRefs) {
			super("Cyclic dependency among config entities: " + involvedRefs);
			this.involvedRefs = List.copyOf(involvedRefs);
		}

		public List<String> getInvolvedRefs() {
			return involvedRefs;
		}

	}

}
