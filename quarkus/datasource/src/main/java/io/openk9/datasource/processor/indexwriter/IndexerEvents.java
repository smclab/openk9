package io.openk9.datasource.processor.indexwriter;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.processor.util.Field;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class IndexerEvents {

	public void sendEvent(DataIndex dataIndex, List<String> docTypes) {
		eventBus.requestAndForget(
			"createOrUpdateDataIndex",
			new JsonObject()
				.put("dataIndex", JsonObject.mapFrom(dataIndex))
				.put("docTypes", new JsonArray(docTypes))
		);
	}

	@ConsumeEvent("createOrUpdateDataIndex")
	@ActivateRequestContext
	Uni<Void> createOrUpdateDataIndex(JsonObject jsonObject) {

		return sessionFactory
			.withTransaction(session -> Uni.createFrom().deferred(() -> {

				DataIndex dataIndex = jsonObject.getJsonObject("dataIndex").mapTo(DataIndex.class);

				if (dataIndex == null) {
					return Uni.createFrom().failure(
						new IllegalArgumentException("dataIndexId is null"));
				}

				JsonArray docTypes = jsonObject.getJsonArray("docTypes");

				return this._getMappings(dataIndex.getName())
					.map(IndexerEvents::_toFlatFields)
					.map(IndexerEvents::_toDocTypeFields)
					.map(_toDocTypeFieldMap(docTypes))
					.call(_persistDocType(session, dataIndex))
					.replaceWithVoid();

			}));
	}

	private Function<Map<String, List<DocTypeField>>, Uni<?>> _persistDocType(
		Mutiny.Session session, DataIndex dataIndex) {

		return m -> Uni.createFrom().deferred(() -> {

			Set<String> docTypeNames = m.keySet();

			CriteriaBuilder criteriaBuilder =
				sessionFactory.getCriteriaBuilder();

			CriteriaQuery<DocType> docTypeQuery =
				criteriaBuilder.createQuery(DocType.class);

			Root<DocType> from = docTypeQuery.from(DocType.class);

			from.fetch(DocType_.docTypeFields);

			docTypeQuery.where(from.get(DocType_.name).in(docTypeNames));

			Uni<List<DocType>> docTypeListUni = session
				.createQuery(docTypeQuery)
				.setCacheable(true)
				.getResultList();

			return docTypeListUni
				.map(results -> {

					List<DocType> docTypes = new ArrayList<>(docTypeNames.size());

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
							docType.setDocTypeFields(List.of());
						}

						List<DocTypeField> docTypeFieldList =
							m.getOrDefault(docTypeName, List.of());

						for (DocTypeField docTypeField : docTypeFieldList) {
							for (DocTypeField typeField : docType.getDocTypeFields()) {
								if (typeField.getName().equals(docTypeField.getName())) {
									docTypeField.setId(typeField.getId());
									break;
								}
							}
						}

						docType.setDocTypeFields(docTypeFieldList);

						for (DocTypeField docTypeField : docTypeFieldList) {
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

	private Function<List<DocTypeField>, Map<String, List<DocTypeField>>> _toDocTypeFieldMap(
		JsonArray docTypes) {
		return list -> list
			.stream()
			.collect(
				Collectors.groupingBy(
					e ->
						docTypes
							.stream()
							.map(a -> (String)a)
							.filter(dc -> e.getName().startsWith(dc + ".") || e.getName().equals(dc))
							.findFirst()
							.orElse("default"),
					Collectors.toList()
				)
			);
	}

	private Uni<Map<String, Object>> _getMappings(String indexName) {

		return Uni
			.createFrom()
			.item(() -> {
				try {
					return client.indices().getMapping(
						new GetMappingsRequest().indices(indexName),
						RequestOptions.DEFAULT
					);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(response -> response.mappings().get(indexName).sourceAsMap());

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
					newRoot.setType((String)localMap.get("type"));

					if (localMap.containsKey("fields")) {
						Map<String, Object> fields =
							(Map<String, Object>)localMap.get("fields");

						List<Field> subFields = fields
							.entrySet()
							.stream()
							.map(e -> Field.of(
								e.getKey(),
								(String)((Map<String, Object>) e.getValue())
									.get("type")))
							.collect(Collectors.toList());

						newRoot.addSubFields(subFields);

					}

				}

			}

		}

	}

	private static List<DocTypeField> _toDocTypeFields(Field root) {

		List<DocTypeField> docTypeFields = new ArrayList<>();

		_toDocTypeFields(root, new ArrayList<>(), docTypeFields);

		return docTypeFields;

	}

	private static void _toDocTypeFields(
		Field root, List<String> acc, List<DocTypeField> docTypeFields) {

		String name = root.getName();

		if (!root.isRoot()) {
			acc.add(name);
		}

		String type = root.getType();

		if (type != null) {

			String fieldName = String.join(".", acc);

			DocTypeField docTypeField = new DocTypeField();
			docTypeField.setName(fieldName);
			docTypeField.setFieldType(FieldType.fromString(type));
			docTypeField.setBoost(1.0);
			docTypeField.setSearchable(false);
			docTypeField.setDescription("this doc type field is auto generated");
			docTypeFields.add(docTypeField);

		}

		for (Field subField : root.getSubFields()) {
			_toDocTypeFields(
				subField, new ArrayList<>(acc), docTypeFields);
		}
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	EventBus eventBus;

}
