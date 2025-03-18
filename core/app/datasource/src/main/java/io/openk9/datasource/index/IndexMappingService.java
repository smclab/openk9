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

package io.openk9.datasource.index;

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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import io.openk9.datasource.index.mappings.IndexMappingsUtil;
import io.openk9.datasource.index.mappings.MappingsKey;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.UnknownTenantException;
import io.openk9.datasource.model.util.DocTypeFieldUtils;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.processor.util.Field;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.util.OpenSearchUtils;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.opensearch.client.indices.PutComposableIndexTemplateRequest;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch.cluster.PutComponentTemplateRequest;
import org.opensearch.cluster.metadata.ComposableIndexTemplate;
import org.opensearch.cluster.metadata.Template;
import org.opensearch.common.compress.CompressedXContent;
import org.opensearch.common.settings.Settings;

/**
 * Service responsible for handling the creation and management of index mappings in OpenSearch.
 * This class provides functionality for creating index templates, component templates, and
 * generating DocType fields from various sources like plugin drivers or existing indices.
 */
@ApplicationScoped
public class IndexMappingService {

	private final static Logger log =
		Logger.getLogger(IndexMappingService.class);

	@Inject
	IndexService indexService;
	@Inject
	DocTypeService docTypeService;
	@Inject
	HttpPluginDriverClient httpPluginDriverClient;
	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;

	/**
	 * Create an IndexTemplate from a dataIndex and a settings map.
	 * The indexTemplate mappings will be created from the docTypes
	 * associated to dataIndex.
	 * The indexTemplate settings will be created from the settings map.
	 *
	 * @param dataIndexTemplate The object containing the dataIndex and the settings map
	 *                         	used to create the dataIndex
	 * @return A {@link Uni<Void>} representing the asynchronous operation. If the operation
	 * succeeds, the result is empty. If the operation fails, the failure is
	 * propagated through the {@link Uni} pipeline, allowing the caller to handle
	 * the error appropriately.
	 */
	public Uni<Void> createDataIndexTemplate(
		DataIndexTemplate dataIndexTemplate) {

		return indexService.createIndexTemplate(
			createIndexTemplateRequest(dataIndexTemplate));
	}

	/**
	 * Create or update a component template that defines an embedding mapping.
	 *
	 * @param embeddingComponentTemplate The object containing the name of the component
	 *                                   template and the dimension of the {@code knnVector}.
	 *                                   Must not be {@code null}.
	 * @return A {@link Uni<Void>} representing the asynchronous operation. If the operation
	 * succeeds, the result is empty. If the operation fails, the failure is
	 * propagated through the {@link Uni} pipeline, allowing the caller to handle
	 * the error appropriately.
	 **/
	public Uni<Void> createEmbeddingComponentTemplate(
		EmbeddingComponentTemplate embeddingComponentTemplate) {

		if (log.isDebugEnabled()) {
			log.debugf(
				"Creating a componentTemplate named %s, the vector size is %d.",
				embeddingComponentTemplate.getName(),
				embeddingComponentTemplate.vectorSize()
			);
		}

		return indexService.putComponentTemplate(
			createComponentTemplateRequest(embeddingComponentTemplate));
	}

	/**
	 * Generates DocType and fields from provided mappings and document types.
	 *
	 * @param session       The Hibernate reactive session used for database operations
	 * @param mappings      A map containing the field mappings structure
	 * @param documentTypes List of document type names to be processed
	 * @return A {@link Uni} containing a Set of generated or updated DocType objects
	 */
	public Uni<Set<DocType>> generateDocTypeFields(
		Mutiny.Session session,
		Map<String, Object> mappings,
		List<String> documentTypes) {

		var docTypeFields = IndexMappingService.toDocTypeFields(mappings);

		var docTypeAndFieldsGroup = IndexMappingService
			.toDocTypeAndFieldsGroup(docTypeFields, documentTypes);

		return _refreshDocTypeSet(session, docTypeAndFieldsGroup);
	}

	/**
	 * Generates DocType fields from an existing index by retrieving its mappings and document types.
	 *
	 * @param session The Hibernate reactive session used for database operations
	 * @param indexName The name of the index to retrieve mappings from
	 * @return A {@link Uni} containing a Set of generated or updated DocType objects
	 */
	public Uni<Set<DocType>> generateDocTypeFieldsFromIndexName(
		Mutiny.Session session, String indexName) {

		return indexService.getMappings(indexName)
			.flatMap(mappings -> indexService
				.getDocumentTypes(indexName)
				.flatMap(documentTypes -> generateDocTypeFields(
					session, mappings, documentTypes)));
	}

	/**
	 * Generates DocType fields from a plugin driver sample data.
	 * This method retrieves a sample from the plugin driver, extracts document types
	 * and mappings, and then generates the corresponding DocType fields.
	 *
	 * @param session The Hibernate reactive session used for database operations
	 * @param httpPluginDriverInfo Information about the plugin driver to retrieve sample from
	 * @return A {@link Uni} containing a Set of generated or updated DocType objects
	 */
	public Uni<Set<DocType>> generateDocTypeFieldsFromPluginDriverSample(
		Mutiny.Session session, HttpPluginDriverInfo httpPluginDriverInfo) {

		return httpPluginDriverClient.getSample(httpPluginDriverInfo)
			.flatMap(ingestionPayload -> {

				var documentTypes =
					IngestionPayloadMapper.getDocumentTypes(ingestionPayload);

				var mappings = OpenSearchUtils.getDynamicMapping(
					ingestionPayload,
					ingestionPayloadMapper
				);

				return generateDocTypeFields(
					session, mappings.getMap(), documentTypes);
			})
			.flatMap(docTypes ->
				session.mergeAll(docTypes.toArray())
					.map(unused -> docTypes)
			);

	}

