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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.ImportMode;
import io.openk9.datasource.config.model.ImportReport;
import io.openk9.datasource.config.model.representation.AclMappingRepresentation;
import io.openk9.datasource.config.model.representation.EnrichPipelineItemRepresentation;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.ProviderModel;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.base.K9EntityDTO;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.service.EnrichPipelineService;
import io.openk9.datasource.service.PluginDriverService;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for the export collector. The test boot already runs
 * {@code createDefault("public")}, so the tenant is populated; here we export it
 * and assert the structural promises of the collector.
 */
@QuarkusTest
public class ConfigExporterTest {

	private static final String TENANT_ID = "public";

	private static final String ENTITY_NAME_PREFIX = "ConfigExporterTest - ";
	private static final String EMBEDDING_MODEL_NAME =
		ENTITY_NAME_PREFIX + "embedding-model";
	private static final String ACL_DOC_TYPE_NAME = ENTITY_NAME_PREFIX + "acl-doc-type";
	private static final String ACL_FIELD_NAME = ENTITY_NAME_PREFIX + "acl-field";
	private static final String COPIED_FIELD_NAME =
		ENTITY_NAME_PREFIX + "copied-acl-field";
	private static final String COPIED_PIPELINE_NAME =
		ENTITY_NAME_PREFIX + "copied-pipeline";
	private static final String COPIED_PLUGIN_DRIVER_NAME =
		ENTITY_NAME_PREFIX + "copied-plugin-driver";

	@Inject
	ConfigExporter configExporter;

	@Inject
	ConfigImporter configImporter;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	DocTypeService docTypeService;

	@Inject
	EnrichItemService enrichItemService;

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	PluginDriverService pluginDriverService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_export_a_self_contained_package_of_the_default_tenant() {
		// 1. Export the tenant the test boot already initialized
		ConfigPackage configPackage =
			configExporter.export(TENANT_ID).await().indefinitely();

		assertEquals(
			ConfigPackage.CURRENT_SCHEMA_VERSION, configPackage.getSchemaVersion());

		List<ConfigEntity> entities = configPackage.getEntities();
		assertNotNull(entities);
		assertFalse(entities.isEmpty(), "the default tenant must export some config");

		// 2. Every handle is unique within the package
		Set<String> handles = new HashSet<>();
		for (ConfigEntity entity : entities) {
			assertTrue(
				handles.add(entity.getRef()),
				"duplicate handle: " + entity.getRef());
		}

		// 3. The graph is self-contained: every reference resolves to a handle
		// that is also exported (no dangling edge, no reference to a runtime
		// entity).
		for (ConfigEntity entity : entities) {
			for (Map.Entry<String, List<String>> reference
					: entity.getReferences().entrySet()) {

				for (String target : reference.getValue()) {
					assertTrue(
						handles.contains(target),
						entity.getRef() + " has reference '" + reference.getKey()
							+ "' to an unexported handle: " + target);
				}
			}
		}

		// 4. The TenantBinding pointers are captured in the metadata
		assertNotNull(configPackage.getMetadata());
		assertNotNull(configPackage.getMetadata().getSourceVirtualHost());

		String activeBucketRef = configPackage.getMetadata().getActiveBucketRef();
		assertNotNull(activeBucketRef);
		assertTrue(
			handles.contains(activeBucketRef),
			"activeBucketRef must point to an exported bucket");

		// 5. The active bucket (the one the tenant is bound to; a tenant may hold
		// several buckets) is wired to its query analysis and search config, which
		// createDefault establishes.
		ConfigEntity activeBucket = entities.stream()
			.filter(entity -> entity.getRef().equals(activeBucketRef))
			.findFirst()
			.orElseThrow();

		assertTrue(activeBucket.getReferences().containsKey("queryAnalysis"));
		assertTrue(activeBucket.getReferences().containsKey("searchConfig"));
	}

