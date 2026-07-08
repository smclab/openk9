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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.persistence.metamodel.EntityType;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.ProviderModel;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.util.ExportIgnore;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

	@Inject
	ConfigExporter configExporter;

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

		String defaultBucketRef = configPackage.getMetadata().getDefaultBucketRef();
		assertNotNull(defaultBucketRef);
		assertTrue(
			handles.contains(defaultBucketRef),
			"defaultBucketRef must point to an exported bucket");

		// 5. The default bucket (the one the tenant is bound to; a tenant may hold
		// several buckets) is wired to its query analysis and search config, which
		// createDefault establishes.
		ConfigEntity defaultBucket = entities.stream()
			.filter(entity -> entity.getRef().equals(defaultBucketRef))
			.findFirst()
			.orElseThrow();

		assertTrue(defaultBucket.getReferences().containsKey("queryAnalysis"));
		assertTrue(defaultBucket.getReferences().containsKey("searchConfig"));
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

	@Test
	void every_persistent_entity_is_exported_or_explicitly_ignored() {
		// Opt-out by governance: every JPA entity must be either exportable
		// (declared in ConfigEntityType) or explicitly @ExportIgnore, so a newly
		// added entity fails the build until a deliberate choice is made.
		Set<Class<?>> exportable = new HashSet<>();
		for (ConfigEntityType type : ConfigEntityType.values()) {
			exportable.add(type.getEntityType());
		}

		List<String> undecided = new ArrayList<>();
		for (EntityType<?> entityType : sessionFactory.getMetamodel().getEntities()) {
			Class<?> javaType = entityType.getJavaType();
			if (!exportable.contains(javaType)
				&& !javaType.isAnnotationPresent(ExportIgnore.class)) {

				undecided.add(javaType.getSimpleName());
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
		// reflectively; assert the overload exists for every registered type.
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

}
