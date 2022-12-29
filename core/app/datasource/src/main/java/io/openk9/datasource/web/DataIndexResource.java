package io.openk9.datasource.web;

import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.Analyzer_;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.processor.indexwriter.IndexerEvents;
import io.openk9.datasource.sql.TransactionInvoker;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@CircuitBreaker
@Path("/v1/data-index")
public class DataIndexResource {

	@Path("/auto-generate-doc-types")
	@POST
	public Uni<Void> autoGenerateDocTypes(
		AutoGenerateDocTypesRequest request) {

		return sf.withTransaction(session -> {

			CriteriaBuilder cb = sf.getCriteriaBuilder();

			CriteriaQuery<DataIndex> query = cb.createQuery(DataIndex.class);

			Root<Datasource> from = query.from(Datasource.class);

			query.select(from.get(Datasource_.dataIndex));

			query.where(from.get(Datasource_.id).in(request.getDatasourceId()));

			return session
				.createQuery(query)
				.setCacheable(true)
				.getSingleResult()
				.onItem()
				.transformToUni(dataIndex -> indexerEvents.generateDocTypeFields(dataIndex));

		});

	}

	@Path("/get-mappings-from-doc-types")
	@POST
	public Uni<Map<String, Object>> getMappings(
		GetMappingsOrSettingsFromDocTypesRequest request) {

		return getMappingsFromDocTypes(request.getDocTypeIds());

	}

	@Path("/get-settings-from-doc-types")
	@POST
	public Uni<Map<String, Object>> getSettings(
		GetMappingsOrSettingsFromDocTypesRequest request) {

		return getSettingsFromDocTypes(request.getDocTypeIds());

	}

	@Path("/create-data-index-from-doc-types")
	@POST
	public Uni<DataIndex> createDataIndexFromDocTypes(
		CreateDataIndexFromDocTypesRequest request) {

		String indexName;

		if (request.getIndexName() == null) {
			indexName = "data-" + OffsetDateTime.now();
		}
		else {
			indexName = request.getIndexName();
		}

		return sf.withTransaction(s -> {

			List<Long> docTypeIds = request.getDocTypeIds();

			Uni<List<DocType>> docTypeListUni =
				_findDocTypes(docTypeIds, s, true);

			return docTypeListUni
				.onItem()
				.transformToUni(Unchecked.function(docTypeList -> {

					if (docTypeList.size() != docTypeIds.size()) {
						throw new RuntimeException(
							"docTypeIds found: " + docTypeList.size() +
							" docTypeIds requested: " + docTypeIds.size());
					}

					DataIndex dataIndex = new DataIndex();

					dataIndex.setDescription("auto-generated");

					dataIndex.setName(indexName);

					dataIndex.setDocTypes(new LinkedHashSet<>(docTypeList));

					return s.persist(dataIndex)
						.map(__ -> dataIndex)
						.call(s::flush)
						.call((di) -> Uni.createFrom().emitter((sink) -> {

							try {
								IndicesClient indices = client.indices();

								Map<String, Object> mappings =
									_createMappings(di.getDocTypes());

								Settings settings;

								Map<String, Object> settingsMap =
									_createSettings(di.getDocTypes());

								if (settingsMap.isEmpty()) {
									settings = Settings.EMPTY;
								}
								else {
									settings = Settings.builder()
										.loadFromMap(settingsMap)
										.build();
								}

								PutComposableIndexTemplateRequest
									putComposableIndexTemplateRequest =
									new PutComposableIndexTemplateRequest();

								ComposableIndexTemplate composableIndexTemplate =
									new ComposableIndexTemplate(
										List.of(indexName),
										new Template(settings, new CompressedXContent(
											Json.encode(mappings)), null),
										null, null, null, null);

								putComposableIndexTemplateRequest
									.name(indexName + "-template")
									.indexTemplate(composableIndexTemplate);

								indices.putIndexTemplate(putComposableIndexTemplateRequest, RequestOptions.DEFAULT);

								sink.complete(null);
							}
							catch (Exception e) {
								sink.fail(e);
							}

						}));

				}));

		});

	}

