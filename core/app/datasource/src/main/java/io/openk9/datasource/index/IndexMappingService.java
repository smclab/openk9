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

import io.openk9.datasource.index.model.DataIndexTemplate;
import io.openk9.datasource.index.model.EmbeddingComponentTemplate;
import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.index.model.MappingsKey;
import io.openk9.datasource.index.util.IndexMappingUtils;
import io.openk9.datasource.index.util.OpenSearchUtils;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.util.DocTypeFieldUtils;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.processor.util.Field;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DocTypeService;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.opensearch.client.indices.PutComposableIndexTemplateRequest;
import org.opensearch.client.opensearch.cluster.PutComponentTemplateRequest;
import org.opensearch.cluster.metadata.ComposableIndexTemplate;
import org.opensearch.cluster.metadata.Template;
import org.opensearch.common.compress.CompressedXContent;
import org.opensearch.common.settings.Settings;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for handling the creation and management of index mappings in OpenSearch.
 * This class provides functionality for creating index templates, component templates, and
 * generating DocType fields from various sources like plugin drivers or existing indices.
 */
@ApplicationScoped
public class IndexMappingService {

	public static final String GENERATE_DOC_TYPE = "IndexMappingService#generateDocTypeFieldsFromPluginDriverSample";

	// all the document fields that must be ignored on write
	private static final String[] IGNORED_FIELD_PATHS = new String[]{
		"vector",
		"ingestionId",
		"datasourceId",
		"parsingDate",
		"tenantId",
		"resources",
		"rest",
		"indexName",
		"last",
		"scheduleId",
		"oldIndexName",
		"type",
	};

	private final static Logger log =
		Logger.getLogger(IndexMappingService.class);

	static {
		Arrays.sort(IGNORED_FIELD_PATHS);
	}

	public static boolean isIgnoredFieldPath(String fieldPath) {
		return Arrays.binarySearch(IGNORED_FIELD_PATHS, fieldPath) >= 0;
	}

	@Inject
	IndexService indexService;
	@Inject
	DocTypeService docTypeService;
	@Inject
	HttpPluginDriverClient httpPluginDriverClient;
	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;
	@Inject
	io.quarkus.qute.Template embeddingComponentMappings;
	@Inject
	Mutiny.SessionFactory sessionFactory;

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
	 * @param session
	 * @param embeddingComponentTemplate The object containing the name of the component
	 *                                   template and the dimension of the {@code knnVector}.
	 *                                   Must not be {@code null}.
	 * @return A {@link Uni<Void>} representing the asynchronous operation. If the operation
	 * succeeds, the result is empty. If the operation fails, the failure is
	 * propagated through the {@link Uni} pipeline, allowing the caller to handle
	 * the error appropriately.
	 **/
	public Uni<Void> createEmbeddingComponentTemplate(
		Mutiny.Session session,
		EmbeddingComponentTemplate embeddingComponentTemplate) {

		if (log.isDebugEnabled()) {
			log.debugf(
				"Creating a componentTemplate named %s, the vector size is %d.",
				embeddingComponentTemplate.getName(),
				embeddingComponentTemplate.vectorSize()
			);
		}

		var mappings = embeddingComponentMappings.data(
			"knnVectorDimension",
			embeddingComponentTemplate.vectorSize()
		).render();

		var componentTemplateRequest = createComponentTemplateRequest(
			embeddingComponentTemplate.getName(),
			mappings
		);

		var jsonObject = (JsonObject) Json.decodeValue(mappings);

		return indexService.putComponentTemplate(componentTemplateRequest)
			.call(() -> generateDocTypeFields(
				session,
				jsonObject.getMap(),
				List.of(DocType.DEFAULT_NAME)
			).flatMap(docTypes -> session.mergeAll(docTypes.toArray())));
	}

	/**
	 * Retrieves mappings configuration from a list of DocType identifiers.
	 * <p>
	 * This method fetches all DocType entities specified by the provided IDs and transforms
	 * them into a structured mapping configuration suitable for OpenSearch index templates.
	 * The mappings define how document fields should be stored and indexed in OpenSearch.
	 *
	 * @param docTypeIds A list of DocType entity IDs to retrieve and process
	 * @return A {@link Uni} containing a Map with MappingsKey objects as keys and their corresponding
	 * mapping configurations as values, ready to be used in OpenSearch index templates
	 */
	public Uni<Map<MappingsKey, Object>> getMappingsFromDocTypes(List<Long> docTypeIds) {
		return docTypeService.findDocTypes(docTypeIds).map(IndexMappingUtils::docTypesToMappings);
	}

