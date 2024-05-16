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

package io.openk9.datasource.util;

import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.opensearch.Version;
import org.opensearch.cluster.metadata.IndexMetadata;
import org.opensearch.common.bytes.BytesArray;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.NamedXContentRegistry;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AnalyzerScope;
import org.opensearch.index.analysis.IndexAnalyzers;
import org.opensearch.index.analysis.NamedAnalyzer;
import org.opensearch.index.mapper.DocumentMapper;
import org.opensearch.index.mapper.MapperService;
import org.opensearch.index.mapper.Mapping;
import org.opensearch.index.mapper.ParsedDocument;
import org.opensearch.index.mapper.SourceToParse;
import org.opensearch.index.similarity.SimilarityService;
import org.opensearch.indices.IndicesModule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ElasticSearchUtils {

	private static final String IGNORE_INDEX = "IGNORE_INDEX";
	private static final String DOCUMENT_TYPE = MapperService.SINGLE_MAPPING_NAME;
	private static MapperService mapperService = null;

	private ElasticSearchUtils() {
	}

	public static JsonObject getDynamicMapping(
		IngestionPayload ingestionPayload,
		IngestionPayloadMapper mapper) {

		var documentTypes = IngestionPayloadMapper.getDocumentTypes(ingestionPayload);

		var dataPayload = mapper.map(ingestionPayload, documentTypes);

		return ElasticSearchUtils.getDynamicMapping(Json.encodeToBuffer(dataPayload).getBytes());
	}

	public static JsonObject getDynamicMapping(byte[] payload) {

		DocumentMapper documentMapper = getMapperService()
			.documentMapperWithAutoCreate()
			.getDocumentMapper();

		ParsedDocument doc = documentMapper.parse(sourceToParse(payload));

		Mapping mapping = documentMapper.mapping();
		if (mapping != null) {
			doc.addDynamicMappingsUpdate(mapping);
		}

		var mappings = ((JsonObject) Json
			.decodeValue(doc
				.dynamicMappingsUpdate()
				.toString()
			)
		).getJsonObject(DOCUMENT_TYPE);

		mappings.put(
			"settings",
			JsonObject.of(
				"index",
				JsonObject.of(
					"number_of_shards", "1",
					"number_of_replicas", "1",
					"highlight", JsonObject.of(
						"max_analyzed_offset", "10000000"
					)
				)
			)
		);

		return mappings;
	}

	private static SourceToParse sourceToParse(byte[] payload) {

		return new SourceToParse(
			IGNORE_INDEX,
			DOCUMENT_TYPE,
			new BytesArray(payload),
			XContentType.JSON
		);

	}

	private static MapperService getMapperService() {
		if (mapperService == null) {
			IndicesModule indicesModule = new IndicesModule(List.of());

			var indexSettings = getIndexSettings();

			AtomicInteger closes = new AtomicInteger(0);
			NamedAnalyzer default_ = new NamedAnalyzer(
				"default",
				AnalyzerScope.INDEX,
				new StandardAnalyzer()
			) {
				@Override
				public void close() {
					super.close();
					closes.incrementAndGet();
				}
			};

			SimilarityService similarityService = new SimilarityService(
				indexSettings,
				null,
				Map.of()
			);

			mapperService = new MapperService(
				indexSettings,
				new IndexAnalyzers(Map.of("default", default_), Map.of(), Map.of()),
				NamedXContentRegistry.EMPTY,
				similarityService,
				indicesModule.getMapperRegistry(),
				() -> {
					throw new UnsupportedOperationException();
				},
				() -> true,
				null
			);
		}

		return mapperService;
	}

	private static IndexSettings getIndexSettings() {
		IndexMetadata indexMetadata = IndexMetadata
			.builder(IGNORE_INDEX)
			.settings(Settings.builder()
				.put("index.number_of_replicas", 0)
				.put("index.number_of_shards", 1)
				.put("index.version.created", Version.CURRENT)
				.build()
			)
			.build();

		IndexSettings indexSettings = new IndexSettings(indexMetadata, Settings.EMPTY);
		return indexSettings;
	}

}
