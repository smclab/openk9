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
import java.util.Set;

import io.openk9.datasource.config.model.representation.EnrichPipelineItemRepresentation;
import io.openk9.datasource.model.dto.base.DatasourceDTO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure unit round-trip (no Quarkus, no Docker) demonstrating that a
 * {@link ConfigPackage} serializes to JSON and deserializes back with the
 * {@code attributes} bound to their concrete, typed DTO and references kept as
 * local handles. This is the evidence behind ADR-0003 §3c.
 */
class ConfigPackageRoundTripTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldRoundTripTypedAttributesAndHandleReferences() throws Exception {
		DatasourceDTO datasource = DatasourceDTO.builder()
			.name("github-connector")
			.description("GitHub data source")
			.schedulable(true)
			.scheduling("0 0 * * * ?")
			.build();

		ConfigEntity datasourceEntity = new ConfigEntity(
			"ds-1",
			ConfigEntityType.DATASOURCE,
			"github-connector",
			datasource,
			Map.of("pluginDriver", List.of("pd-1"), "enrichPipeline", List.of("ep-1")),
			List.of()
		);

		ConfigEntity pipelineItem = new ConfigEntity(
			"epi-1",
			ConfigEntityType.ENRICH_PIPELINE_ITEM,
			null,
			new EnrichPipelineItemRepresentation(2.0f),
			Map.of("enrichPipeline", List.of("ep-1"), "enrichItem", List.of("ei-1")),
			List.of()
		);

		ConfigMetadata metadata = new ConfigMetadata();
		metadata.setSourceVirtualHost("tenant.example.com");
		metadata.setActiveBucketRef("bucket-1");

		ConfigPackage original = new ConfigPackage(
			ConfigPackage.CURRENT_SCHEMA_VERSION,
			metadata,
			List.of(datasourceEntity, pipelineItem)
		);

		String json = mapper.writeValueAsString(original);
		ConfigPackage restored = mapper.readValue(json, ConfigPackage.class);

		assertEquals(ConfigPackage.CURRENT_SCHEMA_VERSION, restored.getSchemaVersion());
		assertEquals("bucket-1", restored.getMetadata().getActiveBucketRef());

		ConfigEntity restoredDatasource = restored.getEntities().get(0);
		assertEquals(ConfigEntityType.DATASOURCE, restoredDatasource.getType());

		DatasourceDTO restoredDto =
			assertInstanceOf(DatasourceDTO.class, restoredDatasource.getAttributes());
		assertEquals("github-connector", restoredDto.getName());
		assertEquals("0 0 * * * ?", restoredDto.getScheduling());
		assertEquals(
			List.of("pd-1"), restoredDatasource.getReferences().get("pluginDriver"));

		ConfigEntity restoredItem = restored.getEntities().get(1);
		EnrichPipelineItemRepresentation weightRep = assertInstanceOf(
			EnrichPipelineItemRepresentation.class, restoredItem.getAttributes());
		assertEquals(2.0f, weightRep.getWeight());
		assertNull(restoredItem.getKey());
	}

	@Test
	void shouldTellAnAbsentAttributeFromANullOne() throws Exception {
		// a package written before the index configuration existed has no key for
		// it, while one that resets a value writes the key as null
		String json = """
			{
			  "ref": "ds-1",
			  "type": "DATASOURCE",
			  "key": "github-connector",
			  "attributes": {"name": "github-connector", "settings": null}
			}""";

		ConfigEntity entity = mapper.readValue(json, ConfigEntity.class);

		// the typed attributes can not tell them apart
		DatasourceDTO dto = assertInstanceOf(DatasourceDTO.class, entity.getAttributes());
		assertNull(dto.getSettings());
		assertNull(dto.getKnnIndex());

		// the keys that were written can
		assertEquals(Set.of("name", "settings"), entity.getAttributeKeys());
	}

	@Test
	void shouldWriteEveryAttributeEvenWhenNull() throws Exception {
		ConfigEntity entity = new ConfigEntity(
			"ds-1",
			ConfigEntityType.DATASOURCE,
			"github-connector",
			DatasourceDTO.builder().name("github-connector").build(),
			Map.of(),
			null
		);

		JsonNode attributes = mapper.readTree(mapper.writeValueAsString(entity))
			.get("attributes");

		// a null is written as a key set to null, so that a reset can be told
		// from a field the package does not manage
		assertTrue(attributes.has("knnIndex"));
		assertTrue(attributes.get("knnIndex").isNull());
		assertTrue(attributes.has("settings"));
		assertTrue(attributes.get("settings").isNull());

		// and the keys the entity was read with are not part of the package
		assertFalse(mapper.readTree(mapper.writeValueAsString(entity))
			.has("attributeKeys"));
	}

}
