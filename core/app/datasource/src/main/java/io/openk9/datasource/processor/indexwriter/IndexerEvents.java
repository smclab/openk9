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

package io.openk9.datasource.processor.indexwriter;

import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.cache.P2PCache;
import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.util.DocTypeFieldUtils;
import io.openk9.datasource.processor.util.Field;
import io.openk9.datasource.service.DocTypeService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class IndexerEvents {

	@Inject
	RestHighLevelClient client;
	@Inject
	IndexService indexService;
	@Inject
	DocTypeService docTypeService;
	@Inject
	ActorSystemProvider actorSystemProvider;

	public Uni<Void> generateDocTypeFields(Mutiny.Session session, DataIndex dataIndex) {

		if (dataIndex == null) {
			return Uni.createFrom().failure(new IllegalArgumentException("dataIndexId is null"));
		}

		return indexService
			.getMappings(dataIndex.getIndexName())
			.map(IndexerEvents::toDocTypeFields)
			.plug(docTypeFieldsUni -> docTypeFieldsUni.flatMap(
				docTypeFields -> _getDocumentTypes(dataIndex.getIndexName())
					.map(docTypes -> Tuple2.of(docTypeFields, docTypes))
			))
			.map(IndexerEvents::toDocTypeAndFieldsGroup)
			.call(map -> _persistDocType(map, dataIndex, session))
			.replaceWithVoid();
	}

	public Uni<Void> generateDocTypeFields(
		Mutiny.Session session,
		DataIndex dataIndex,
		Map<String, Object> mappings,
		List<String> documentTypes) {

		var docTypeFields = IndexerEvents.toDocTypeFields(mappings);

		var docTypeAndFieldsGroup = IndexerEvents.toDocTypeAndFieldsGroup(Tuple2.of(
			docTypeFields,
			documentTypes
		));

		return _persistDocType(docTypeAndFieldsGroup, dataIndex, session);
	}
	protected static List<DocTypeField> toDocTypeFields(Map<String, Object> mappings) {
		return _toDocTypeFields(_toFlatFields(mappings));
	}

	protected static Map<String, List<DocTypeField>> toDocTypeAndFieldsGroup(
		Tuple2<List<DocTypeField>, List<String>> t2) {

		List<DocTypeField> docTypeFields = t2.getItem1();

		List<String> documentTypes = t2.getItem2();

		Map<String, List<DocTypeField>> grouped = docTypeFields.stream()
			.collect(Collectors.groupingBy(
				field -> documentTypes.stream()
					.filter(dt ->
						field.getFieldName().startsWith(dt + ".")
						|| field.getFieldName().equals(dt)
					)
					.findFirst()
					.orElse("default"),
				Collectors.toList()
			));

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

			List<DocTypeField> generatedFields =
				mappedDocTypeAndFields.getOrDefault(docTypeName, List.of());

			Set<DocTypeField> persistedFields = docType.getDocTypeFields();

			List<DocTypeField> retainedFields = new ArrayList<>();

			for (DocTypeField docTypeField : generatedFields) {
				boolean retained = true;
				for (DocTypeField existingField : persistedFields) {

					if (Objects.equals(
						existingField.getPath(),
						DocTypeFieldUtils.fieldPath(docTypeName, docTypeField)
					)) {
						retained = false;
						break;
					}
				}
				if (retained) {
					retainedFields.add(docTypeField);
				}
			}

			persistedFields.addAll(retainedFields);

			_setDocTypeToDocTypeFields(docType, persistedFields);

			docTypes.add(docType);
		}

		return docTypes;

	}

	private static void _explodeDocTypeFirstLevel(Map<String, List<DocTypeField>> grouped) {
		for (String docTypeName : grouped.keySet()) {
			if (!docTypeName.equals("default")) {
				List<DocTypeField> groupedDocTypeFields = grouped.get(docTypeName);
				groupedDocTypeFields
					.stream()
					.filter(docTypeField -> Objects.equals(
						docTypeField.getFieldName(),
						docTypeName
					))
					.findFirst()
					.ifPresent(root -> {
						Set<DocTypeField> subFields = root.getSubDocTypeFields();
						if (subFields != null && !subFields.isEmpty()) {
							groupedDocTypeFields.remove(root);
							for (DocTypeField subField : subFields) {
								subField.setParentDocTypeField(null);
							}
							groupedDocTypeFields.addAll(subFields);
						}
					});
			}
		}
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
			else if (value instanceof Map && ((Map) value).size() == 1) {
				Map<String, Object> map = (Map<String, Object>) value;
				if (map.containsKey("type")) {
					root.addSubField(Field.of(key, (String) map.get("type")));
				}
				else {
					Field newRoot = Field.of(key);
					root.addSubField(newRoot);
					_toFlatFields((Map<String, Object>) value, newRoot);
				}
			}
			else if (value instanceof Map && ((Map) value).size() > 1) {
				Map<String, Object> localMap = ((Map<String, Object>) value);

				Field newRoot = Field.of(key);

				root.addSubField(newRoot);

				if (localMap.containsKey("type")) {
					_populateField(newRoot, localMap);
				}

			}

		}

	}

	private static Field _populateField(
		Field field, Map<String, Object> props) {

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
					Map<String, Object> fields = (Map<String, Object>) entryValue;

					List<Field> subFields = fields.entrySet().stream()
						.map(e -> _populateField(
							Field.of(e.getKey()),
							(Map<String, Object>) e.getValue()
						)).collect(Collectors.toList());

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
		Field field,
		List<String> acc,
		DocTypeField parent,
		Collection<DocTypeField> docTypeFields) {

		String name = field.getName();
		acc.add(name);

		String type = field.getType();

		boolean isI18NField = field
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
			docTypeField.setJsonConfig(new JsonObject(field.getExtra()).toString());
		}

		if (parent != null) {
			docTypeField.setParentDocTypeField(parent);
		}

		docTypeFields.add(docTypeField);

		switch (fieldType) {
			case TEXT, KEYWORD, WILDCARD, CONSTANT_KEYWORD, I18N ->
				docTypeField.setSearchable(true);
			default -> docTypeField.setSearchable(false);
		}

		for (Field subField : field.getSubFields()) {
			_toDocTypeFields(
				subField,
				new ArrayList<>(acc),
				docTypeField,
				docTypeField.getSubDocTypeFields()
			);

		}

	}

	private Uni<List<String>> _getDocumentTypes(String indexName) {
		return Uni.createFrom().item(() -> {

			SearchRequest searchRequest = new SearchRequest(indexName);

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

			searchSourceBuilder.size(0);

			searchSourceBuilder.aggregation(AggregationBuilders
				.terms("documentTypes")
				.field("documentTypes.keyword")
				.size(1000)
			);

			searchRequest.source(searchSourceBuilder);

			try {
				SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);

				return search
					.getAggregations()
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

	private Uni<Void> _persistDocType(
		Map<String, List<DocTypeField>> docTypesGroup,
		DataIndex dataIndex,
		Mutiny.Session session) {

		Set<String> docTypeNames = docTypesGroup.keySet();

		return docTypeService
			.getDocTypesAndDocTypeFieldsByNames(session, docTypeNames)
			.map(docTypes -> mergeDocTypes(docTypesGroup, docTypes))
			.flatMap(docTypes -> session
				.merge(dataIndex)
				.flatMap(merged -> {
					merged.setDocTypes(docTypes);
					return session.persist(merged);
				})
			)
			.invoke(() -> P2PCache.askInvalidation(actorSystemProvider.getActorSystem()));
	}

}