	@Test
	void should_export_only_the_seed_type_when_shallow() {
		// 1. Shallow export seeded with a single type
		ConfigPackage shallow = configExporter
			.export(TENANT_ID, List.of(ConfigEntityType.BUCKET), false)
			.await().indefinitely();

		List<ConfigEntity> entities = shallow.getEntities();
		assertFalse(
			entities.isEmpty(), "the default tenant must export at least one bucket");

		// 2. Shallow keeps only the seed: every node is of the requested type
		for (ConfigEntity entity : entities) {
			assertEquals(ConfigEntityType.BUCKET, entity.getType());
		}

		// 3. Metadata pointers outside the bucket-only scope are nulled, while the
		// active bucket (itself a bucket) stays wired.
		Set<String> handles = handlesOf(entities);
		String activeBucketRef = shallow.getMetadata().getActiveBucketRef();
		assertNotNull(activeBucketRef);
		assertTrue(handles.contains(activeBucketRef));
		assertNull(shallow.getMetadata().getEnabledEmbeddingModelRef());
		assertNull(shallow.getMetadata().getEnabledLargeLanguageModelRef());
	}

	@Test
	void should_close_over_dependencies_when_deep() {
		// 1. Deep and shallow exports seeded with the same single type
		ConfigPackage deep = configExporter
			.export(TENANT_ID, List.of(ConfigEntityType.BUCKET), true)
			.await().indefinitely();
		ConfigPackage shallow = configExporter
			.export(TENANT_ID, List.of(ConfigEntityType.BUCKET), false)
			.await().indefinitely();

		Set<String> deepHandles = handlesOf(deep.getEntities());
		Set<String> shallowHandles = handlesOf(shallow.getEntities());

		// 2. Deep is a strict superset: it keeps the seed and pulls in more
		assertTrue(deepHandles.containsAll(shallowHandles));
		assertTrue(
			deepHandles.size() > shallowHandles.size(),
			"deep export must pull in the bucket dependencies");

		// 3. Deep is self-contained: every reference resolves within the package
		for (ConfigEntity entity : deep.getEntities()) {
			for (List<String> targets : entity.getReferences().values()) {
				for (String target : targets) {
					assertTrue(
						deepHandles.contains(target),
						entity.getRef() + " dangles a reference to " + target);
				}
			}
		}

		// 4. The active bucket's queryAnalysis and searchConfig travelled along
		String activeBucketRef = deep.getMetadata().getActiveBucketRef();
		ConfigEntity activeBucket = deep.getEntities().stream()
			.filter(entity -> entity.getRef().equals(activeBucketRef))
			.findFirst()
			.orElseThrow();
		for (String target : activeBucket.getReferences().get("queryAnalysis")) {
			assertTrue(deepHandles.contains(target));
		}
		for (String target : activeBucket.getReferences().get("searchConfig")) {
			assertTrue(deepHandles.contains(target));
		}
	}

	@Test
	void should_carry_the_pipeline_joins_and_items_when_deep() {
		// 1. The full export holds the init pipeline with its joins and items
		ConfigPackage full = configExporter.export(TENANT_ID).await().indefinitely();
		String pipelineRef = nodeOf(
			full, ConfigEntityType.ENRICH_PIPELINE,
			Initializer.INIT_DATASOURCE_PIPELINE).getRef();
		List<ConfigEntity> joins = joinsOf(full, "enrichPipeline", pipelineRef);
		assertFalse(joins.isEmpty(), "the init pipeline must have enrich items");

		// 2. A deep export seeded with the pipelines carries the same joins and
		// the enrich items they connect
		ConfigPackage deep = exportType(ConfigEntityType.ENRICH_PIPELINE, true);

		assertTrue(
			handlesOf(deep.getEntities()).containsAll(handlesWithEndpoints(joins)),
			"the deep export must carry the pipeline joins and their items");
		assertSelfContained(deep);
	}

