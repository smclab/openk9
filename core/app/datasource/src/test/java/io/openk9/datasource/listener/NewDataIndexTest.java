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

package io.openk9.datasource.listener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.ml.grpc.EmbeddingOuterClass;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The reindex builds the new {@link DataIndex} by inheriting from the one it
 * replaces. This is the list of properties that must survive a reindex.
 */
class NewDataIndexTest {

	private static final String EMBEDDING_JSON_CONFIG = "{\"batch\": 8}";
	private static final String NEW_DATA_INDEX_NAME = "7-data-a-schedule-id";
	private static final String SETTINGS = "{\"index\": {\"number_of_replicas\": 2}}";

	@Test
	@DisplayName("Should inherit every property that describes the indexing")
	void should_inherit_from_the_replaced_data_index() {
		// an old dataIndex configured away from the defaults
		var datasource = new Datasource();
		var embeddingDocTypeField = new DocTypeField();
		var docType = new DocType();
		var category = new DocTypeField();
		category.setName("category");
		var title = new DocTypeField();
		title.setName("title");

		var oldDataIndex = new DataIndex();
		oldDataIndex.setName("7-data-an-older-schedule-id");
		oldDataIndex.setDatasource(datasource);
		oldDataIndex.setKnnIndex(true);
		oldDataIndex.setChunkType(
			EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TEXT_SPLITTER);
		oldDataIndex.setChunkWindowSize(3);
		oldDataIndex.setEmbeddingJsonConfig(EMBEDDING_JSON_CONFIG);
		oldDataIndex.setEmbeddingDocTypeField(embeddingDocTypeField);
		oldDataIndex.setSettings(SETTINGS);
		oldDataIndex.setDocTypes(new LinkedHashSet<>(Set.of(docType)));
		oldDataIndex.setChunkHeaderFields(new ArrayList<>(List.of(category, title)));

		// the dataIndex the reindex will create
		var newDataIndex = JobScheduler.newDataIndex(
			NEW_DATA_INDEX_NAME, datasource, oldDataIndex);

		// it is a new index on the same datasource
		assertEquals(NEW_DATA_INDEX_NAME, newDataIndex.getName());
		assertEquals(datasource, newDataIndex.getDatasource());

		// and it inherits everything that describes how documents are indexed
		assertTrue(newDataIndex.getKnnIndex());
		assertEquals(
			EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TEXT_SPLITTER,
			newDataIndex.getChunkType()
		);
		assertEquals(3, newDataIndex.getChunkWindowSize());
		assertEquals(EMBEDDING_JSON_CONFIG, newDataIndex.getEmbeddingJsonConfig());
		assertEquals(embeddingDocTypeField, newDataIndex.getEmbeddingDocTypeField());
		assertEquals(SETTINGS, newDataIndex.getSettings());
		assertEquals(Set.of(docType), newDataIndex.getDocTypes());

		assertEquals(List.of(category, title), newDataIndex.getChunkHeaderFields());

		// the docTypes and the header are copied, not shared with the replaced
		// dataIndex
		assertNotSame(oldDataIndex.getDocTypes(), newDataIndex.getDocTypes());
		assertNotSame(
			oldDataIndex.getChunkHeaderFields(), newDataIndex.getChunkHeaderFields());
	}

	@Test
	@DisplayName("Should build a default dataIndex when there is nothing to inherit")
	void should_build_a_default_data_index_for_a_first_indexing() {
		// a datasource that has never been indexed has no dataIndex to inherit from
		var datasource = new Datasource();

		var newDataIndex = JobScheduler.newDataIndex(
			NEW_DATA_INDEX_NAME, datasource, null);

		assertEquals(NEW_DATA_INDEX_NAME, newDataIndex.getName());
		assertEquals(datasource, newDataIndex.getDatasource());
		assertFalse(newDataIndex.getKnnIndex());
		assertNull(newDataIndex.getEmbeddingDocTypeField());
		assertNull(newDataIndex.getSettings());
		assertTrue(newDataIndex.getDocTypes().isEmpty());
		assertTrue(newDataIndex.getChunkHeaderFields().isEmpty());
	}

	@Test
	@DisplayName("Should take the shape the datasource asks for on a first indexing")
	void should_take_the_shape_of_the_datasource_for_a_first_indexing() {
		// a datasource configured away from the defaults, never indexed
		var embeddingDocTypeField = new DocTypeField();
		var docType = new DocType();

		var datasource = new Datasource();
		datasource.setKnnIndex(true);
		datasource.setChunkType(
			EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TEXT_SPLITTER);
		datasource.setChunkWindowSize(3);
		datasource.setEmbeddingJsonConfig(EMBEDDING_JSON_CONFIG);
		datasource.setEmbeddingDocTypeField(embeddingDocTypeField);
		datasource.setSettings(SETTINGS);
		datasource.setDocTypes(new LinkedHashSet<>(Set.of(docType)));

		// the dataIndex of the first indexing
		var newDataIndex = JobScheduler.newDataIndex(
			NEW_DATA_INDEX_NAME, datasource, null);

		// it is created the way the datasource asks for
		assertTrue(newDataIndex.getKnnIndex());
		assertEquals(
			EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TEXT_SPLITTER,
			newDataIndex.getChunkType()
		);
		assertEquals(3, newDataIndex.getChunkWindowSize());
		assertEquals(EMBEDDING_JSON_CONFIG, newDataIndex.getEmbeddingJsonConfig());
		assertEquals(embeddingDocTypeField, newDataIndex.getEmbeddingDocTypeField());
		assertEquals(SETTINGS, newDataIndex.getSettings());
		assertEquals(Set.of(docType), newDataIndex.getDocTypes());

		// the docTypes are copied, not shared with the datasource
		assertNotSame(datasource.getDocTypes(), newDataIndex.getDocTypes());
	}

	@Test
	@DisplayName("Should ignore the shape of the datasource when there is a dataIndex")
	void should_ignore_the_shape_of_the_datasource_when_replacing_a_data_index() {
		// the datasource asks for a knn index, but the one it has is not
		var datasource = new Datasource();
		datasource.setKnnIndex(true);
		datasource.setChunkWindowSize(9);

		var oldDataIndex = new DataIndex();
		oldDataIndex.setDatasource(datasource);

		var newDataIndex = JobScheduler.newDataIndex(
			NEW_DATA_INDEX_NAME, datasource, oldDataIndex);

		// a reindex changes the index and not its configuration
		assertFalse(newDataIndex.getKnnIndex());
		assertEquals(0, newDataIndex.getChunkWindowSize());
	}

}