	private static Map<String, Object> _createSettings(Collection<DocType> docTypes) {

		List<Analyzer> analyzers =
			docTypes
				.stream()
				.map(DocType::getDocTypeFields)
				.flatMap(Collection::stream)
				.map(DocTypeField::getDocTypeFieldAndChildren)
				.flatMap(Collection::stream)
				.map(DocTypeField::getAnalyzer)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		Map<String, Object> analyzerMap = _createAnalyzer(analyzers);

		Map<String, Object> tokenizerMap = _createTokenizer(analyzers);

		Map<String, Object> filterMap = _createFilter(analyzers);

		Map<String, Object> charFilterMap = _createCharFilter(analyzers);

		Map<String, Object> analysis = new LinkedHashMap<>();

		Map<String, Object> index = new LinkedHashMap<>();

		Map<String, Object> highlight = new LinkedHashMap<>();

		highlight.put("max_analyzed_offset", "10000000");

		index.put("highlight", highlight);

		if (!analyzerMap.isEmpty()) {
			analysis.put("analyzer", analyzerMap);
		}

		if (!tokenizerMap.isEmpty()) {
			analysis.put("tokenizer", tokenizerMap);
		}

		if (!filterMap.isEmpty()) {
			analysis.put("filter", filterMap);
		}

		if (!charFilterMap.isEmpty()) {
			analysis.put("char_filter", charFilterMap);
		}

		if (!analysis.isEmpty() || !index.isEmpty() ) {
			Map<String, Object> settingsMap = new LinkedHashMap<>();

			settingsMap.put("analysis", analysis);

			settingsMap.put("index", index);

			return settingsMap;

		}

		return Map.of();

	}

	private static Map<String, Object> _createCharFilter(List<Analyzer> analyzers) {
		return analyzers
			.stream()
			.map(Analyzer::getCharFilters)
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.filter(tokenFilter -> StringUtils.isNotBlank(tokenFilter.getJsonConfig()))
			.distinct()
			.collect(
				Collectors.toMap(
					CharFilter::getName,
					charFilter -> new JsonObject(charFilter.getJsonConfig()).getMap())
			);
	}

	private static Map<String, Object> _createFilter(List<Analyzer> analyzers) {
		return analyzers
			.stream()
			.map(Analyzer::getTokenFilters)
			.filter(Objects::nonNull)
			.flatMap(Collection::stream)
			.filter(tokenFilter -> StringUtils.isNotBlank(tokenFilter.getJsonConfig()))
			.distinct()
			.collect(
				Collectors.toMap(
					TokenFilter::getName,
					tokenFilter -> new JsonObject(tokenFilter.getJsonConfig()).getMap())
			);
	}

	private static Map<String, Object> _createTokenizer(List<Analyzer> analyzers) {

		return analyzers
			.stream()
			.map(Analyzer::getTokenizer)
			.filter(tokenizer -> tokenizer != null && StringUtils.isNotBlank(tokenizer.getJsonConfig()))
			.distinct()
			.collect(
				Collectors.toMap(
					Tokenizer::getName,
					tokenizer -> new JsonObject(tokenizer.getJsonConfig()).getMap())
			);

	}

	private static Map<String, Object> _createAnalyzer(
		List<Analyzer> analyzers) {

		Map<String, Object> analyzerMap = new LinkedHashMap<>();

		for (Analyzer analyzer : analyzers) {

			Map<String, Object> internalSettings = new LinkedHashMap<>();

			Tokenizer tokenizer = analyzer.getTokenizer();

			if (tokenizer != null) {
				internalSettings.put("tokenizer", tokenizer.getName());
			}

			Set<TokenFilter> tokenFilters = analyzer.getTokenFilters();

			if (tokenFilters != null && !tokenFilters.isEmpty()) {
				internalSettings.put(
					"filter", tokenFilters
						.stream()
						.map(TokenFilter::getName)
						.toList()
				);
			}

			Set<CharFilter> charFilters = analyzer.getCharFilters();

			if (charFilters != null && !charFilters.isEmpty()) {
				internalSettings.put(
					"char_filter", charFilters
						.stream()
						.map(CharFilter::getName)
						.toList()
				);
			}

			String jsonConfig = analyzer.getJsonConfig();

			if (jsonConfig != null) {

				JsonObject jsonObject = new JsonObject(jsonConfig);

				Map<String, Object> map = jsonObject.getMap();

				for (Map.Entry<String, Object> entry : map.entrySet()) {
					if (!internalSettings.containsKey(entry.getKey())) {
						internalSettings.put(entry.getKey(), entry.getValue());
					}
				}

			}

			analyzerMap.put(analyzer.getName(), internalSettings);

		}

		return analyzerMap;
	}

	private static Map<String, Object> _createMappings(
		Collection<DocType> docTypes) {
		return docTypes
			.stream()
			.map(DocType::getDocTypeFields)
			.flatMap(Collection::stream)
			.collect(
				Collectors.collectingAndThen(
					Collectors.toList(),
					DataIndexResource::docTypeFieldsToMappings)
			);
	}

	private Uni<Map<String, Object>> getMappingsFromDocTypes(
		List<Long> docTypeIds) {

		return sf.withTransaction(session -> {

			Uni<List<DocType>> docTypeListUni =
				_findDocTypes(docTypeIds, session, false);

			return docTypeListUni.map(DataIndexResource::_createMappings);

		});
	}

	private Uni<Map<String, Object>> getSettingsFromDocTypes(
		List<Long> docTypeIds) {

		return sf.withTransaction(session -> {

			Uni<List<DocType>> docTypeListUni =
				_findDocTypes(docTypeIds, session, true);

			return docTypeListUni.map(DataIndexResource::_createSettings);

		});
	}

