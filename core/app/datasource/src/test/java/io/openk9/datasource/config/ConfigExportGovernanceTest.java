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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.model.util.ExportIgnore;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure guard (no Quarkus boot, so it runs in a normal build) over the export side of
 * the import/export feature. It asserts the two governance promises the exporter
 * relies on without booting a persistence unit, so a violation fails an ordinary
 * build instead of hiding in a {@code @QuarkusTest} that the default build excludes.
 * <p>
 * Persistent entities are enumerated from the module's own Jandex index, read straight
 * from bytecode: that is the classpath scan the metamodel used to provide, minus the
 * boot. A newly added {@code @Entity} therefore fails the build until it is either
 * declared in {@link ConfigEntityType} or annotated {@link ExportIgnore}.
 * <p>
 * The index is a build artifact the jandex-maven-plugin regenerates at
 * {@code process-classes}, before {@code test}: a Maven build (and CI) always scans
 * the freshly compiled classes, so the guard reacts there. Running this test in
 * isolation from an IDE reads whatever index the last Maven build wrote — not the
 * IDE's own compilation — so after adding an entity, trigger a Maven build to see the
 * guard react.
 */
public class ConfigExportGovernanceTest {

	private static final DotName ENTITY =
		DotName.createSimple("jakarta.persistence.Entity");
	private static final DotName EXPORT_IGNORE =
		DotName.createSimple(ExportIgnore.class.getName());

	@Test
	void every_persistent_entity_is_exported_or_explicitly_ignored() throws IOException {
		// Opt-out by governance: every JPA entity must be either exportable
		// (declared in ConfigEntityType) or explicitly @ExportIgnore, so a newly
		// added entity fails the build until a deliberate choice is made.
		Set<String> exportable = new HashSet<>();
		for (ConfigEntityType type : ConfigEntityType.values()) {
			exportable.add(type.getEntityType().getName());
		}

		List<String> undecided = new ArrayList<>();
		for (AnnotationInstance annotation : moduleIndex().getAnnotations(ENTITY)) {
			ClassInfo entity = annotation.target().asClass();
			if (!exportable.contains(entity.name().toString())
				&& entity.declaredAnnotation(EXPORT_IGNORE) == null) {

				undecided.add(entity.simpleName());
			}
		}

		assertTrue(
			undecided.isEmpty(),
			"every persistent entity must be exportable (declared in "
				+ "ConfigEntityType) or annotated @ExportIgnore; undecided: "
				+ undecided);
	}

	@Test
	void every_exportable_type_has_a_mapper_dto_method() {
		// The generic collector resolves ConfigEntityMapper.dto(entityClass)
		// reflectively; assert the overload exists for every registered type (join
		// types included: they are exported through their Representation dto).
		List<String> missing = new ArrayList<>();
		for (ConfigEntityType type : ConfigEntityType.values()) {
			try {
				ConfigEntityMapper.class.getMethod("dto", type.getEntityType());
			}
			catch (NoSuchMethodException e) {
				missing.add(type.getEntityType().getSimpleName());
			}
		}

		assertTrue(
			missing.isEmpty(),
			"ConfigEntityMapper is missing a dto(...) overload for: " + missing);
	}

	/**
	 * Reads this module's Jandex index, pinned to the datasource output directory via
	 * a datasource class' code source so a {@code jandex.idx} shipped by a dependency
	 * jar can never be picked instead.
	 */
	private static Index moduleIndex() throws IOException {
		Path classesRoot;
		try {
			classesRoot = Path.of(ConfigEntityType.class.getProtectionDomain()
				.getCodeSource().getLocation().toURI());
		}
		catch (URISyntaxException e) {
			throw new IllegalStateException("cannot locate the module classes root", e);
		}

		Path indexFile = classesRoot.resolve("META-INF/jandex.idx");
		try (InputStream in = Files.newInputStream(indexFile)) {
			return new IndexReader(in).read();
		}
	}

}