	/**
	 * Retrieves index settings configuration from a list of DocType identifiers.
	 * <p>
	 * This method fetches all DocType entities specified by the provided IDs and extracts
	 * their associated settings configuration. The settings define various OpenSearch index
	 * properties such as number of shards, replicas, analysis settings, etc.
	 *
	 * @param docTypeIds A list of DocType entity IDs to retrieve and process
	 * @return A {@link Uni} containing a Map with setting names as String keys and their
	 * corresponding setting values as Objects, ready to be used in OpenSearch index templates
	 */
	public Uni<Map<String, Object>> getSettingsFromDocTypes(List<Long> docTypeIds) {
		return docTypeService.findDocTypes(docTypeIds).map(IndexMappingUtils::docTypesToSettings);
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
		Mutiny.Session session, IndexName indexName) {

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
	 * The operation includes a retry mechanism for 'SYSTEM' provisioned plugin drivers
	 * to handle transient failures during transaction processing.
	 *
	 * @param message The message containing the Hibernate reactive session, plugin driver information,
	 * and provisioning type.
	 * @return A {@link Uni} containing a Set of generated or updated DocType objects
	 */
	@ConsumeEvent(GENERATE_DOC_TYPE)
	public Uni<Set<DocType>> generateDocTypeFieldsFromPluginDriverSample(
			GenerateDocTypeFromPluginSampleMessage message) {

		var session = message.session();
		var httpPluginDriverInfo = message.httpPluginDriverInfo();
		var provisioning = message.provisioning();

		Uni<Set<DocType>> generateDocTypeUni;

		// Adds the retry if pluginDriver is of 'SYSTEM' type.
		generateDocTypeUni = switch (provisioning) {
			case USER -> generateDocTypeUni(httpPluginDriverInfo, session);
			case SYSTEM -> sessionFactory.withTransaction(newSession ->
					generateDocTypeUni(httpPluginDriverInfo, newSession)
				)
				.onFailure()
				.retry()
				.withBackOff(Duration.ofSeconds(5))
				.atMost(20);
		};

		return generateDocTypeUni
			.flatMap(docTypes -> {
				log.debug("DocType size=" + docTypes.size());
				return Uni.createFrom().item(docTypes);
			})
			.onItem()
			.invoke(() -> log.info("DocumentTypes associated with pluginDriver created/updated."))
			.onFailure()
			.invoke(() -> log.warn("Error creating/updating DocumentTypes associated with pluginDriver"));
	}

	protected static PutComponentTemplateRequest createComponentTemplateRequest(
		String componentTemplateName, String mappings) {

		return PutComponentTemplateRequest.of(component -> component
			.name(componentTemplateName)
			.template(template -> template
				.settings(settings -> settings.knn(true))
				.mappings(mapping -> mapping
					.withJson(new StringReader(mappings))
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
			IndexMappingUtils.docTypesToMappings(dataIndex.getDocTypes());

		var settings = getSettings(indexSettings, dataIndex);

		PutComposableIndexTemplateRequest request =
			new PutComposableIndexTemplateRequest();

		ComposableIndexTemplate composableIndexTemplate = null;

		try {
			IndexName indexName = IndexName.from(tenantId, dataIndex);

			List<String> componentTemplates = new ArrayList<>();

			// adds the knn component template on this indexTemplate
			if (dataIndex.getKnnIndex() && embeddingModel != null) {

				var componentTemplate = new EmbeddingComponentTemplate(
					tenantId,
					embeddingModel.getName(),
					embeddingModel.getVectorSize()
				);

				componentTemplates.add(componentTemplate.getName());

			}

			composableIndexTemplate = new ComposableIndexTemplate(
				List.of(indexName.toString()),
				new Template(
					settings, new CompressedXContent(
					Json.encode(mappings)), null
				),
				componentTemplates
				, null, null, null
			);

			request
				.name(indexName + IndexService.TEMPLATE_SUFFIX)
				.indexTemplate(composableIndexTemplate);

			return request;
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
				var fieldPath = DocTypeFieldUtils.fieldPath(docTypeName, docTypeField);
				boolean retained = true;

				// does not retain an ignored field
				if (isIgnoredFieldPath(fieldPath)) {
					continue;
				}

				// does not retain an existing field
				for (DocTypeField existingField : persistedFields) {

					var existingFieldPath = existingField.getPath();
					if (Objects.equals(existingFieldPath, fieldPath)) {
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
					.orElse(DocType.DEFAULT_NAME),
				Collectors.toList()
			));

		_explodeDocTypeFirstLevel(grouped);

		return grouped;
	}

	protected static List<DocTypeField> toDocTypeFields(Map<String, Object> mappings) {
		return _toDocTypeFields(_toFlatFields(mappings));
	}

	private Uni<Set<DocType>> generateDocTypeUni(HttpPluginDriverInfo httpPluginDriverInfo, Mutiny.Session session) {
		Uni<Set<DocType>> generateDocTypeUni;
		generateDocTypeUni = httpPluginDriverClient.getSample(httpPluginDriverInfo)
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
		return generateDocTypeUni;
	}

	private static Settings getSettings(Map<String, Object> settingsMap, DataIndex dataIndex) {

		var settingsBuilder = Settings.builder();

		settingsMap = settingsMap != null && !settingsMap.isEmpty()
			? settingsMap
			: IndexMappingUtils.docTypesToSettings(dataIndex.getDocTypes());

		if (!settingsMap.isEmpty()) {
			settingsBuilder.loadFromMap(settingsMap);
		}

		return settingsBuilder.build();
	}

	private static void _explodeDocTypeFirstLevel(Map<String, List<DocTypeField>> grouped) {
		for (String docTypeName : grouped.keySet()) {
			if (!docTypeName.equals(DocType.DEFAULT_NAME)) {
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

	public record GenerateDocTypeFromPluginSampleMessage(
		Mutiny.Session session,
		HttpPluginDriverInfo httpPluginDriverInfo,
		PluginDriver.Provisioning provisioning
	) {}

}
