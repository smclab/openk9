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

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.ImportMode;
import io.openk9.datasource.config.model.ImportResult;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.base.CharFilterDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;
import io.openk9.datasource.service.CharFilterService;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.RAGConfigurationService;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for the transactional importer. The boot runs
 * {@code createDefault("public")}, so the tenant is populated; we export it with
 * the (green) exporter to obtain a realistic package and apply it back against the
 * same tenant, then extend the package with a new subgraph to exercise creation
 * and association rewiring.
 */
@QuarkusTest
public class ConfigImporterTest {

	private static final String TENANT_ID = "public";

	private static final String ENTITY_NAME_PREFIX = "ConfigImporterTest - ";
	private static final String DOC_TYPE_NAME = ENTITY_NAME_PREFIX + "doc-type";
	private static final String DOC_TYPE_FIELD_NAME = ENTITY_NAME_PREFIX + "field";
	private static final String SECRET_CHAR_FILTER_NAME =
		ENTITY_NAME_PREFIX + "secret-char-filter";
	private static final String CREATED_CHAR_FILTER_NAME =
		ENTITY_NAME_PREFIX + "created-char-filter";
	private static final String EXISTING_REF_FIELD_NAME =
		ENTITY_NAME_PREFIX + "existing-ref-field";
	private static final String RAG_CONFIGURATION_NAME =
		ENTITY_NAME_PREFIX + "rag-config";

	@Inject
	ConfigExporter configExporter;

	@Inject
	ConfigImporter configImporter;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	DocTypeService docTypeService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	CharFilterService charFilterService;

	@Inject
	RAGConfigurationService ragConfigurationService;

	private static final String COUNT_JOIN_ROWS =
		"select count(i) from EnrichPipelineItem i";

	@Test
	void round_trip_in_skip_mode_creates_nothing() {
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();
		long itemsBefore = count(COUNT_JOIN_ROWS);

		ImportResult result =
			configImporter.apply(TENANT_ID, pkg, ImportMode.SKIP)
				.await().indefinitely();

		assertEquals(0, result.created(),
			"round-trip against the same tenant must create nothing");
		assertTrue(result.skipped() > 0, "every existing entity must be skipped");
		assertEquals(
			pkg.getEntities().size(),
			result.created() + result.overwritten() + result.skipped(),
			"every package entity, join entities included, must be counted");
		assertEquals(itemsBefore, count(COUNT_JOIN_ROWS),
			"join rows must not be duplicated");
	}

	@Test
	void round_trip_in_overwrite_mode_is_idempotent() {
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();
		long itemsBefore = count(COUNT_JOIN_ROWS);

		ImportResult result =
			configImporter.apply(TENANT_ID, pkg, ImportMode.OVERWRITE)
				.await().indefinitely();

		assertEquals(0, result.created());
		assertTrue(result.overwritten() > 0);
		assertEquals(itemsBefore, count(COUNT_JOIN_ROWS),
			"overwriting must not duplicate join rows");
	}

	@Test
	void a_new_subgraph_is_created_and_its_association_is_wired() {
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

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
				.fieldName("zz-field-name")
				.fieldType(FieldType.TEXT)
				.build(),
			fieldRefs,
			null);

		List<ConfigEntity> entities = new ArrayList<>(pkg.getEntities());
		entities.add(newDocType);
		entities.add(newField);
		ConfigPackage augmented = new ConfigPackage(
			pkg.getSchemaVersion(), pkg.getMetadata(), entities);

		ImportResult result =
			configImporter.apply(TENANT_ID, augmented, ImportMode.SKIP)
				.await().indefinitely();

		Long docTypeId = result.resolvedIds().get("DOC_TYPE-NEW");
		Long fieldId = result.resolvedIds().get("DOC_TYPE_FIELD-NEW");
		assertNotNull(docTypeId, "the new doc type must have been created");
		assertNotNull(fieldId, "the new doc type field must have been created");