	private Uni<List<DocType>> _findDocTypes(
		List<Long> docTypeIds, Mutiny.Session session,
		boolean includeAnalyzerSubtypes) {

		CriteriaBuilder cb = sf.getCriteriaBuilder();

		CriteriaQuery<DocType> query = cb.createQuery(DocType.class);

		Root<DocType> from = query.from(DocType.class);

		Fetch<DocType, DocTypeField> docTypeFieldFetch = from
			.fetch(DocType_.docTypeFields);

		Fetch<DocTypeField, DocTypeField> subDocTypeFieldFetch =
			docTypeFieldFetch.fetch(DocTypeField_.subDocTypeFields, JoinType.LEFT);

		Fetch<DocTypeField, Analyzer> analyzerFetch =
			docTypeFieldFetch
				.fetch(DocTypeField_.analyzer, JoinType.LEFT);

		Fetch<DocTypeField, Analyzer> subAnalyzerFetch =
			subDocTypeFieldFetch
				.fetch(DocTypeField_.analyzer, JoinType.LEFT);

		if (includeAnalyzerSubtypes) {
			analyzerFetch.fetch(Analyzer_.tokenizer, JoinType.LEFT);
			analyzerFetch.fetch(Analyzer_.tokenFilters, JoinType.LEFT);
			analyzerFetch.fetch(Analyzer_.charFilters, JoinType.LEFT);
			subAnalyzerFetch.fetch(Analyzer_.tokenizer, JoinType.LEFT);
			subAnalyzerFetch.fetch(Analyzer_.tokenFilters, JoinType.LEFT);
			subAnalyzerFetch.fetch(Analyzer_.charFilters, JoinType.LEFT);
		}

		query.where(from.get(DocType_.id).in(docTypeIds));

		query.distinct(true);

		return session
			.createQuery(query)
			.setCacheable(true)
			.getResultList();
	}

	public static Map<String, Object> docTypeFieldsToMappings(
		List<DocTypeField> fieldNames) {

		Map<String, Object> properties = new LinkedHashMap<>();

		Iterator<DocTypeField> orderedFieldNames = fieldNames
			.stream()
			.sorted(Comparator.comparingInt((DocTypeField f) -> f.getFieldName().length()))
			.iterator();

		while (orderedFieldNames.hasNext()) {

			DocTypeField docTypeField = orderedFieldNames.next();

			String[] fieldNamesArray = docTypeField.getFieldName().split("\\.");

			Map<String, Object> current = properties;

			for (int i = 0; i < fieldNamesArray.length; i++) {

				String fieldName = fieldNamesArray[i];

				boolean isLast = i == fieldNamesArray.length - 1;

				current = (Map<String, Object>) current.computeIfAbsent(
					"properties", k -> new LinkedHashMap<>());

				current =
					(Map<String, Object>) current.computeIfAbsent(
						fieldName, k -> new LinkedHashMap<>());

				if (isLast) {

					_populateDocTypeFieldMap(docTypeField, current);

					Set<DocTypeField> subDocTypeFields =
						docTypeField.getSubDocTypeFields();

					if (subDocTypeFields != null) {
						for (DocTypeField subDocTypeField : subDocTypeFields) {

							Map<String, Object> fields =
								(Map<String, Object>) current.computeIfAbsent(
									"fields", k -> new LinkedHashMap<>());

							String subFieldName =
								subDocTypeField.getFieldName().substring(
									docTypeField.getFieldName().length() + 1);

							Map<String, Object> subFieldMap =
								(Map<String, Object>) fields.computeIfAbsent(
									subFieldName, k -> new LinkedHashMap<>());

							_populateDocTypeFieldMap(
								subDocTypeField, subFieldMap);

						}
					}
				}
			}
		}

		return properties;

	}

	private static void _populateDocTypeFieldMap(
		DocTypeField docTypeField, Map<String, Object> docTypeFieldMap) {

		FieldType fieldType = docTypeField.getFieldType();

		docTypeFieldMap.put("type", fieldType.getType());

		Analyzer analyzer = docTypeField.getAnalyzer();

		if (analyzer != null) {
			docTypeFieldMap.put("analyzer", analyzer.getName());
		}

		String fieldConfig = docTypeField.getJsonConfig();

		if (fieldConfig != null) {
			JsonObject fieldConfigJson = new JsonObject(fieldConfig);
			for (Map.Entry<String, Object> entry : fieldConfigJson) {
				docTypeFieldMap.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AutoGenerateDocTypesRequest {
		private long datasourceId;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class GetMappingsOrSettingsFromDocTypesRequest {
		private List<Long> docTypeIds;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class CreateDataIndexFromDocTypesRequest {
		private List<Long> docTypeIds;
		private String indexName;
	}

	@Inject
	TransactionInvoker sf;

	@Inject
	IndexerEvents indexerEvents;

	@Inject
	RestHighLevelClient client;

}
