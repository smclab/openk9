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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.ImportMode;
import io.openk9.datasource.config.model.ImportPlan;
import io.openk9.datasource.config.model.PlannedAction;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.LanguageDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for the import matcher. The test boot already runs
 * {@code createDefault("public")}, so the tenant is populated; we export it with
 * the (already green) exporter to obtain a realistic package, then match it back
 * against the same tenant.
 */
@QuarkusTest
public class ConfigMatcherTest {

	private static final String TENANT_ID = "public";

	private static final String ENTITY_NAME_PREFIX = "ConfigMatcherTest - ";
	private static final String LANGUAGE_NAME = ENTITY_NAME_PREFIX + "language";
	private static final String DOC_TYPE_NAME = ENTITY_NAME_PREFIX + "doc-type";
	private static final String DOC_TYPE_FIELD_NAME = ENTITY_NAME_PREFIX + "field";

	@Inject
	ConfigExporter configExporter;

	@Inject
	ConfigMatcher configMatcher;

	@Test
	void every_exportable_type_has_a_derivable_natural_key() {
		// Governance twin of the exporter's: every non-join exportable type must
		// expose an identity derivable from the JPA model, or the build fails.
		List<String> undecided = new ArrayList<>();
		for (ConfigEntityType type : ConfigEntityType.values()) {
			Class<?> entityClass = type.getEntityType();
			if (ConfigMatcher.isJoinEntity(entityClass)) {
				continue;
			}
			if (configMatcher.naturalKeyOf(entityClass).isEmpty()) {
				undecided.add(entityClass.getSimpleName());
			}
		}

		assertTrue(
			undecided.isEmpty(),
			"every non-join exportable type must have a derivable natural key "
				+ "(@Table unique constraint or @Column(unique = true)); "
				+ "undecided: " + undecided);
	}

	@Test
	void round_trip_against_the_same_tenant_skips_every_entity() {
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		ImportPlan plan =
			configMatcher.plan(TENANT_ID, pkg, ImportMode.SKIP).await().indefinitely();

		// Every non-join entity already exists, so every action is SKIP with a
		// resolved existing id; no unexpected CREATE.
		assertEquals(0, plan.count(PlannedAction.Action.CREATE),
			"round-trip against the same tenant must not plan any CREATE");

		boolean sawDocTypeField = false;
		for (PlannedAction action : plan.getActions()) {
			assertSame(PlannedAction.Action.SKIP, action.action(), action.ref());
			assertNotNull(action.existingId(), action.ref());

			// Join entities are rebuilt by the importer, never planned here.
			assertNotSame(ConfigEntityType.ENRICH_PIPELINE_ITEM, action.type());
			assertNotSame(ConfigEntityType.ACL_MAPPING, action.type());

			if (action.type() == ConfigEntityType.DOC_TYPE_FIELD) {
				sawDocTypeField = true;
			}
		}

		// The composite-key match (fieldName + docType + parentDocTypeField) works.
		assertTrue(sawDocTypeField, "the default tenant must have doc type fields");
	}

	@Test
	void round_trip_in_overwrite_mode_overwrites_every_entity() {
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		ImportPlan plan = configMatcher
			.plan(TENANT_ID, pkg, ImportMode.OVERWRITE).await().indefinitely();

		assertEquals(0, plan.count(PlannedAction.Action.CREATE));
		for (PlannedAction action : plan.getActions()) {
			assertSame(PlannedAction.Action.OVERWRITE, action.action(), action.ref());
			assertNotNull(action.existingId(), action.ref());
		}
	}

	@Test
	void an_entity_absent_from_the_tenant_is_a_create() {
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		ConfigEntity newLanguage = new ConfigEntity(
			"LANGUAGE-NEW",
			ConfigEntityType.LANGUAGE,
			LANGUAGE_NAME,
			LanguageDTO.builder().name(LANGUAGE_NAME).build(),
			new LinkedHashMap<>(),
			null);

		ImportPlan plan = plan(pkg, List.of(newLanguage), ImportMode.SKIP);

		PlannedAction action = plan.byRef().get("LANGUAGE-NEW");
		assertNotNull(action);
		assertSame(PlannedAction.Action.CREATE, action.action());
		assertNull(action.existingId());
	}

	@Test
	void a_composite_entity_whose_ancestor_is_created_is_itself_created() {
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		// A brand new doc type (CREATE) and a field pointing at it: the field
		// cannot match an existing row because its identity ancestor does not yet
		// exist, so it is a CREATE too.
		ConfigEntity newDocType = new ConfigEntity(
			"DOC_TYPE-NEW",
			ConfigEntityType.DOC_TYPE,
			DOC_TYPE_NAME,
			DocTypeDTO.builder().name(DOC_TYPE_NAME).build(),
			new LinkedHashMap<>(),
			null);

		Map<String, List<String>> fieldRefs = new LinkedHashMap<>();
		fieldRefs.put("docType", List.of("DOC_TYPE-NEW"));

		ConfigEntity newField = new ConfigEntity(
			"DOC_TYPE_FIELD-NEW",
			ConfigEntityType.DOC_TYPE_FIELD,
			DOC_TYPE_FIELD_NAME,
			DocTypeFieldDTO.builder()
				.name(DOC_TYPE_FIELD_NAME)
				.fieldName(ENTITY_NAME_PREFIX + "field-name")
				.build(),
			fieldRefs,
			null);

		ImportPlan plan = plan(pkg, List.of(newDocType, newField), ImportMode.SKIP);

		assertSame(
			PlannedAction.Action.CREATE, plan.byRef().get("DOC_TYPE-NEW").action());

		PlannedAction fieldAction = plan.byRef().get("DOC_TYPE_FIELD-NEW");
		assertSame(PlannedAction.Action.CREATE, fieldAction.action());
		assertNull(fieldAction.existingId());
	}

	/**
	 * Plans a package augmented with extra synthetic entities appended to the
	 * exported baseline.
	 */
	private ImportPlan plan(
		ConfigPackage baseline, List<ConfigEntity> extra, ImportMode mode) {

		List<ConfigEntity> entities = new ArrayList<>(baseline.getEntities());
		entities.addAll(extra);

		ConfigPackage augmented = new ConfigPackage(
			baseline.getSchemaVersion(), baseline.getMetadata(), entities);

		return configMatcher.plan(TENANT_ID, augmented, mode).await().indefinitely();
	}

	private static void assertNotSame(ConfigEntityType unexpected, ConfigEntityType actual) {
		assertFalse(unexpected == actual, "unexpected join entity in plan: " + actual);
	}

}