	@Test
	void should_reach_the_pipeline_items_from_a_datasource_when_deep() {
		// 1. The init datasource points at the init pipeline, whose items are
		// linked only through the join entities
		ConfigPackage full = configExporter.export(TENANT_ID).await().indefinitely();
		String pipelineRef = nodeOf(
			full, ConfigEntityType.ENRICH_PIPELINE,
			Initializer.INIT_DATASOURCE_PIPELINE).getRef();
		ConfigEntity datasource = nodeOf(
			full, ConfigEntityType.DATASOURCE,
			Initializer.INIT_DATASOURCE_CONNECTION);
		assertEquals(
			List.of(pipelineRef), datasource.getReferences().get("enrichPipeline"));
		List<ConfigEntity> joins = joinsOf(full, "enrichPipeline", pipelineRef);
		assertFalse(joins.isEmpty(), "the init pipeline must have enrich items");

		// 2. A deep export seeded with the datasources reaches the pipeline, its
		// joins and their items
		ConfigPackage deep = exportType(ConfigEntityType.DATASOURCE, true);
		Set<String> handles = handlesOf(deep.getEntities());

		assertTrue(handles.contains(pipelineRef));
		assertTrue(
			handles.containsAll(handlesWithEndpoints(joins)),
			"the deep export must carry the pipeline joins and their items");
		assertSelfContained(deep);
	}

	@Test
	void should_leave_the_joins_out_when_shallow() {
		// 1. Shallow export seeded with the pipelines alone
		ConfigPackage shallow = exportType(ConfigEntityType.ENRICH_PIPELINE, false);
		assertFalse(shallow.getEntities().isEmpty());

		// 2. Only the seed travels: no join, no enrich item
		for (ConfigEntity entity : shallow.getEntities()) {
			assertEquals(ConfigEntityType.ENRICH_PIPELINE, entity.getType());
		}
	}

	@Test
	void should_not_climb_from_the_enrich_items_to_their_pipelines() {
		// 1. Deep export seeded with the enrich items, the far side of the joins
		ConfigPackage deep = exportType(ConfigEntityType.ENRICH_ITEM, true);
		assertFalse(deep.getEntities().isEmpty());

		// 2. A join is followed from its pipeline only: neither the joins nor
		// the pipelines using the items are pulled in
		for (ConfigEntity entity : deep.getEntities()) {
			assertNotEquals(ConfigEntityType.ENRICH_PIPELINE_ITEM, entity.getType());
			assertNotEquals(ConfigEntityType.ENRICH_PIPELINE, entity.getType());
		}
	}

	@Test
	void should_never_export_a_join_without_its_endpoints() {
		// 1. Shallow, a join type seeded alone has no endpoint to travel with,
		// so no join is exported
		ConfigPackage shallow =
			exportType(ConfigEntityType.ENRICH_PIPELINE_ITEM, false);

		for (ConfigEntity entity : shallow.getEntities()) {
			assertNotEquals(
				ConfigEntityType.ENRICH_PIPELINE_ITEM, entity.getType(),
				"a join must not travel without its endpoints: " + entity.getRef());
		}
		assertSelfContained(shallow);

		// 2. Deep, the joins travel with both their endpoints
		ConfigPackage deep = exportType(ConfigEntityType.ENRICH_PIPELINE_ITEM, true);

		assertTrue(deep.getEntities().stream().anyMatch(entity ->
			entity.getType() == ConfigEntityType.ENRICH_PIPELINE_ITEM));
		assertSelfContained(deep);
	}

	@Test
	void should_carry_the_acl_mappings_and_their_fields_when_deep() {
		// 1. Map a new doc type field on the init plugin driver
		AclFixture fixture = createAclFixture();

		try {
			ConfigPackage full =
				configExporter.export(TENANT_ID).await().indefinitely();
			String pluginDriverRef = nodeOf(
				full, ConfigEntityType.PLUGIN_DRIVER,
				Initializer.INIT_DATASOURCE_PLUGIN).getRef();
			String fieldRef = nodeOf(
				full, ConfigEntityType.DOC_TYPE_FIELD, ACL_FIELD_NAME).getRef();
			Set<String> expected = handlesWithEndpoints(
				joinsOf(full, "pluginDriver", pluginDriverRef));
			assertTrue(
				expected.contains(fieldRef),
				"the init plugin driver must map the new field");

			// 2. Deep from the plugin drivers: the ACL mappings and their fields
			// travel along
			ConfigPackage fromPluginDrivers =
				exportType(ConfigEntityType.PLUGIN_DRIVER, true);

			assertTrue(handlesOf(fromPluginDrivers.getEntities()).containsAll(expected));
			assertSelfContained(fromPluginDrivers);

			// 3. Deep from the datasources: reached through the plugin driver
			ConfigPackage fromDatasources =
				exportType(ConfigEntityType.DATASOURCE, true);

			assertTrue(handlesOf(fromDatasources.getEntities()).containsAll(expected));
			assertSelfContained(fromDatasources);

			// 4. Deep from the fields: an ACL mapping is followed from its plugin
			// driver only, so the field never climbs back to it
			ConfigPackage fromFields =
				exportType(ConfigEntityType.DOC_TYPE_FIELD, true);
			Set<String> fieldHandles = handlesOf(fromFields.getEntities());

			assertTrue(fieldHandles.contains(fieldRef));
			assertFalse(fieldHandles.contains(pluginDriverRef));
			for (ConfigEntity entity : fromFields.getEntities()) {
				assertNotEquals(ConfigEntityType.ACL_MAPPING, entity.getType());
			}
		}
		finally {
			// 5. Clean up the fixture
			removeAclFixture(fixture);
		}
	}

