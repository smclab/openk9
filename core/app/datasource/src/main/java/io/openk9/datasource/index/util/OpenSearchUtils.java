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

package io.openk9.datasource.index.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import io.openk9.datasource.index.exception.IndexMappingException;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.processor.payload.IngestionPayload;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.opensearch.Version;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.cluster.ClusterModule;
import org.opensearch.cluster.metadata.IndexMetadata;
import org.opensearch.common.compress.CompressedXContent;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.core.common.bytes.BytesArray;
import org.opensearch.core.common.bytes.BytesReference;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AnalyzerScope;
import org.opensearch.index.analysis.IndexAnalyzers;
import org.opensearch.index.analysis.NamedAnalyzer;
import org.opensearch.index.mapper.DocumentMapper;
import org.opensearch.index.mapper.MapperService;
import org.opensearch.index.mapper.Mapping;
import org.opensearch.index.mapper.ParsedDocument;
import org.opensearch.index.mapper.SourceToParse;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.WrapperQueryBuilder;
import org.opensearch.index.similarity.SimilarityService;
import org.opensearch.indices.IndicesModule;
import org.opensearch.indices.mapper.MapperRegistry;
import org.opensearch.script.ScriptModule;
import org.opensearch.script.ScriptService;

public class OpenSearchUtils {

	private static final String IGNORE_INDEX = "IGNORE_INDEX";
	private static final String DOCUMENT_TYPE = MapperService.SINGLE_MAPPING_NAME;
	private static final Settings INDEX_SETTING =
		Settings.builder().put(
			"index.version.created",
			Version.CURRENT
		).build();
	private static final NamedXContentRegistry DEFAULT_NAMED_X_CONTENT_REGISTRY =
		new NamedXContentRegistry(
			ClusterModule.getNamedXWriteables()
		);
	private static final Set<Character> FORBIDDEN_CHARACTERS = Set.of(
		':', '#', '\\', '/', '*', '?', '"', '<', '>', '|', ' ', ',');

	public static JsonObject getDynamicMapping(byte[] payload)
		throws IOException {

		try (MapperService mapperService = createMapperService()) {

			DocumentMapper documentMapper = mapperService
				.documentMapperWithAutoCreate()
				.getDocumentMapper();

			ParsedDocument doc = documentMapper.parse(sourceToParse(payload));

			Mapping mapping = documentMapper.mapping();
			if (mapping != null) {
				doc.addDynamicMappingsUpdate(mapping);
			}

			return ((JsonObject) Json
				.decodeValue(doc
					.dynamicMappingsUpdate()
					.toString()
				)
			).getJsonObject(DOCUMENT_TYPE);

		}
	}

	public static JsonObject getDynamicMapping(
		IngestionPayload ingestionPayload,
		IngestionPayloadMapper mapper) {

		try {
			var documentTypes = IngestionPayloadMapper.getDocumentTypes(ingestionPayload);

			var dataPayload = mapper.map(ingestionPayload, documentTypes);

			return OpenSearchUtils.getDynamicMapping(Json.encodeToBuffer(dataPayload).getBytes());
		}
		catch (Exception e) {
			throw new IndexMappingException(e);
		}
	}

	/**
	 * Extracts the top-level error reason and the first causedBy reason, if present.
	 *
	 * @param error The initial ErrorCause.
	 * @return A formatted string with the primary error and optionally the first cause.
	 */
	public static String getPrimaryAndFirstCauseReason(ErrorCause error) {
		if (error == null) {
			return "Unknown error (ErrorCause was null)";
		}
		StringBuilder reasonBuilder = new StringBuilder();

		// Append top-level error type and reason
		reasonBuilder.append("(").append(error.type()).append(") ");
		reasonBuilder.append(error.reason() != null ? error.reason() : "No reason provided");

		ErrorCause causedBy = error.causedBy();

		// Check if there's a causedBy
		if (causedBy != null) {
			reasonBuilder.append(" -> Caused by: ");

			// Append the causedBy's type and reason
			reasonBuilder.append("(").append(causedBy.type()).append(") ");
			reasonBuilder.append(
				causedBy.reason() != null ? causedBy.reason() : "No reason provided");
		}

		return reasonBuilder.toString();
	}


	/**
	 * Take a candidate name and removes illegal first characters
	 * then replace all forbidden characters (include uppercase).
	 *
	 * @param name the candidate name
	 * @return the sanitized name that could be used in OpenSearch
	 */
	public static String nameSanitizer(String name) {
		Objects.requireNonNull(name);

		var n = name.length();

		if (n == 0) {
			throw new IllegalArgumentException("name is empty");
		}

		StringBuilder builder = new StringBuilder();

		int i = 0;
		while (name.charAt(i) == '_' || name.charAt(i) == '-') {
			i++;
		}

		for (; i < n; i++) {
			if (FORBIDDEN_CHARACTERS.contains(name.charAt(i))) {
				builder.append("_");
			}
			else {
				builder.append(name.charAt(i));
			}
		}

		return builder.toString().toLowerCase();
	}

	public static WrapperQueryBuilder toWrapperQueryBuilder(Query query) {

		try (var os = new ByteArrayOutputStream()) {

			var generator = jakarta.json.Json.createGenerator(os);

			query.serialize(generator, new JacksonJsonpMapper());

			generator.close();

			return QueryBuilders.wrapperQuery(os.toByteArray());

		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static MapperService createMapperService()
		throws IOException {

		IndexMetadata meta = IndexMetadata.builder("index")
			.settings(Settings.builder()
				.put("index.version.created", Version.CURRENT)
			)
			.numberOfReplicas(0)
			.numberOfShards(1)
			.build();

		MapperRegistry mapperRegistry = new IndicesModule(
			emptyList()
		).getMapperRegistry();

		ScriptModule scriptModule = new ScriptModule(Settings.EMPTY, emptyList());

		ScriptService scriptService = new ScriptService(
			INDEX_SETTING,
			scriptModule.engines,
			scriptModule.contexts
		);

		IndexSettings indexSettings = new IndexSettings(meta, INDEX_SETTING);
		SimilarityService similarityService = new SimilarityService(
			indexSettings,
			scriptService,
			emptyMap()
		);

		IndexAnalyzers indexAnalyzers = new IndexAnalyzers(
			singletonMap(
				"default",
				new NamedAnalyzer("default", AnalyzerScope.INDEX, new StandardAnalyzer())
			),
			emptyMap(),
			emptyMap()
		);

		MapperService mapperService = new MapperService(
			indexSettings,
			indexAnalyzers,
			DEFAULT_NAMED_X_CONTENT_REGISTRY,
			similarityService,
			mapperRegistry,
			() -> {
				throw new UnsupportedOperationException();
			},
			() -> true,
			scriptService
		);

		XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().endObject();

		mapperService.merge(
			"_doc",
			new CompressedXContent(BytesReference.bytes(mapping)),
			MapperService.MergeReason.MAPPING_UPDATE
		);

		return mapperService;
	}

	private static SourceToParse sourceToParse(byte[] payload) {

		return new SourceToParse(
			IGNORE_INDEX,
			DOCUMENT_TYPE,
			new BytesArray(payload),
			XContentType.JSON
		);

	}

	private OpenSearchUtils() {
	}

}
