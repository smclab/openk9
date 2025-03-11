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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.processor.payload.IngestionPayload;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.opensearch.Version;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.cluster.ClusterModule;
import org.opensearch.cluster.metadata.IndexMetadata;
import org.opensearch.common.CheckedConsumer;
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
import org.opensearch.plugins.MapperPlugin;
import org.opensearch.plugins.Plugin;
import org.opensearch.plugins.ScriptPlugin;
import org.opensearch.script.ScriptModule;
import org.opensearch.script.ScriptService;

public class OpenSearchUtils {

	private static final String IGNORE_INDEX = "IGNORE_INDEX";
	private static final String DOCUMENT_TYPE = MapperService.SINGLE_MAPPING_NAME;

	private OpenSearchUtils() {
	}

	public static JsonObject getDynamicMapping(
		IngestionPayload ingestionPayload,
		IngestionPayloadMapper mapper) {

		var documentTypes = IngestionPayloadMapper.getDocumentTypes(ingestionPayload);

		var dataPayload = mapper.map(ingestionPayload, documentTypes);

		return OpenSearchUtils.getDynamicMapping(Json.encodeToBuffer(dataPayload).getBytes());
	}


	protected static final Settings SETTINGS = Settings.builder().put(
		"index.version.created",
		Version.CURRENT
	).build();
	private static final NamedXContentRegistry DEFAULT_NAMED_X_CONTENT_REGISTRY =
		new NamedXContentRegistry(
			ClusterModule.getNamedXWriteables()
		);

	public static JsonObject getDynamicMapping(byte[] payload) {

		DocumentMapper documentMapper = null;
		try {
			documentMapper = createMapperService(Version.CURRENT, dynamicMapping(b -> {}))
				.documentMapperWithAutoCreate()
				.getDocumentMapper();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

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

	/**
	 * Take a candidate indexName and removes illegal first characters
	 * then replace all forbidden characters (include uppercase).
	 *
	 * @param name the candidate name
	 * @return the sanitized name
	 */
	public static String indexNameSanitizer(String name) {
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

	private static final Set<Character> FORBIDDEN_CHARACTERS = Set.of(
		':', '#', '\\', '/', '*', '?', '"', '<', '>', '|', ' ', ',');

	private static SourceToParse sourceToParse(byte[] payload) {

		return new SourceToParse(
			IGNORE_INDEX,
			DOCUMENT_TYPE,
			new BytesArray(payload),
			XContentType.JSON
		);

	}

	protected static IndexAnalyzers createIndexAnalyzers(IndexSettings indexSettings) {
		return new IndexAnalyzers(
			singletonMap(
				"default",
				new NamedAnalyzer("default", AnalyzerScope.INDEX, new StandardAnalyzer())
			),
			emptyMap(),
			emptyMap()
		);
	}

	protected static MapperService createMapperService(Version version, XContentBuilder mapping)
		throws IOException {
		IndexMetadata meta = IndexMetadata.builder("index")
			.settings(Settings.builder().put("index.version.created", version))
			.numberOfReplicas(0)
			.numberOfShards(1)
			.build();
		IndexSettings indexSettings = new IndexSettings(meta, getIndexSettings());
		MapperRegistry mapperRegistry = new IndicesModule(
			getPlugins().stream()
				.filter(p -> p instanceof MapperPlugin)
				.map(p -> (MapperPlugin) p)
				.collect(toList())
		).getMapperRegistry();
		ScriptModule scriptModule = new ScriptModule(
			Settings.EMPTY,
			getPlugins().stream()
				.filter(p -> p instanceof ScriptPlugin)
				.map(p -> (ScriptPlugin) p)
				.collect(toList())
		);
		ScriptService scriptService = new ScriptService(
			getIndexSettings(),
			scriptModule.engines,
			scriptModule.contexts
		);
		SimilarityService similarityService = new SimilarityService(
			indexSettings,
			scriptService,
			emptyMap()
		);
		MapperService mapperService = new MapperService(
			indexSettings,
			createIndexAnalyzers(indexSettings),
			xContentRegistry(),
			similarityService,
			mapperRegistry,
			() -> {
				throw new UnsupportedOperationException();
			},
			() -> true,
			scriptService
		);
		merge(mapperService, mapping);
		return mapperService;
	}

	protected static Settings getIndexSettings() {
		return SETTINGS;
	}

	protected static Collection<? extends Plugin> getPlugins() {
		return emptyList();
	}

	protected static void merge(MapperService mapperService, XContentBuilder mapping)
		throws IOException {
		merge(mapperService, MapperService.MergeReason.MAPPING_UPDATE, mapping);
	}

	protected static void merge(
		MapperService mapperService,
		MapperService.MergeReason reason,
		XContentBuilder mapping) throws IOException {
		mapperService.merge("_doc", new CompressedXContent(BytesReference.bytes(mapping)), reason);
	}

	protected static NamedXContentRegistry xContentRegistry() {
		return DEFAULT_NAMED_X_CONTENT_REGISTRY;
	}

	private static XContentBuilder dynamicMapping(CheckedConsumer<XContentBuilder, IOException> buildFields)
		throws IOException {
		return topMapping(b -> {
			b.field("dynamic", true);
			b.startObject("properties");
			buildFields.accept(b);
			b.endObject();
		});
	}

	private static XContentBuilder topMapping(CheckedConsumer<XContentBuilder, IOException> buildFields)
		throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject("_doc");
		buildFields.accept(builder);
		return builder.endObject().endObject();
	}
}