	@Test
	void should_rebuild_the_pipeline_items_from_a_deep_export() {
		// 1. Deep export of the pipelines; rename the init pipeline and its items
		// so the import matches none of them, as in a clean tenant
		ConfigPackage deep = exportType(ConfigEntityType.ENRICH_PIPELINE, true);
		ConfigEntity pipeline = nodeOf(
			deep, ConfigEntityType.ENRICH_PIPELINE,
			Initializer.INIT_DATASOURCE_PIPELINE);
		List<ConfigEntity> joins = joinsOf(deep, "enrichPipeline", pipeline.getRef());
		assertFalse(joins.isEmpty(), "the deep export must carry the pipeline joins");

		rename(pipeline, COPIED_PIPELINE_NAME);
		List<String> itemRefs = new ArrayList<>();
		for (ConfigEntity join : joins) {
			ConfigEntity item =
				nodeOf(deep, join.getReferences().get("enrichItem").getFirst());
			rename(item, ENTITY_NAME_PREFIX + item.getKey());
			itemRefs.add(item.getRef());
		}
		Map<String, Float> expectedWeights = itemWeights(deep, pipeline.getRef());

		// 2. Import the package
		ImportReport report = configImporter
			.importConfig(TENANT_ID, deep, ImportMode.SKIP, false)
			.await().indefinitely();
		Long pipelineId = report.resolvedIds().get(pipeline.getRef());

		try {
			// 3. A new export holds the rebuilt pipeline with the same items and
			// weights
			ConfigPackage reexport =
				exportType(ConfigEntityType.ENRICH_PIPELINE, true);
			String copiedRef = nodeOf(
				reexport, ConfigEntityType.ENRICH_PIPELINE,
				COPIED_PIPELINE_NAME).getRef();

			assertEquals(expectedWeights, itemWeights(reexport, copiedRef));
		}
		finally {
			// 4. Clean up: the pipeline with its joins, then the items
			EntitiesUtils.removeEntity(pipelineId, enrichPipelineService, sessionFactory);
			for (String itemRef : itemRefs) {
				EntitiesUtils.removeEntity(
					report.resolvedIds().get(itemRef), enrichItemService, sessionFactory);
			}
		}
	}

