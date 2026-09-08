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

import java.util.LinkedHashSet;
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

		// the docTypes are copied, not shared with the replaced dataIndex
		assertNotSame(oldDataIndex.getDocTypes(), newDataIndex.getDocTypes());
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
	}

}