		// Reload the field and assert its docType FK points at the new doc type:
		// this proves create + to-one association rewiring + topological order.
		Long wiredDocTypeId = sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.find(DocTypeField.class, fieldId)
				.chain(field -> s.fetch(field.getDocType()))
				.map(DocType::getId)
		).await().indefinitely();

		assertEquals(docTypeId, wiredDocTypeId,
			"the created field must be wired to the created doc type");

		// Created in this method (not in setup): remove it here, the field
		// first so the doc-type FK is released before the doc-type is deleted.
		EntitiesUtils.removeEntity(fieldId, docTypeFieldService, sessionFactory);
		EntitiesUtils.removeEntity(docTypeId, docTypeService, sessionFactory);
	}

	@Test
	void a_new_field_referencing_an_unloaded_doc_type_is_wired() {
		// A new field pointing at an existing (SKIP, hence not loaded) doc type:
		// its docType is only known by id. The DocTypeField @PostPersist callback
		// reads docType.getName() to build the path, so the importer must load the
		// referenced doc type, not set a bare proxy - otherwise the reactive session
		// throws LazyInitializationException at flush. This exercises the partial /
		// cross-tenant case the full-package round-trips do not, because there every
		// referenced entity is itself processed and thus already loaded.
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		String existingDocTypeRef = pkg.getEntities().stream()
			.filter(entity -> entity.getType() == ConfigEntityType.DOC_TYPE)
			.map(ConfigEntity::getRef)
			.findFirst()
			.orElseThrow(() ->
				new IllegalStateException("the default tenant must have doc types"));

		Map<String, List<String>> fieldRefs = new LinkedHashMap<>();
		fieldRefs.put("docType", List.of(existingDocTypeRef));

		ConfigEntity newField = new ConfigEntity(
			"DOC_TYPE_FIELD-EXISTING-REF",
			ConfigEntityType.DOC_TYPE_FIELD,
			EXISTING_REF_FIELD_NAME,
			DocTypeFieldDTO.builder()
				.name(EXISTING_REF_FIELD_NAME)
				.fieldName(EXISTING_REF_FIELD_NAME + "-fn")
				.fieldType(FieldType.TEXT)
				.build(),
			fieldRefs,
			null);

		List<ConfigEntity> entities = new ArrayList<>(pkg.getEntities());
		entities.add(newField);
		ConfigPackage augmented = new ConfigPackage(
			pkg.getSchemaVersion(), pkg.getMetadata(), entities);

		ImportResult result =
			configImporter.apply(TENANT_ID, augmented, ImportMode.SKIP)
				.await().indefinitely();

		Long existingDocTypeId = result.resolvedIds().get(existingDocTypeRef);
		Long fieldId = result.resolvedIds().get("DOC_TYPE_FIELD-EXISTING-REF");
		assertNotNull(fieldId, "the new field must have been created");

		Long wiredDocTypeId = sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.find(DocTypeField.class, fieldId)
				.chain(field -> s.fetch(field.getDocType()))
				.map(DocType::getId)
		).await().indefinitely();

		assertEquals(existingDocTypeId, wiredDocTypeId,
			"the created field must be wired to the existing doc type");

		// Created in this method: remove only the field; the doc type pre-existed.
		EntitiesUtils.removeEntity(fieldId, docTypeFieldService, sessionFactory);
	}

	@Test
	void a_new_rag_configuration_is_created_with_its_type() {
		// RAGConfiguration declares CreateRAGConfigurationDTO as its attributes, a
		// subtype carrying the mandatory type. Creating one on import must persist
		// that type: mapping through the entity's base K9EntityMapper (typed on the
		// base DTO) would drop it and fail the whole import on the NOT NULL column.
		// The round-trips above never hit this: an existing RAGConfiguration is
		// SKIP'd (no create) and overwrite leaves the immutable type untouched.
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		ConfigEntity newRag = new ConfigEntity(
			"RAG_CONFIGURATION-NEW",
			ConfigEntityType.RAG_CONFIGURATION,
			RAG_CONFIGURATION_NAME,
			CreateRAGConfigurationDTO.builder()
				.name(RAG_CONFIGURATION_NAME)
				.type(RAGType.CHAT_RAG)
				.build(),
			new LinkedHashMap<>(),
			null);

		List<ConfigEntity> entities = new ArrayList<>(pkg.getEntities());
		entities.add(newRag);
		ConfigPackage augmented = new ConfigPackage(
			pkg.getSchemaVersion(), pkg.getMetadata(), entities);

		ImportResult result =
			configImporter.apply(TENANT_ID, augmented, ImportMode.SKIP)
				.await().indefinitely();

		Long ragId = result.resolvedIds().get("RAG_CONFIGURATION-NEW");
		assertNotNull(ragId, "the new RAG configuration must have been created");

		RAGType storedType = sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.find(RAGConfiguration.class, ragId).map(RAGConfiguration::getType)
		).await().indefinitely();

		assertEquals(RAGType.CHAT_RAG, storedType,
			"the mandatory type from the declared DTO must be persisted on create");

		// Delete the RAG configuration created by this method.
		EntitiesUtils.removeEntity(ragId, ragConfigurationService, sessionFactory);
	}

	@Test
	void overwrite_restores_a_redacted_secret_from_the_target() {
		// Seed an entity carrying a secret inside its jsonConfig. The exporter
		// redacts it to the placeholder, so re-importing in overwrite mode must
		// restore the stored value rather than persist the placeholder (the case
		// a plain strip-and-patch would get wrong, since jsonConfig is one column).
		String secret = "s3cr3t-value";
		Long id = seedCharFilter(
			SECRET_CHAR_FILTER_NAME,
			"{\"password\":\"" + secret + "\",\"mapping\":\"a=>b\"}");

		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();
		configImporter.apply(TENANT_ID, pkg, ImportMode.OVERWRITE)
			.await().indefinitely();

		String stored = loadCharFilterJsonConfig(id);
		assertTrue(stored.contains(secret),
			"the real secret must survive the overwrite");
		assertFalse(stored.contains(ConfigRedactor.PLACEHOLDER),
			"the redaction placeholder must never be persisted");

		// Delete the char filter created by this method.
		EntitiesUtils.removeEntity(id, charFilterService, sessionFactory);
	}

	@Test
	void create_never_persists_the_redaction_placeholder() {
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		// A brand new entity whose secret arrives already redacted: there is no
		// target to restore from, so the redacted path is dropped, never stored.
		ConfigEntity redacted = new ConfigEntity(
			"CHAR_FILTER-NEW",
			ConfigEntityType.CHAR_FILTER,
			CREATED_CHAR_FILTER_NAME,
			CharFilterDTO.builder()
				.name(CREATED_CHAR_FILTER_NAME)
				.type("html_strip")
				.jsonConfig("{\"password\":\"" + ConfigRedactor.PLACEHOLDER + "\"}")
				.build(),
			new LinkedHashMap<>(),
			List.of("jsonConfig.password"));

		List<ConfigEntity> entities = new ArrayList<>(pkg.getEntities());
		entities.add(redacted);
		ConfigPackage augmented = new ConfigPackage(
			pkg.getSchemaVersion(), pkg.getMetadata(), entities);

		ImportResult result =
			configImporter.apply(TENANT_ID, augmented, ImportMode.SKIP)
				.await().indefinitely();

		Long id = result.resolvedIds().get("CHAR_FILTER-NEW");
		assertNotNull(id, "the new char filter must have been created");
		assertFalse(loadCharFilterJsonConfig(id).contains(ConfigRedactor.PLACEHOLDER),
			"create must not persist the placeholder");

		// Delete the char filter created by this method.
		EntitiesUtils.removeEntity(id, charFilterService, sessionFactory);
	}

	private Long seedCharFilter(String name, String jsonConfig) {
		return sessionFactory.withTransaction(TENANT_ID, (s, t) -> {
			CharFilter charFilter = new CharFilter();
			charFilter.setName(name);
			charFilter.setJsonConfig(jsonConfig);
			return s.persist(charFilter).call(s::flush).replaceWith(charFilter);
		}).await().indefinitely().getId();
	}

	private String loadCharFilterJsonConfig(Long id) {
		return sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.find(CharFilter.class, id).map(CharFilter::getJsonConfig)
		).await().indefinitely();
	}

	private long count(String query) {
		return sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.createQuery(query, Long.class).getSingleResult()
		).await().indefinitely();
	}

}