	@Test
	void should_rebuild_the_acl_mappings_from_a_deep_export() {
		// 1. Map a new doc type field on the init plugin driver
		AclFixture fixture = createAclFixture();
		ImportReport report = null;
		ConfigEntity pluginDriver = null;
		ConfigEntity field = null;

		try {
			// 2. Deep export of the plugin drivers; rename the init plugin driver
			// and the mapped field so the import creates both
			ConfigPackage deep = exportType(ConfigEntityType.PLUGIN_DRIVER, true);
			pluginDriver = nodeOf(
				deep, ConfigEntityType.PLUGIN_DRIVER,
				Initializer.INIT_DATASOURCE_PLUGIN);
			field = nodeOf(deep, ConfigEntityType.DOC_TYPE_FIELD, ACL_FIELD_NAME);

			rename(pluginDriver, COPIED_PLUGIN_DRIVER_NAME);
			rename(field, COPIED_FIELD_NAME);
			((DocTypeFieldDTO) field.getAttributes()).setFieldName("copied-acl-field");
			Map<String, UserField> expectedUserFields =
				aclUserFields(deep, pluginDriver.getRef());
			assertTrue(
				expectedUserFields.containsKey(COPIED_FIELD_NAME),
				"the deep export must carry the ACL mapping of the new field");

			// 3. Import the package
			report = configImporter
				.importConfig(TENANT_ID, deep, ImportMode.SKIP, false)
				.await().indefinitely();

			// 4. A new export holds the created plugin driver mapping the same
			// fields with the same user fields
			ConfigPackage reexport =
				exportType(ConfigEntityType.PLUGIN_DRIVER, true);
			String copiedRef = nodeOf(
				reexport, ConfigEntityType.PLUGIN_DRIVER,
				COPIED_PLUGIN_DRIVER_NAME).getRef();

			assertEquals(expectedUserFields, aclUserFields(reexport, copiedRef));
		}
		finally {
			// 5. Clean up: the created plugin driver with its mappings and the
			// created field, then the fixture
			if (report != null) {
				EntitiesUtils.removeEntity(
					report.resolvedIds().get(pluginDriver.getRef()),
					pluginDriverService, sessionFactory);
				EntitiesUtils.removeEntity(
					report.resolvedIds().get(field.getRef()),
					docTypeFieldService, sessionFactory);
			}
			removeAclFixture(fixture);
		}
	}

	@Test
	void should_redact_the_embedding_model_api_key() {
		// 1. Persist an embedding model carrying a secret apiKey
		EmbeddingModel model = new EmbeddingModel();
		model.setName(EMBEDDING_MODEL_NAME);
		model.setApiKey("super-secret-key");
		// provider/model are NOT NULL columns (embedded ProviderModel)
		model.setProviderModel(new ProviderModel("test-model", "test-provider"));

		sessionFactory
			.withTransaction(TENANT_ID, (s, t) -> s.persist(model))
			.await()
			.indefinitely();

		try {
			// 2. Export and locate the corresponding node
			ConfigPackage configPackage =
				configExporter.export(TENANT_ID).await().indefinitely();

			ConfigEntity node = configPackage.getEntities().stream()
				.filter(entity -> entity.getType() == ConfigEntityType.EMBEDDING_MODEL)
				.filter(entity -> EMBEDDING_MODEL_NAME.equals(entity.getKey()))
				.findFirst()
				.orElseThrow();

			// 3. The apiKey is redacted and recorded in redactedFields
			EmbeddingModelDTO attributes = (EmbeddingModelDTO) node.getAttributes();
			assertEquals(ConfigRedactor.PLACEHOLDER, attributes.getApiKey());
			assertNotNull(node.getRedactedFields());
			assertTrue(node.getRedactedFields().contains("apiKey"));
		}
		finally {
			// 4. Clean up the fixture so other tests stay isolated
			sessionFactory
				.withTransaction(TENANT_ID, (s, t) ->
					s.remove(s.getReference(EmbeddingModel.class, model.getId())))
				.await()
				.indefinitely();
		}
	}

	private static Set<String> handlesOf(List<ConfigEntity> entities) {
		Set<String> handles = new HashSet<>();
		for (ConfigEntity entity : entities) {
			handles.add(entity.getRef());
		}
		return handles;
	}

	private static Map<String, UserField> aclUserFields(
		ConfigPackage configPackage, String pluginDriverRef) {

		Map<String, UserField> userFields = new HashMap<>();
		for (ConfigEntity join : joinsOf(configPackage, "pluginDriver", pluginDriverRef)) {
			ConfigEntity field = nodeOf(
				configPackage, join.getReferences().get("docTypeField").getFirst());
			AclMappingRepresentation attributes =
				(AclMappingRepresentation) join.getAttributes();
			userFields.put(field.getKey(), attributes.getUserField());
		}
		return userFields;
	}

	private static void assertSelfContained(ConfigPackage configPackage) {
		Set<String> handles = handlesOf(configPackage.getEntities());
		for (ConfigEntity entity : configPackage.getEntities()) {
			for (List<String> targets : entity.getReferences().values()) {
				for (String target : targets) {
					assertTrue(
						handles.contains(target),
						entity.getRef() + " dangles a reference to " + target);
				}
			}
		}
	}

