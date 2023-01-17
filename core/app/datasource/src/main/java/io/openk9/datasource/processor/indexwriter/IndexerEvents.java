package io.openk9.datasource.processor.indexwriter;

import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.processor.util.Field;
import io.openk9.datasource.sql.TransactionInvoker;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class IndexerEvents {

	public void requestAndForget(DataIndex dataIndex, List<String> docTypes) {
		eventBus.requestAndForget(
			"createOrUpdateDataIndex",
			new JsonObject()
				.put("dataIndex", JsonObject.mapFrom(dataIndex))
				.put("docTypes", new JsonArray(docTypes))
		);
	}

	public Uni<Void> generateDocTypeFields(DataIndex dataIndex) {

		if (dataIndex == null) {
			return Uni.createFrom().failure(
				new IllegalArgumentException("dataIndexId is null"));
		}

		return indexService.getMappings(dataIndex.getName())
			.map(IndexerEvents::_toFlatFields)
			.map(IndexerEvents::_toDocTypeFields)
			.plug(docTypeFields -> Uni
				.combine()
				.all()
				.unis(docTypeFields, _getDocumentTypes(dataIndex.getName())).asTuple())
			.map(_toDocTypeFieldMap())
			.call(_persistDocType(dataIndex))
			.replaceWithVoid();
	}

	@ConsumeEvent("createOrUpdateDataIndex")
	@ActivateRequestContext
	Uni<Void> createOrUpdateDataIndex(JsonObject jsonObject) {

		return Uni.createFrom().deferred(() -> {

			DataIndex dataIndex = jsonObject.getJsonObject("dataIndex").mapTo(DataIndex.class);

			return generateDocTypeFields(dataIndex);

		});
	}

	private Function<Map<String, List<DocTypeField>>, Uni<?>> _persistDocType(
		DataIndex dataIndex) {

		return m -> sessionFactory.withTransaction(session -> {

			Set<String> docTypeNames = m.keySet();

			CriteriaBuilder criteriaBuilder =
				sessionFactory.getCriteriaBuilder();

			CriteriaQuery<DocType> docTypeQuery =
				criteriaBuilder.createQuery(DocType.class);

			Root<DocType> from = docTypeQuery.from(DocType.class);

			Fetch<DocType, DocTypeField> docTypeFieldFetch = from
				.fetch(DocType_.docTypeFields);

			docTypeFieldFetch
				.fetch(DocTypeField_.subDocTypeFields, JoinType.LEFT);

			docTypeFieldFetch
				.fetch(DocTypeField_.parentDocTypeField, JoinType.LEFT);

			docTypeQuery.where(from.get(DocType_.name).in(docTypeNames));

			Uni<List<DocType>> docTypeListUni = session
				.createQuery(docTypeQuery)
				.setCacheable(true)
				.getResultList();

			return docTypeListUni
				.map(results -> {

					Set<DocType> docTypes = new LinkedHashSet<>(docTypeNames.size());

					for (String docTypeName : docTypeNames) {

						Optional<DocType> first =
							results
								.stream()
								.filter(docType -> docType.getName().equals(
									docTypeName))
								.findFirst();

						DocType docType;

						if (first.isPresent()) {
							docType = first.get();
						}
						else {
							docType = new DocType();
							docType.setName(docTypeName);
							docType.setDescription("auto-generated");
							docType.setDocTypeFields(new LinkedHashSet<>());
						}

						List<DocTypeField> docTypeFieldList =
							m.getOrDefault(docTypeName, List.of());

						for (DocTypeField docTypeField : docTypeFieldList) {
							for (DocTypeField typeField : docType.getDocTypeFields()) {

								if (typeField.getFieldName().equals(docTypeField.getFieldName())) {
									docTypeField.setId(typeField.getId());
									break;
								}

								DocTypeField parentDocTypeField =
									typeField.getParentDocTypeField();

								if (parentDocTypeField != null) {
									if (parentDocTypeField.getFieldName().equals(
										docTypeField.getFieldName())) {
										docTypeField.setId(
											parentDocTypeField.getId());
										break;
									}
								}

								Set<DocTypeField> subDocTypeFields =
									typeField.getSubDocTypeFields();

								if (subDocTypeFields != null) {
									Optional<DocTypeField> subDocTypeField =
										subDocTypeFields
											.stream()
											.filter(
												subTypeField -> subTypeField
													.getFieldName()
													.equals(docTypeField.getFieldName()))
											.findFirst();

									if (subDocTypeField.isPresent()) {
										docTypeField.setId(
											subDocTypeField.get().getId());
										break;
									}
								}

							}
						}

						Set<DocTypeField> docTypeFields =
							docType.getDocTypeFields();

						docTypeFields.addAll(docTypeFieldList);

						for (DocTypeField docTypeField : docTypeFields) {
							docTypeField.setDocType(docType);
						}

						docTypes.add(docType);

					}

					return docTypes;

				})
				.flatMap(docTypes -> {
					dataIndex.setDocTypes(docTypes);
					return session.merge(dataIndex)
						.call(session::flush);
				});
		});
	}

	private Function<Tuple2<List<DocTypeField>, List<String>>, Map<String, List<DocTypeField>>> _toDocTypeFieldMap() {
		return t2 -> {

			List<DocTypeField> list = t2.getItem1();

			List<String> documentTypes = t2.getItem2();

			return list
				.stream()
				.collect(
					Collectors.groupingBy(
						e ->
							documentTypes
								.stream()
								.filter(dc -> e.getFieldName().startsWith(dc + ".") || e.getFieldName().equals(dc))
								.findFirst()
								.orElse("default"),
						Collectors.toList()
					)
				);
		};
	}

	public Uni<List<String>> _getDocumentTypes(String indexName) {
		return Uni
			.createFrom()
			.item(() -> {

				SearchRequest searchRequest = new SearchRequest(indexName);

				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

				searchSourceBuilder.size(0);

				searchSourceBuilder.aggregation(
					AggregationBuilders
						.terms("documentTypes")
						.field("documentTypes.keyword")
						.size(1000));

				searchRequest.source(searchSourceBuilder);

				try {
					SearchResponse search = client.search(
						searchRequest, RequestOptions.DEFAULT
					);

					return search.getAggregations()
						.<Terms>get("documentTypes")
						.getBuckets()
						.stream()
						.map(MultiBucketsAggregation.Bucket::getKeyAsString)
						.collect(Collectors.toList());
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			});

	}

	private static Field _toFlatFields(Map<String, Object> mappings) {
		Field root = Field.createRoot();
		_toFlatFields(mappings, root);
		return root;
	}

	private static void _toFlatFields(
		Map<String, Object> mappings, Field root) {

		for (Map.Entry<String, Object> kv : mappings.entrySet()) {

			String key = kv.getKey();
			Object value = kv.getValue();

			if (key.equals("properties")) {
				_toFlatFields((Map<String, Object>) value, root);
			}
			else if (value instanceof Map && ((Map)value).size() == 1) {
				Map<String, Object> map = (Map<String, Object>)value;
				if (map.containsKey("type")) {
					root.addSubField(Field.of(key, (String)map.get("type")));
				}
				else {
					Field newRoot = Field.of(key);
					root.addSubField(newRoot);
					_toFlatFields((Map<String, Object>) value, newRoot);
				}
			}
			else if (value instanceof Map && ((Map)value).size() > 1) {
				Map<String, Object> localMap = ((Map<String, Object>)value);

				Field newRoot = Field.of(key);

				root.addSubField(newRoot);

				if (localMap.containsKey("type")) {
					_populateField(newRoot, localMap);
				}

			}

		}

	}

	private static Field _populateField(Field field, Map<String, Object> props) {

		if (props == null) {
			return field;
		}

		Map<String, Object> extra = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : props.entrySet()) {
			String entryKey = entry.getKey();
			Object entryValue = entry.getValue();
			switch (entryKey) {
				case "type" -> field.setType((String) entryValue);
				case "fields" -> {
					Map<String, Object> fields =
						(Map<String, Object>) entryValue;

					List<Field> subFields = fields
						.entrySet()
						.stream()
						.map(e -> _populateField(
							Field.of(e.getKey()),
							(Map<String, Object>)e.getValue())
						)
						.collect(Collectors.toList());

					field.addSubFields(subFields);
				}
				default -> extra.put(entryKey, entryValue);
			}
		}
		if (!extra.isEmpty()) {
			field.setExtra(extra);
		}

		return field;

	}

	private static List<DocTypeField> _toDocTypeFields(Field root) {

		List<DocTypeField> docTypeFields = new ArrayList<>();

		_toDocTypeFields(root, new ArrayList<>(), null, docTypeFields);

		return docTypeFields;

	}

	private static void _toDocTypeFields(
		Field root, List<String> acc, DocTypeField parent,
		Collection<DocTypeField> docTypeFields) {

		String name = root.getName();

		if (!root.isRoot()) {
			acc.add(name);
		}

		String type = root.getType();

		if (type != null) {

			String fieldName = String.join(".", acc);

			DocTypeField docTypeField = new DocTypeField();
			docTypeField.setName(fieldName);
			docTypeField.setFieldName(fieldName);
			docTypeField.setBoost(1.0);
			FieldType fieldType = FieldType.fromString(type);
			docTypeField.setFieldType(fieldType);
			switch (fieldType) {
				case TEXT, KEYWORD, WILDCARD, CONSTANT_KEYWORD -> docTypeField.setSearchable(true);
				default -> docTypeField.setSearchable(false);
			}
			docTypeField.setDescription("auto-generated");
			docTypeField.setSubDocTypeFields(new LinkedHashSet<>());
			if (root.getExtra() != null && !root.getExtra().isEmpty()) {
				docTypeField.setJsonConfig(
					new JsonObject(root.getExtra()).toString());
			}

			if (parent != null) {
				docTypeField.setParentDocTypeField(parent);
			}

			docTypeFields.add(docTypeField);

			for (Field subField : root.getSubFields()) {
				_toDocTypeFields(
					subField, new ArrayList<>(acc), docTypeField,
					docTypeField.getSubDocTypeFields());
			}

		}
		else {
			for (Field subField : root.getSubFields()) {
				_toDocTypeFields(
					subField, new ArrayList<>(acc), null, docTypeFields);
			}
		}

	}

	@Inject
	RestHighLevelClient client;

	@Inject
	TransactionInvoker sessionFactory;

	@Inject
	EventBus eventBus;

	@Inject
	IndexService indexService;

}
