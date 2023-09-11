package io.openk9.datasource.processor.indexwriter;

import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.processor.util.Field;
import io.openk9.datasource.service.DocTypeService;
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
			.map(IndexerEvents::toDocTypeFields)
			.plug(docTypeFields -> Uni
				.combine()
				.all()
				.unis(docTypeFields, _getDocumentTypes(dataIndex.getName()))
				.asTuple()
			)
			.map(IndexerEvents::toDocTypeAndFieldsGroup)
			.call(_persistDocType(dataIndex))
			.replaceWithVoid();
	}

	@ConsumeEvent("createOrUpdateDataIndex")
	@ActivateRequestContext
	public Uni<Void> createOrUpdateDataIndex(JsonObject jsonObject) {

		return Uni.createFrom().deferred(() -> {

			DataIndex dataIndex = jsonObject.getJsonObject("dataIndex").mapTo(DataIndex.class);

			return generateDocTypeFields(dataIndex);

		});
	}

	protected static List<DocTypeField> toDocTypeFields(Map<String, Object> mappings) {
		return _toDocTypeFields(_toFlatFields(mappings));
	}

	protected static Map<String, List<DocTypeField>> toDocTypeAndFieldsGroup(
		Tuple2<List<DocTypeField>, List<String>> t2) {

		List<DocTypeField> docTypeFields = t2.getItem1();

		List<String> documentTypes = t2.getItem2();

		Map<String, List<DocTypeField>> grouped = docTypeFields
			.stream()
			.collect(
				Collectors.groupingBy(
					e ->
						documentTypes
							.stream()
							.filter(dt -> e.getFieldName().startsWith(dt + ".")
								|| e.getFieldName().equals(dt))
							.findFirst()
							.orElse("default"),
					Collectors.toList()
				)
			);

		_explodeDocTypeFirstLevel(grouped);

		return grouped;
	}

	protected static Set<DocType> mergeDocTypes(
		Map<String, List<DocTypeField>> mappedDocTypeAndFields,
		Collection<DocType> existingDocTypes) {

		Set<String> mappedDocTypeNames = mappedDocTypeAndFields.keySet();

		Set<DocType> docTypes = new LinkedHashSet<>(mappedDocTypeNames.size());

		for (String docTypeName : mappedDocTypeNames) {

			DocType docType =
				existingDocTypes
					.stream()
					.filter(d -> d.getName().equals(docTypeName))
					.findFirst()
					.orElseGet(() -> {
						DocType newDocType = new DocType();
						newDocType.setName(docTypeName);
						newDocType.setDescription("auto-generated");
						newDocType.setDocTypeFields(new LinkedHashSet<>());
						return newDocType;
					});

			List<DocTypeField> mappedDocTypeFields =
				mappedDocTypeAndFields.getOrDefault(docTypeName, List.of());

			Set<DocTypeField> docTypeFields = docType.getDocTypeFields();

			for (DocTypeField docTypeField : mappedDocTypeFields) {
				for (DocTypeField existingField : docTypeFields) {

					if ((docType.getName() + "." + DocTypeField.fieldPath(docTypeField))
						.equals(DocTypeField.fieldPath(existingField))) {

						docTypeField.setId(existingField.getId());
						break;
					}
				}
			}

			docTypeFields.addAll(mappedDocTypeFields);

			_setDocTypeToDocTypeFields(docType, docTypeFields);

			docTypes.add(docType);
		}

		return docTypes;

	}

	private static void _explodeDocTypeFirstLevel(Map<String, List<DocTypeField>> grouped) {
		for (String docTypeName : grouped.keySet()) {
			if (!docTypeName.equals("default")) {
				List<DocTypeField> groupedDocTypeFields = grouped.get(docTypeName);
				Optional<DocTypeField> optRoot = groupedDocTypeFields
					.stream()
					.filter(docTypeField -> docTypeField
						.getFieldName().equals(docTypeName)
					)
					.findFirst();
				if (optRoot.isPresent()) {
					DocTypeField root = optRoot.get();
					Set<DocTypeField> subFields = root.getSubDocTypeFields();
					if (subFields != null && !subFields.isEmpty()) {
						groupedDocTypeFields.remove(root);
						for (DocTypeField subField : subFields) {
							subField.setParentDocTypeField(null);
						}
						groupedDocTypeFields.addAll(subFields);
					}
				}
			}
		}
	}

	private Function<Map<String, List<DocTypeField>>, Uni<?>> _persistDocType(
		DataIndex dataIndex) {

		return m -> sessionFactory.withTransaction(session -> {

			Set<String> docTypeNames = m.keySet();

			return docTypeService.getDocTypesAndDocTypeFieldsByNames(docTypeNames)
				.map(docTypes -> mergeDocTypes(m, docTypes))
				.flatMap(docTypes -> {
					dataIndex.setDocTypes(docTypes);
					return session.merge(dataIndex)
						.call(session::flush);
				});
		});
	}

	private static void _setDocTypeToDocTypeFields(
		DocType docType, Set<DocTypeField> docTypeFields) {

		if (docTypeFields == null) {
			return;
		}

		for (DocTypeField docTypeField : docTypeFields) {
			docTypeField.setDocType(docType);
			_setDocTypeToDocTypeFields(docType, docTypeField.getSubDocTypeFields());
		}

	}

	private Uni<List<String>> _getDocumentTypes(String indexName) {
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

		for (Field subField : root.getSubFields()) {
			if (!subField.isRoot()) {
				_toDocTypeFields(subField, new ArrayList<>(), null, docTypeFields);
			}
		}

		return docTypeFields;

	}

	private static void _toDocTypeFields(
		Field field, List<String> acc, DocTypeField parent,
		Collection<DocTypeField> docTypeFields) {

		String name = field.getName();
		acc.add(name);

		String type = field.getType();

		boolean isI18NField =
			field
				.getSubFields()
				.stream()
				.map(Field::getName)
				.anyMatch(fieldName -> fieldName.equals("i18n"));

		String fieldName = String.join(".", acc);

		DocTypeField docTypeField = new DocTypeField();
		docTypeField.setName(fieldName);
		docTypeField.setFieldName(name);
		docTypeField.setBoost(type != null ? 1.0 : null);
		FieldType fieldType = isI18NField
			? FieldType.I18N
			: type != null
			? FieldType.fromString(type)
			: FieldType.OBJECT;
		docTypeField.setFieldType(fieldType);
		docTypeField.setDescription("auto-generated");
		docTypeField.setSubDocTypeFields(new LinkedHashSet<>());
		if (field.getExtra() != null && !field.getExtra().isEmpty()) {
			docTypeField.setJsonConfig(
				new JsonObject(field.getExtra()).toString());
		}

		if (parent != null) {
			docTypeField.setParentDocTypeField(parent);
		}

		docTypeFields.add(docTypeField);

		switch (fieldType) {
			case TEXT, KEYWORD, WILDCARD, CONSTANT_KEYWORD, I18N -> docTypeField.setSearchable(true);
			default -> docTypeField.setSearchable(false);
		}

		for (Field subField : field.getSubFields()) {
			_toDocTypeFields(
				subField, new ArrayList<>(acc), docTypeField,
				docTypeField.getSubDocTypeFields());

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

	@Inject
	DocTypeService docTypeService;

}
