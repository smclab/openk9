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
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.ImportMode;
import io.openk9.datasource.config.model.ImportReport;
import io.openk9.datasource.config.model.PlannedAction;
import io.openk9.datasource.config.model.representation.AclMappingRepresentation;
import io.openk9.datasource.config.model.representation.EnrichPipelineItemRepresentation;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.EnrichPipelineItemKey;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.PluginDriverDocTypeFieldKey;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.ResultAclConfig;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.dto.base.CharFilterDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.base.EnrichPipelineDTO;
import io.openk9.datasource.model.dto.base.ResultAclConfigDTO;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;
import io.openk9.datasource.service.CharFilterService;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.EnrichPipelineService;
import io.openk9.datasource.service.RAGConfigurationService;
import io.openk9.datasource.service.ResultAclConfigService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
	private static final String ROLLBACK_CHAR_FILTER_NAME =
		ENTITY_NAME_PREFIX + "rollback-char-filter";
	private static final String CONFLICT_CHAR_FILTER_NAME =
		ENTITY_NAME_PREFIX + "conflict-char-filter";
	private static final String DRYRUN_CHAR_FILTER_NAME =
		ENTITY_NAME_PREFIX + "dryrun-char-filter";
	private static final String DANGLING_FIELD_NAME =
		ENTITY_NAME_PREFIX + "dangling-field";
	private static final String ACL_DOC_TYPE_NAME =
		ENTITY_NAME_PREFIX + "result-acl-doc-type";
	private static final String ACL_TITLE_NAME =
		ENTITY_NAME_PREFIX + "result-acl-title";
	private static final String ACL_DATE_NAME =
		ENTITY_NAME_PREFIX + "result-acl-date";
	private static final String SKIP_JOINS_PIPELINE_NAME =
		ENTITY_NAME_PREFIX + "skip-joins-pipeline";
	private static final String OVERWRITE_JOINS_PIPELINE_NAME =
		ENTITY_NAME_PREFIX + "overwrite-joins-pipeline";
	private static final String CREATED_PIPELINE_NAME =
		ENTITY_NAME_PREFIX + "created-pipeline";
	private static final String OLD_EMBEDDING_MODEL_NAME =
		ENTITY_NAME_PREFIX + "old-embedding-model";

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

	@Inject
	ResultAclConfigService resultAclConfigService;

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	ObjectMapper objectMapper;

	private static final String COUNT_JOIN_ROWS =
		"select count(i) from EnrichPipelineItem i";
	private static final String COUNT_ACL_MAPPING_ROWS =
		"select count(a) from AclMapping a";

	@Test
	void round_trip_in_skip_mode_creates_nothing() {
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();
		long itemsBefore = count(COUNT_JOIN_ROWS);

		ImportReport result =
			configImporter.importConfig(TENANT_ID, pkg, ImportMode.SKIP, false)
				.await().indefinitely();

		assertEquals(0, result.created(),
			"round-trip against the same tenant must create nothing");
		assertEquals(0, result.overwritten(),
			"SKIP must overwrite nothing, join entities included");
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

		ImportReport result =
			configImporter.importConfig(TENANT_ID, pkg, ImportMode.OVERWRITE, false)
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

		ImportReport result =
			configImporter.importConfig(TENANT_ID, augmented, ImportMode.SKIP, false)
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
		// its docType is only known by id, so the importer must load it rather
		// than set a bare proxy - a reactive session cannot lazily fetch one,
		// and DocTypeField.getPath reads docType.getName() at the first read.
		// Asserts the wiring; it does not itself read the path. Exercises the
		// partial / cross-tenant case the full-package round-trips do not,
		// because there every referenced entity is itself processed and thus
		// already loaded.

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

		ImportReport result =
			configImporter.importConfig(TENANT_ID, augmented, ImportMode.SKIP, false)
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

		ImportReport result =
			configImporter.importConfig(TENANT_ID, augmented, ImportMode.SKIP, false)
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
		configImporter.importConfig(TENANT_ID, pkg, ImportMode.OVERWRITE, false)
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

		ImportReport result =
			configImporter.importConfig(TENANT_ID, augmented, ImportMode.SKIP, false)
				.await().indefinitely();

		Long id = result.resolvedIds().get("CHAR_FILTER-NEW");
		assertNotNull(id, "the new char filter must have been created");
		assertFalse(loadCharFilterJsonConfig(id).contains(ConfigRedactor.PLACEHOLDER),
			"create must not persist the placeholder");

		// Delete the char filter created by this method.
		EntitiesUtils.removeEntity(id, charFilterService, sessionFactory);
	}

	@Test
	void an_error_during_apply_rolls_back_the_whole_import() {
		// Two new char filters share a name; the name column is unique, so the second
		// CREATE violates the constraint at flush. The whole apply runs in one
		// transaction, so the first (already flushed) insert must be rolled back:
		// afterwards no row carries the shared name.
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		List<ConfigEntity> entities = new ArrayList<>(pkg.getEntities());
		entities.add(charFilterNode("CHAR_FILTER-ROLLBACK-A", ROLLBACK_CHAR_FILTER_NAME));
		entities.add(charFilterNode("CHAR_FILTER-ROLLBACK-B", ROLLBACK_CHAR_FILTER_NAME));
		ConfigPackage augmented = new ConfigPackage(
			pkg.getSchemaVersion(), pkg.getMetadata(), entities);

		assertThrows(Exception.class, () ->
			configImporter.importConfig(TENANT_ID, augmented, ImportMode.SKIP, false)
				.await().indefinitely());

		assertEquals(
			0L, countCharFilterByName(ROLLBACK_CHAR_FILTER_NAME),
			"a failed apply must roll back the flushed insert: no row keeps the name");
	}

	@Test
	void the_tenant_binding_is_rebound_and_the_virtual_host_preserved() {
		// After an import the destination TenantBinding must point at the imported
		// bucket and models (resolved from the package metadata) and keep its own
		// virtualHost (routing identity, never transferred).
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();
		String virtualHostBefore = tenantBindingVirtualHost();

		ImportReport result =
			configImporter.importConfig(TENANT_ID, pkg, ImportMode.OVERWRITE, false)
				.await().indefinitely();

		Object[] binding = sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.createQuery(
				"select tb.bucket.id, tb.embeddingModel.id, "
					+ "tb.largeLanguageModel.id, tb.virtualHost "
					+ "from TenantBinding tb where tb.id = 1", Object[].class)
				.getSingleResult()
		).await().indefinitely();

		var metadata = pkg.getMetadata();
		assertEquals(
			result.resolvedIds().get(metadata.getActiveBucketRef()), binding[0],
			"the tenant binding must reference the imported active bucket");
		if (metadata.getEnabledEmbeddingModelRef() != null) {
			assertEquals(
				result.resolvedIds().get(metadata.getEnabledEmbeddingModelRef()),
				binding[1],
				"the tenant binding must reference the imported embedding model");
		}
		if (metadata.getEnabledLargeLanguageModelRef() != null) {
			assertEquals(
				result.resolvedIds().get(metadata.getEnabledLargeLanguageModelRef()),
				binding[2],
				"the tenant binding must reference the imported LLM");
		}
		assertEquals(virtualHostBefore, binding[3],
			"the import must preserve the destination virtual host");
	}

	@Test
	void skip_keeps_the_existing_value_while_overwrite_replaces_it() {
		// Conflict semantics: an incoming entity that matches an existing one by name
		// but carries a different value. SKIP must leave the target untouched;
		// OVERWRITE must apply the incoming value.
		Long id = seedCharFilter(CONFLICT_CHAR_FILTER_NAME, "{\"mapping\":\"a=>b\"}");

		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();
		ConfigEntity original = pkg.getEntities().stream()
			.filter(entity -> entity.getType() == ConfigEntityType.CHAR_FILTER)
			.filter(entity -> CONFLICT_CHAR_FILTER_NAME.equals(entity.getKey()))
			.findFirst()
			.orElseThrow();
		ConfigEntity changed = new ConfigEntity(
			original.getRef(), original.getType(), original.getKey(),
			CharFilterDTO.builder()
				.name(CONFLICT_CHAR_FILTER_NAME)
				.jsonConfig("{\"mapping\":\"x=>y\"}")
				.build(),
			original.getReferences(), original.getRedactedFields());

		List<ConfigEntity> entities = new ArrayList<>(pkg.getEntities());
		entities.replaceAll(entity -> entity == original ? changed : entity);
		ConfigPackage augmented = new ConfigPackage(
			pkg.getSchemaVersion(), pkg.getMetadata(), entities);

		configImporter.importConfig(TENANT_ID, augmented, ImportMode.SKIP, false)
			.await().indefinitely();
		assertTrue(loadCharFilterJsonConfig(id).contains("a=>b"),
			"SKIP must keep the existing value on a name match");

		configImporter.importConfig(TENANT_ID, augmented, ImportMode.OVERWRITE, false)
			.await().indefinitely();
		assertTrue(loadCharFilterJsonConfig(id).contains("x=>y"),
			"OVERWRITE must apply the incoming value on a name match");

		EntitiesUtils.removeEntity(id, charFilterService, sessionFactory);
	}

	@Test
	void the_result_acl_config_round_trips_with_its_roles_and_visible_fields() {
		// 1. Seed a doc type with two fields and the configuration
		var docType = docTypeService.create(DocTypeDTO.builder()
				.name(ACL_DOC_TYPE_NAME)
				.build())
			.await().indefinitely();
		var title = addAclField(docType, ACL_TITLE_NAME, "resultAclImportTitle");
		var date = addAclField(docType, ACL_DATE_NAME, "resultAclImportDate");

		var seeded = resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(true)
				.superUserRoles(List.of("auditor", "legal"))
				.visibleFieldIds(Set.of(title.getId(), date.getId()))
				.build())
			.await().indefinitely();

		// 2. The export carries the values and the visible fields as references
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();
		ConfigEntity exported = pkg.getEntities().stream()
			.filter(entity -> entity.getType() == ConfigEntityType.RESULT_ACL_CONFIG)
			.findFirst()
			.orElseThrow();
		var exportedAttributes = (ResultAclConfigDTO) exported.getAttributes();

		assertEquals(ResultAclConfig.DEFAULT_NAME, exported.getKey());
		assertTrue(exportedAttributes.getEnabled());
		assertEquals(List.of("auditor", "legal"), exportedAttributes.getSuperUserRoles());
		assertEquals(2, exported.getReferences().get("visibleFields").size());

		// 3. Without a configuration in the tenant the import creates it
		EntitiesUtils.removeEntity(seeded.getId(), resultAclConfigService, sessionFactory);
		assertNull(resultAclConfigService.findCurrent().await().indefinitely());

		configImporter.importConfig(TENANT_ID, pkg, ImportMode.SKIP, false)
			.await().indefinitely();

		assertResultAclConfig(
			true, List.of("auditor", "legal"), Set.of(title.getId(), date.getId()));

		// 4. With a changed configuration the overwrite restores the exported one
		var created = resultAclConfigService.findCurrent().await().indefinitely();
		resultAclConfigService.update(created.getId(), ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.superUserRoles(List.of("other"))
				.visibleFieldIds(Set.of(title.getId()))
				.build())
			.await().indefinitely();

		configImporter.importConfig(TENANT_ID, pkg, ImportMode.OVERWRITE, false)
			.await().indefinitely();

		assertResultAclConfig(
			true, List.of("auditor", "legal"), Set.of(title.getId(), date.getId()));
		assertEquals(1, countByName("ResultAclConfig", ResultAclConfig.DEFAULT_NAME),
			"the import must overwrite the configuration, not duplicate it");

		// Created in this method: remove the configuration before the doc type,
		// which deletes its fields.
		EntitiesUtils.removeEntity(
			resultAclConfigService.findCurrent().await().indefinitely(),
			resultAclConfigService,
			sessionFactory);
		docTypeService.deleteById(docType.getId()).await().indefinitely();
	}

	private DocTypeField addAclField(DocType docType, String name, String fieldName) {
		return docTypeService.addDocTypeField(
				docType.getId(),
				DocTypeFieldDTO.builder()
					.name(name)
					.fieldName(fieldName)
					.fieldType(FieldType.TEXT)
					.build())
			.await().indefinitely()
			.right;
	}

	private void assertResultAclConfig(
		boolean enabled, List<String> roles, Set<Long> visibleFieldIds) {

		var current = resultAclConfigService.findCurrent().await().indefinitely();
		var currentVisibleFieldIds = resultAclConfigService
			.getVisibleFields(current.getId())
			.await().indefinitely()
			.stream()
			.map(DocTypeField::getId)
			.collect(Collectors.toSet());

		assertEquals(enabled, current.isEnabled());
		assertEquals(roles, ResultAclConfig.toRoleList(current.getSuperUserRoles()));
		assertEquals(visibleFieldIds, currentVisibleFieldIds);
	}

	private static ConfigEntity charFilterNode(String ref, String name) {
		return new ConfigEntity(
			ref, ConfigEntityType.CHAR_FILTER, name,
			CharFilterDTO.builder().name(name).type("html_strip").build(),
			new LinkedHashMap<>(), null);
	}

	private long countCharFilterByName(String name) {
		return sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.createQuery(
				"select count(c) from CharFilter c where c.name = :n", Long.class)
				.setParameter("n", name)
				.getSingleResult()
		).await().indefinitely();
	}

	private String tenantBindingVirtualHost() {
		return sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.createQuery(
				"select tb.virtualHost from TenantBinding tb where tb.id = 1",
				String.class).getSingleResult()
		).await().indefinitely();
	}

	@Test
	void dry_run_previews_a_creation_then_apply_writes_it() {
		// A brand new char filter carrying a redacted secret, appended to a full
		// round-trip package. The dry-run must plan its creation and flag the
		// secret to re-enter, yet write nothing; the apply must then persist it.
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		ConfigEntity newCharFilter = new ConfigEntity(
			"CHAR_FILTER-DRYRUN",
			ConfigEntityType.CHAR_FILTER,
			DRYRUN_CHAR_FILTER_NAME,
			CharFilterDTO.builder()
				.name(DRYRUN_CHAR_FILTER_NAME)
				.type("html_strip")
				.jsonConfig("{\"password\":\"" + ConfigRedactor.PLACEHOLDER + "\"}")
				.build(),
			new LinkedHashMap<>(),
			List.of("jsonConfig.password"));

		List<ConfigEntity> entities = new ArrayList<>(pkg.getEntities());
		entities.add(newCharFilter);
		ConfigPackage augmented = new ConfigPackage(
			pkg.getSchemaVersion(), pkg.getMetadata(), entities);

		// 1. Dry-run: previews the creation, flags the secret, writes nothing
		ImportReport preview =
			configImporter.importConfig(TENANT_ID, augmented, ImportMode.SKIP, true)
				.await().indefinitely();

		assertTrue(preview.dryRun());
		assertFalse(preview.applied());
		assertEquals(1, preview.created(),
			"only the appended char filter is new; the round-trip is skipped");
		assertTrue(preview.resolvedIds().isEmpty(), "dry-run resolves no ids");
		assertTrue(
			preview.secretsToReenter().stream().anyMatch(secret ->
				"CHAR_FILTER-DRYRUN".equals(secret.ref())
					&& secret.fields().contains("jsonConfig.password")),
			"the created entity's redacted secret must be flagged to re-enter");
		assertEquals(0, countByName("CharFilter", DRYRUN_CHAR_FILTER_NAME),
			"dry-run must not create the char filter");

		// 2. Apply: persists the creation and resolves its id
		ImportReport applied =
			configImporter.importConfig(TENANT_ID, augmented, ImportMode.SKIP, false)
				.await().indefinitely();

		assertFalse(applied.dryRun());
		assertTrue(applied.applied());
		assertEquals(1, applied.created());
		Long id = applied.resolvedIds().get("CHAR_FILTER-DRYRUN");
		assertNotNull(id, "apply must create the char filter and resolve its id");
		assertEquals(1, countByName("CharFilter", DRYRUN_CHAR_FILTER_NAME));

		// 3. Clean up the char filter created by this method
		EntitiesUtils.removeEntity(id, charFilterService, sessionFactory);
	}

	@Test
	void dry_run_reports_a_missing_reference() {
		// A field pointing at a doc type absent from the package: the dangling edge
		// a shallow export leaves behind. The report must surface it, and the
		// dry-run must write nothing.
		Map<String, List<String>> fieldRefs = new LinkedHashMap<>();
		fieldRefs.put("docType", List.of("DOC_TYPE-MISSING"));

		ConfigEntity danglingField = new ConfigEntity(
			"DOC_TYPE_FIELD-DANGLING",
			ConfigEntityType.DOC_TYPE_FIELD,
			DANGLING_FIELD_NAME,
			DocTypeFieldDTO.builder()
				.name(DANGLING_FIELD_NAME)
				.fieldName(DANGLING_FIELD_NAME + "-fn")
				.fieldType(FieldType.TEXT)
				.build(),
			fieldRefs,
			null);

		ConfigPackage pkg = new ConfigPackage(
			ConfigPackage.CURRENT_SCHEMA_VERSION, null, List.of(danglingField));

		ImportReport report =
			configImporter.importConfig(TENANT_ID, pkg, ImportMode.SKIP, true)
				.await().indefinitely();

		// 1. The dangling edge is reported
		assertTrue(
			report.missingReferences().stream().anyMatch(missing ->
				"DOC_TYPE_FIELD-DANGLING".equals(missing.fromRef())
					&& "docType".equals(missing.relationship())
					&& "DOC_TYPE-MISSING".equals(missing.targetHandle())),
			"the reference to the absent doc type must be reported as missing");

		// 2. The dry-run wrote nothing
		assertFalse(report.applied());
		assertEquals(0, countByName("DocTypeField", DANGLING_FIELD_NAME));
	}

	@Test
	void skip_leaves_the_joins_of_an_existing_parent_untouched() {
		// A join follows the action of its parent (the pipeline, the plugin
		// driver). Both parents exist, so SKIP must neither rewrite an existing
		// join nor add a missing one, in the preview as in the apply.

		// 1. Seed both joins, then export them carrying different values
		SeededJoins joins =
			seedJoins(SKIP_JOINS_PIPELINE_NAME, 1.0f, UserField.USERNAME);
		ConfigPackage pkg = withJoinValues(
			configExporter.export(TENANT_ID).await().indefinitely(),
			joins, 5.0f, UserField.ROLES);

		// 2. Joins present: SKIP keeps their current values
		ImportReport present = previewThenApply(pkg, ImportMode.SKIP);

		assertEquals(0, present.created());
		assertEquals(0, present.overwritten(), "SKIP must not rewrite a join");
		assertEquals(
			PlannedAction.Action.SKIP,
			actionOf(present, joins.enrichPipelineItemRef()));
		assertEquals(
			PlannedAction.Action.SKIP, actionOf(present, joins.aclMappingRef()));
		assertEquals(1.0f, loadWeight(joins.pipelineId(), joins.enrichItemId()));
		assertEquals(
			UserField.USERNAME,
			loadUserField(joins.pluginDriverId(), joins.docTypeFieldId()));

		// 3. Joins absent: SKIP does not add them to the existing parents
		removeJoinRows(joins);
		ImportReport absent = previewThenApply(pkg, ImportMode.SKIP);

		assertEquals(0, absent.created(),
			"SKIP must not add a join to an existing parent");
		assertNull(loadWeight(joins.pipelineId(), joins.enrichItemId()));
		assertNull(loadUserField(joins.pluginDriverId(), joins.docTypeFieldId()));

		// 4. Clean up the pipeline created by this method
		EntitiesUtils.removeEntity(
			joins.pipelineId(), enrichPipelineService, sessionFactory);
	}

	@Test
	void overwrite_rewrites_existing_joins_and_creates_missing_ones() {
		// 1. Seed both joins, then export them carrying different values
		SeededJoins joins =
			seedJoins(OVERWRITE_JOINS_PIPELINE_NAME, 1.0f, UserField.USERNAME);
		ConfigPackage pkg = withJoinValues(
			configExporter.export(TENANT_ID).await().indefinitely(),
			joins, 5.0f, UserField.ROLES);

		// 2. Joins present: OVERWRITE applies the package values
		ImportReport present = previewThenApply(pkg, ImportMode.OVERWRITE);

		assertEquals(
			PlannedAction.Action.OVERWRITE,
			actionOf(present, joins.enrichPipelineItemRef()));
		assertEquals(
			PlannedAction.Action.OVERWRITE,
			actionOf(present, joins.aclMappingRef()));
		assertEquals(5.0f, loadWeight(joins.pipelineId(), joins.enrichItemId()));
		assertEquals(
			UserField.ROLES,
			loadUserField(joins.pluginDriverId(), joins.docTypeFieldId()));

		// 3. Joins absent: OVERWRITE creates them with the package values
		removeJoinRows(joins);
		ImportReport absent = previewThenApply(pkg, ImportMode.OVERWRITE);

		assertEquals(2, absent.created(), "only the two joins are missing");
		assertEquals(
			PlannedAction.Action.CREATE,
			actionOf(absent, joins.enrichPipelineItemRef()));
		assertEquals(
			PlannedAction.Action.CREATE, actionOf(absent, joins.aclMappingRef()));
		assertEquals(5.0f, loadWeight(joins.pipelineId(), joins.enrichItemId()));
		assertEquals(
			UserField.ROLES,
			loadUserField(joins.pluginDriverId(), joins.docTypeFieldId()));

		// 4. Clean up the joins, then the pipeline created by this method
		removeJoinRows(joins);
		EntitiesUtils.removeEntity(
			joins.pipelineId(), enrichPipelineService, sessionFactory);
	}

	@Test
	void a_join_with_an_endpoint_outside_the_package_is_skipped() {
		// A join whose endpoints are not in the package cannot be applied: it is
		// planned as SKIP and its dangling edges are reported.
		Map<String, List<String>> refs = new LinkedHashMap<>();
		refs.put("pluginDriver", List.of("PLUGIN_DRIVER-MISSING"));
		refs.put("docTypeField", List.of("DOC_TYPE_FIELD-MISSING"));

		ConfigEntity danglingMapping = new ConfigEntity(
			"ACL_MAPPING-MISSING",
			ConfigEntityType.ACL_MAPPING,
			null,
			new AclMappingRepresentation(UserField.ROLES),
			refs,
			null);

		ConfigPackage pkg = new ConfigPackage(
			ConfigPackage.CURRENT_SCHEMA_VERSION, null, List.of(danglingMapping));
		long mappingsBefore = count(COUNT_ACL_MAPPING_ROWS);

		// 1. Preview and apply both skip the join
		ImportReport report = previewThenApply(pkg, ImportMode.OVERWRITE);

		assertEquals(1, report.skipped());
		assertEquals(
			PlannedAction.Action.SKIP, actionOf(report, "ACL_MAPPING-MISSING"));

		// 2. Both dangling edges are reported, and nothing is written
		assertEquals(
			2,
			report.missingReferences().stream()
				.filter(missing -> "ACL_MAPPING-MISSING".equals(missing.fromRef()))
				.count());
		assertEquals(mappingsBefore, count(COUNT_ACL_MAPPING_ROWS));
	}

	@Test
	void the_join_of_a_created_parent_is_created() {
		// A new pipeline joined to an existing enrich item: the parent is created,
		// so its join is created too, even in SKIP, after both endpoints exist.
		ConfigPackage pkg = configExporter.export(TENANT_ID).await().indefinitely();

		String enrichItemRef = pkg.getEntities().stream()
			.filter(entity -> entity.getType() == ConfigEntityType.ENRICH_ITEM)
			.map(ConfigEntity::getRef)
			.findFirst()
			.orElseThrow(() ->
				new IllegalStateException("the test tenant must have enrich items"));

		ConfigEntity newPipeline = new ConfigEntity(
			"ENRICH_PIPELINE-NEW",
			ConfigEntityType.ENRICH_PIPELINE,
			CREATED_PIPELINE_NAME,
			EnrichPipelineDTO.builder().name(CREATED_PIPELINE_NAME).build(),
			new LinkedHashMap<>(),
			null);

		Map<String, List<String>> itemRefs = new LinkedHashMap<>();
		itemRefs.put("enrichPipeline", List.of("ENRICH_PIPELINE-NEW"));
		itemRefs.put("enrichItem", List.of(enrichItemRef));

		ConfigEntity newItem = new ConfigEntity(
			"ENRICH_PIPELINE_ITEM-NEW",
			ConfigEntityType.ENRICH_PIPELINE_ITEM,
			null,
			new EnrichPipelineItemRepresentation(2.0f),
			itemRefs,
			null);

		List<ConfigEntity> entities = new ArrayList<>(pkg.getEntities());
		entities.add(newPipeline);
		entities.add(newItem);
		ConfigPackage augmented = new ConfigPackage(
			pkg.getSchemaVersion(), pkg.getMetadata(), entities);

		// 1. Preview and apply both create the pipeline and its join
		ImportReport report = previewThenApply(augmented, ImportMode.SKIP);

		assertEquals(2, report.created());
		assertEquals(
			PlannedAction.Action.CREATE,
			actionOf(report, "ENRICH_PIPELINE_ITEM-NEW"));

		// 2. The join row exists with the package value
		Long pipelineId = report.resolvedIds().get("ENRICH_PIPELINE-NEW");
		Long enrichItemId = report.resolvedIds().get(enrichItemRef);
		assertNotNull(pipelineId, "the new pipeline must have been created");
		assertEquals(2.0f, loadWeight(pipelineId, enrichItemId));

		// 3. Clean up the join, then the pipeline created by this method
		removeEnrichPipelineItem(pipelineId, enrichItemId);
		EntitiesUtils.removeEntity(pipelineId, enrichPipelineService, sessionFactory);
	}

	@Test
	void an_older_export_with_the_multimodal_flag_is_still_read()
		throws JsonProcessingException {

		// an embedding model exported when the flag was still in the schema
		String json = """
			{
				"ref": "EMBEDDING_MODEL-OLD",
				"type": "EMBEDDING_MODEL",
				"key": "%s",
				"attributes": {
					"name": "%s",
					"vectorSize": 1024,
					"multimodal": true
				}
			}
			""".formatted(OLD_EMBEDDING_MODEL_NAME, OLD_EMBEDDING_MODEL_NAME);

		// the request body is read by the same mapper
		ConfigEntity entity = objectMapper.readValue(json, ConfigEntity.class);

		// the flag is dropped, the other attributes are kept
		EmbeddingModelDTO dto =
			assertInstanceOf(EmbeddingModelDTO.class, entity.getAttributes());
		assertEquals(OLD_EMBEDDING_MODEL_NAME, dto.getName());
		assertEquals(1024, dto.getVectorSize());
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

	private long countByName(String jpqlEntity, String name) {
		return sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.createQuery(
					"select count(e) from " + jpqlEntity + " e where e.name = :name",
					Long.class)
				.setParameter("name", name)
				.getSingleResult()
		).await().indefinitely();
	}

	/**
	 * Imports the package as a dry-run, then applies it, and asserts that the
	 * preview announced exactly what the apply did: the same actions and counts,
	 * with every package entity accounted for. Returns the apply report.
	 */
	private ImportReport previewThenApply(ConfigPackage pkg, ImportMode mode) {
		ImportReport preview =
			configImporter.importConfig(TENANT_ID, pkg, mode, true)
				.await().indefinitely();
		ImportReport applied =
			configImporter.importConfig(TENANT_ID, pkg, mode, false)
				.await().indefinitely();

		assertEquals(preview.actions(), applied.actions(),
			"the preview must plan the actions the apply takes");
		assertEquals(preview.created(), applied.created());
		assertEquals(preview.overwritten(), applied.overwritten());
		assertEquals(preview.skipped(), applied.skipped());
		assertEquals(pkg.getEntities().size(), applied.actions().size(),
			"every package entity, join entities included, must be planned");
		assertEquals(
			pkg.getEntities().size(),
			applied.created() + applied.overwritten() + applied.skipped());
		return applied;
	}

	private static PlannedAction.Action actionOf(ImportReport report, String ref) {
		return report.actions().stream()
			.filter(action -> ref.equals(action.ref()))
			.map(PlannedAction::action)
			.findFirst()
			.orElseThrow(() -> new AssertionError("no action planned for " + ref));
	}

	/**
	 * Seeds a new pipeline joined to an existing enrich item, and an ACL mapping
	 * between an existing plugin driver and an existing doc type field.
	 */
	private SeededJoins seedJoins(
		String pipelineName, float weight, UserField userField) {

		Long enrichItemId = firstId("EnrichItem");
		Long pluginDriverId = firstId("PluginDriver");
		Long docTypeFieldId = firstId("DocTypeField");

		Long pipelineId = sessionFactory.withTransaction(TENANT_ID, (s, t) -> {
			EnrichPipeline pipeline = new EnrichPipeline();
			pipeline.setName(pipelineName);
			return s.persist(pipeline).call(s::flush).replaceWith(pipeline);
		}).await().indefinitely().getId();

		sessionFactory.withTransaction(TENANT_ID, (s, t) -> {
			EnrichPipelineItem item = new EnrichPipelineItem();
			item.setKey(EnrichPipelineItemKey.of(pipelineId, enrichItemId));
			item.setEnrichPipeline(s.getReference(EnrichPipeline.class, pipelineId));
			item.setEnrichItem(s.getReference(EnrichItem.class, enrichItemId));
			item.setWeight(weight);

			AclMapping mapping = new AclMapping();
			mapping.setKey(
				PluginDriverDocTypeFieldKey.of(pluginDriverId, docTypeFieldId));
			mapping.setPluginDriver(
				s.getReference(PluginDriver.class, pluginDriverId));
			mapping.setDocTypeField(
				s.getReference(DocTypeField.class, docTypeFieldId));
			mapping.setUserField(userField);

			return s.persistAll(item, mapping);
		}).await().indefinitely();

		return new SeededJoins(
			pipelineId, enrichItemId, pluginDriverId, docTypeFieldId);
	}

	/**
	 * The package with the two seeded joins carrying the given values instead of
	 * the exported ones.
	 */
	private static ConfigPackage withJoinValues(
		ConfigPackage pkg, SeededJoins joins, float weight, UserField userField) {

		List<ConfigEntity> entities = new ArrayList<>(pkg.getEntities());
		entities.replaceAll(entity -> {
			if (joins.enrichPipelineItemRef().equals(entity.getRef())) {
				return new ConfigEntity(
					entity.getRef(), entity.getType(), entity.getKey(),
					new EnrichPipelineItemRepresentation(weight),
					entity.getReferences(), entity.getRedactedFields());
			}
			if (joins.aclMappingRef().equals(entity.getRef())) {
				return new ConfigEntity(
					entity.getRef(), entity.getType(), entity.getKey(),
					new AclMappingRepresentation(userField),
					entity.getReferences(), entity.getRedactedFields());
			}
			return entity;
		});
		return new ConfigPackage(pkg.getSchemaVersion(), pkg.getMetadata(), entities);
	}

	private Long firstId(String jpqlEntity) {
		return sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.createQuery("select min(e.id) from " + jpqlEntity + " e", Long.class)
				.getSingleResult()
		).await().indefinitely();
	}

	private Float loadWeight(Long pipelineId, Long enrichItemId) {
		EnrichPipelineItemKey key = EnrichPipelineItemKey.of(pipelineId, enrichItemId);
		return sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.find(EnrichPipelineItem.class, key)
				.onItem().ifNotNull().transform(EnrichPipelineItem::getWeight)
		).await().indefinitely();
	}

	private UserField loadUserField(Long pluginDriverId, Long docTypeFieldId) {
		PluginDriverDocTypeFieldKey key =
			PluginDriverDocTypeFieldKey.of(pluginDriverId, docTypeFieldId);
		return sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.find(AclMapping.class, key)
				.onItem().ifNotNull().transform(AclMapping::getUserField)
		).await().indefinitely();
	}

	private void removeJoinRows(SeededJoins joins) {
		removeEnrichPipelineItem(joins.pipelineId(), joins.enrichItemId());

		PluginDriverDocTypeFieldKey key = PluginDriverDocTypeFieldKey.of(
			joins.pluginDriverId(), joins.docTypeFieldId());
		sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.find(AclMapping.class, key)
				.onItem().ifNotNull().call(mapping -> s.remove(mapping))
		).await().indefinitely();
	}

	private void removeEnrichPipelineItem(Long pipelineId, Long enrichItemId) {
		EnrichPipelineItemKey key = EnrichPipelineItemKey.of(pipelineId, enrichItemId);
		sessionFactory.withTransaction(TENANT_ID, (s, t) ->
			s.find(EnrichPipelineItem.class, key)
				.onItem().ifNotNull().call(item -> s.remove(item))
		).await().indefinitely();
	}

	/**
	 * The endpoints of the two join rows seeded by a test: a pipeline-to-item join
	 * and a plugin-driver-to-field ACL mapping, with their package handles.
	 */
	private record SeededJoins(
		Long pipelineId, Long enrichItemId, Long pluginDriverId,
		Long docTypeFieldId) {

		String enrichPipelineItemRef() {
			return ConfigEntityType.ENRICH_PIPELINE_ITEM.name()
				+ "-" + pipelineId + "-" + enrichItemId;
		}

		String aclMappingRef() {
			return ConfigEntityType.ACL_MAPPING.name()
				+ "-" + pluginDriverId + "-" + docTypeFieldId;
		}

	}

}