	private static Set<String> handlesWithEndpoints(List<ConfigEntity> joins) {
		Set<String> handles = new HashSet<>();
		for (ConfigEntity join : joins) {
			handles.add(join.getRef());
			for (List<String> targets : join.getReferences().values()) {
				handles.addAll(targets);
			}
		}
		return handles;
	}

	private static Map<String, Float> itemWeights(
		ConfigPackage configPackage, String pipelineRef) {

		Map<String, Float> weights = new HashMap<>();
		for (ConfigEntity join : joinsOf(configPackage, "enrichPipeline", pipelineRef)) {
			ConfigEntity item = nodeOf(
				configPackage, join.getReferences().get("enrichItem").getFirst());
			EnrichPipelineItemRepresentation attributes =
				(EnrichPipelineItemRepresentation) join.getAttributes();
			weights.put(item.getKey(), attributes.getWeight());
		}
		return weights;
	}

	/**
	 * The join entities whose {@code parentReference} points at {@code parentRef}.
	 */
	private static List<ConfigEntity> joinsOf(
		ConfigPackage configPackage, String parentReference, String parentRef) {

		List<ConfigEntity> joins = new ArrayList<>();
		for (ConfigEntity entity : configPackage.getEntities()) {
			List<String> parents = entity.getReferences().get(parentReference);
			if (ConfigMatcher.isJoinEntity(entity.getType().getEntityType())
				&& parents != null && parents.contains(parentRef)) {

				joins.add(entity);
			}
		}
		return joins;
	}

	private static ConfigEntity nodeOf(ConfigPackage configPackage, String ref) {
		return configPackage.getEntities().stream()
			.filter(entity -> entity.getRef().equals(ref))
			.findFirst()
			.orElseThrow();
	}

	private static ConfigEntity nodeOf(
		ConfigPackage configPackage, ConfigEntityType type, String key) {

		return configPackage.getEntities().stream()
			.filter(entity -> entity.getType() == type && key.equals(entity.getKey()))
			.findFirst()
			.orElseThrow();
	}

	/**
	 * Renames a node in the package, both its matching key and its attributes,
	 * so the import finds no existing entity to match it against.
	 */
	private static void rename(ConfigEntity entity, String name) {
		entity.setKey(name);
		((K9EntityDTO) entity.getAttributes()).setName(name);
	}

	/**
	 * Persists a doc type with one field and maps the field on the init plugin
	 * driver.
	 */
	private AclFixture createAclFixture() {
		PluginDriver pluginDriver = EntitiesUtils.getEntity(
			Initializer.INIT_DATASOURCE_PLUGIN, pluginDriverService, sessionFactory);
		DocType docType = docTypeService
			.create(DocTypeDTO.builder().name(ACL_DOC_TYPE_NAME).build())
			.await().indefinitely();
		DocTypeField field = docTypeService
			.addDocTypeField(docType.getId(), DocTypeFieldDTO.builder()
				.name(ACL_FIELD_NAME)
				.fieldName("acl-field")
				.fieldType(FieldType.TEXT)
				.build())
			.await().indefinitely()
			.right;
		pluginDriverService
			.addDocTypeField(pluginDriver.getId(), field.getId(), UserField.USERNAME)
			.await().indefinitely();

		return new AclFixture(pluginDriver.getId(), docType.getId(), field.getId());
	}

	private ConfigPackage exportType(
		ConfigEntityType type, boolean includeDependencies) {

		return configExporter
			.export(TENANT_ID, List.of(type), includeDependencies)
			.await().indefinitely();
	}

	/**
	 * Removes the fixture of {@link #createAclFixture()}, the mapping first so
	 * neither endpoint is still referenced.
	 */
	private void removeAclFixture(AclFixture fixture) {
		pluginDriverService
			.removeDocTypeField(fixture.pluginDriverId(), fixture.docTypeFieldId())
			.await().indefinitely();
		EntitiesUtils.removeEntity(
			fixture.docTypeFieldId(), docTypeFieldService, sessionFactory);
		EntitiesUtils.removeEntity(fixture.docTypeId(), docTypeService, sessionFactory);
	}

	private record AclFixture(
		long pluginDriverId, long docTypeId, long docTypeFieldId
	) {}

}
