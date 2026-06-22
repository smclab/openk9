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

import java.util.List;
import java.util.Map;

import io.openk9.datasource.config.model.ConfigEntity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit tests (no Quarkus, no Docker) for the dependency ordering: every
 * referenced handle must come before the entity that references it, including
 * the tricky shapes of the real config graph (self-reference, composite-key
 * joins, shared nodes) and a hard failure on genuine cycles.
 */
class ConfigEntitySorterTest {

	private final ConfigEntitySorter sorter = new ConfigEntitySorter();

	@Test
	void shouldOrderDependenciesBeforeDependents() {
		List<ConfigEntity> ordered = sorter.sort(List.of(
			node("a", "b"),
			node("b", "c"),
			node("c")
		));

		assertTrue(indexOf(ordered, "c") < indexOf(ordered, "b"));
		assertTrue(indexOf(ordered, "b") < indexOf(ordered, "a"));
	}

	@Test
	void shouldOrderCompositeJoinAfterBothEndpoints() {
		List<ConfigEntity> ordered = sorter.sort(List.of(
			node("epi", "ep", "ei"),
			node("ep"),
			node("ei")
		));

		assertTrue(indexOf(ordered, "ep") < indexOf(ordered, "epi"));
		assertTrue(indexOf(ordered, "ei") < indexOf(ordered, "epi"));
	}

	@Test
	void shouldHandleSelfReferenceAndSharedNodesOfRealGraph() {
		// dt <- dtfParent <- dtfChild ; dtfChild also reached by annotator and acl
		List<ConfigEntity> ordered = sorter.sort(List.of(
			node("annotator", "dtfChild"),
			node("acl", "pd", "dtfChild"),
			node("dtfChild", "dt", "dtfParent"),
			node("dtfParent", "dt"),
			node("dt"),
			node("pd")
		));

		assertTrue(indexOf(ordered, "dt") < indexOf(ordered, "dtfParent"));
		assertTrue(indexOf(ordered, "dtfParent") < indexOf(ordered, "dtfChild"));
		assertTrue(indexOf(ordered, "dtfChild") < indexOf(ordered, "annotator"));
		assertTrue(indexOf(ordered, "dtfChild") < indexOf(ordered, "acl"));
		assertTrue(indexOf(ordered, "pd") < indexOf(ordered, "acl"));
		assertEquals(6, ordered.size());
	}

	@Test
	void shouldIgnoreReferencesToHandlesOutsideThePackage() {
		List<ConfigEntity> ordered = sorter.sort(List.of(
			node("a", "external-handle")
		));

		assertEquals(1, ordered.size());
	}

	@Test
	void shouldThrowOnCycle() {
		ConfigEntitySorter.CyclicDependencyException exception = assertThrows(
			ConfigEntitySorter.CyclicDependencyException.class,
			() -> sorter.sort(List.of(
				node("x", "y"),
				node("y", "x")
			))
		);

		assertTrue(exception.getInvolvedRefs().containsAll(List.of("x", "y")));
	}

	private static ConfigEntity node(String ref, String... dependencyHandles) {
		Map<String, List<String>> references = dependencyHandles.length == 0
			? Map.of()
			: Map.of("dependsOn", List.of(dependencyHandles));

		return new ConfigEntity(ref, null, null, null, references, null);
	}

	private static int indexOf(List<ConfigEntity> ordered, String ref) {
		for (int i = 0; i < ordered.size(); i++) {
			if (ref.equals(ordered.get(i).getRef())) {
				return i;
			}
		}
		return -1;
	}

}