	protected static PutComponentTemplateRequest createComponentTemplateRequest(
		EmbeddingComponentTemplate embeddingComponentTemplate) {

		return PutComponentTemplateRequest.of(component -> component
			.name(embeddingComponentTemplate.getName())
			.template(template -> template
				.settings(settings -> settings.knn(true))
				.mappings(mapping -> mapping
					.properties(
						"indexName", p -> p
							.text(text -> text.fields(
								"keyword",
								Property.of(field -> field
									.keyword(keyword -> keyword.ignoreAbove(256)))
							))
					)
					.properties(
						"contentId", p -> p
							.text(text -> text.fields(
								"keyword",
								Property.of(field -> field
									.keyword(keyword -> keyword.ignoreAbove(256)))
							))
					)
					.properties(
						"number", p -> p
							.integer(int_ -> int_)
					)
					.properties(
						"total", p -> p
							.integer(int_ -> int_)
					)
					.properties(
						"chunkText", p -> p
							.text(text -> text.fields(
								"keyword",
								Property.of(field -> field
									.keyword(keyword -> keyword.ignoreAbove(256)))
							))
					)
					.properties(
						"title", p -> p
							.text(text -> text.fields(
								"keyword",
								Property.of(field -> field
									.keyword(keyword -> keyword.ignoreAbove(256)))
							))
					)
					.properties(
						"url", p -> p
							.text(text -> text.fields(
								"keyword",
								Property.of(field -> field
									.keyword(keyword -> keyword.ignoreAbove(256)))
							))
					)
					.properties(
						"vector", p -> p
							.knnVector(knn -> knn.dimension(embeddingComponentTemplate.vectorSize()))
					)
				)
			)
		);
	}

	protected static PutComposableIndexTemplateRequest createIndexTemplateRequest(
		DataIndexTemplate indexTemplateRequest) {

		var tenantId = indexTemplateRequest.tenantId();
		var dataIndex = indexTemplateRequest.dataIndex();
		var indexSettings = indexTemplateRequest.settings();
		var embeddingModel = indexTemplateRequest.embeddingModel();

		Map<MappingsKey, Object> mappings =
			IndexMappingsUtil.docTypesToMappings(dataIndex.getDocTypes());

		var settings = getSettings(indexSettings, dataIndex);

		PutComposableIndexTemplateRequest request =
			new PutComposableIndexTemplateRequest();

		ComposableIndexTemplate composableIndexTemplate = null;

		try {
			String indexName = null;

			if (tenantId != null) {
				indexName = DataIndex.getIndexName(tenantId, dataIndex);
			}
			else {
				indexName = dataIndex.getIndexName();
			}

			List<String> componentTemplates = new ArrayList<>();

			if (embeddingModel != null) {

				var componentTemplate = EmbeddingComponentTemplate
					.fromEmbeddingModel(embeddingModel);
				componentTemplates.add(componentTemplate.getName());

			}

			composableIndexTemplate = new ComposableIndexTemplate(
				List.of(indexName),
				new Template(
					settings, new CompressedXContent(
					Json.encode(mappings)), null
				),
				componentTemplates
				, null, null, null
			);

			request
				.name(indexName + "-template")
				.indexTemplate(composableIndexTemplate);

			return request;
		}
		catch (UnknownTenantException e) {
			throw new WebApplicationException(Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(JsonObject.of(
					DataIndexService.DETAILS_FIELD, "cannot obtain a proper index name"
				))
				.build());
		}
		catch (IOException e) {
			throw new WebApplicationException(Response
				.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(JsonObject.of(
					DataIndexService.DETAILS_FIELD, "failed creating IndexTemplate"
				))
				.build());
		}
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

	protected static Map<String, List<DocTypeField>> toDocTypeAndFieldsGroup(
		List<DocTypeField> docTypeFields, List<String> documentTypes) {

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

	protected static List<DocTypeField> toDocTypeFields(Map<String, Object> mappings) {
		return _toDocTypeFields(_toFlatFields(mappings));
	}

	private static Settings getSettings(Map<String, Object> settingsMap, DataIndex dataIndex) {
		Settings settings;

		settingsMap = settingsMap != null && !settingsMap.isEmpty()
			? settingsMap
			: IndexMappingsUtil.docTypesToSettings(dataIndex.getDocTypes());

		if (settingsMap.isEmpty()) {
			settings = Settings.EMPTY;
		}
		else {
			settings = Settings.builder()
				.loadFromMap(settingsMap)
				.build();
		}

		return settings;
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

	private static Field _toFlatFields(Map<String, Object> mappings) {
		Field root = Field.createRoot();
		_toFlatFields(mappings, root);
		return root;
	}

	private Uni<Set<DocType>> _refreshDocTypeSet(
		Mutiny.Session session, Map<String, List<DocTypeField>> docTypeAndFieldsGroup) {

		Set<String> docTypeNames = docTypeAndFieldsGroup.keySet();

		return docTypeService
			.getDocTypesAndDocTypeFieldsByNames(session, docTypeNames)
			.map(existingDocTypes -> mergeDocTypes(
				docTypeAndFieldsGroup, existingDocTypes)
			);
	}

}
