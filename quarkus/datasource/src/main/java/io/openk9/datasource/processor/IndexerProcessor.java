package io.openk9.datasource.processor;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.openk9.datasource.processor.util.Field;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.util.UniActionListener;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class IndexerProcessor {

	@Incoming("index-writer")
	@ActivateRequestContext
	public Uni<Void> process(Message<?> message) {

		IngestionIndexWriterPayload payload =
			_messagePayloadToJson(message)
				.mapTo(IngestionIndexWriterPayload.class);

		IngestionPayload ingestionPayload = payload.getIngestionPayload();

		ContextInternal context =(ContextInternal) Vertx.currentContext();



		return datasourceService.withTransaction(s ->
			s.find(Datasource.class, ingestionPayload.getDatasourceId())
				.onItem()
				.ifNotNull()
				.transformToUni(datasource ->
					s.fetch(datasource.getDataIndex())
						.flatMap(dataIndex -> {

							String indexName;

							if (dataIndex != null) {
								indexName = dataIndex.getName();
							}
							else {
								indexName = datasource.getId() + "-data-" + UUID.randomUUID();
							}

							IndexRequest indexRequest = new IndexRequest(indexName);

							JsonObject jsonObject = _toIndexDocument(ingestionPayload);

							indexRequest.source(
								jsonObject.toString(),
								XContentType.JSON
							);

							indexRequest.setRefreshPolicy(
								WriteRequest.RefreshPolicy.WAIT_UNTIL);

							GetMappingsResponse mapping;

							try {

								client.index(
									indexRequest,
									RequestOptions.DEFAULT);

								mapping =
									client.indices().getMapping(
										new GetMappingsRequest().indices(
											indexName),
										RequestOptions.DEFAULT
									);
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}

							Map<String, Object> map =
								mapping.mappings().get(
									indexName).sourceAsMap();

							Field field =
								IndexerProcessor._toFlatFields(map);

							List<DocTypeField> currDocTypeFields =
								new ArrayList<>();

							_toDocTypeFields(
								field, new ArrayList<>(), currDocTypeFields,
								List.of(ingestionPayload.getDocumentTypes()));

							Map<String, List<DocTypeField>> dt =
								currDocTypeFields
									.stream()
									.map(dtf ->
										Tuple2.of(
											dtf.getName().contains(".")
												? dtf
												.getName()
												.substring(0,
													dtf.getName().indexOf(
														"."))
												: dtf.getName(),
											dtf
										)
									)
									.collect(Collectors.groupingBy(
										Tuple2::getItem1,
										Collectors.mapping(
											Tuple2::getItem2,
											Collectors.toList())));

							Uni<List<DocType>> docTypeListUni =
								dataIndex == null
									? Uni.createFrom().<List<DocType>>item(List::of)
									: s.fetch(dataIndex.getDocTypes());

							Uni<List<DocType>> docTypesUni =
								docTypeListUni.map(docTypes -> {

									List<DocType> docTypeUniList =
										new ArrayList<>();

									for (String name : dt.keySet()) {

										DocType docType = docTypes
											.stream()
											.filter(d -> d.getName().equals(
												name))
											.findFirst()
											.orElse(null);

										List<DocTypeField>
											docTypeFieldList = dt.get(name);

										if (docType == null) {
											DocType newDocType =
												new DocType();
											newDocType.setName(name);
											newDocType.setDocTypeFields(
												docTypeFieldList);
											newDocType.setDescription(
												"auto-generated");
											docTypeUniList.add(newDocType);

											for (DocTypeField docTypeField : docTypeFieldList) {
												docTypeField.setDocType(newDocType);
											}

										}
										else {

											List<DocTypeField> docTypeFields =
												docType.getDocTypeFields();

											List<DocTypeField>
												collect =
												_mergeDocTypeList(
													docTypeFieldList,
													docTypeFields);

											docType.setDocTypeFields(collect);

											for (DocTypeField docTypeField : collect) {
												docTypeField.setDocType(docType);
											}

											docTypeUniList.add(docType);


										}
									}

									return docTypeUniList;

								});

							return docTypesUni
								.flatMap(docTypeList -> {

									DataIndex di = dataIndex;

									if (di == null) {
										di = new DataIndex();
										di.setName(indexName);
									}

									di.setDocTypes(docTypeList);
									datasource.setDataIndex(di);
									return s.merge(datasource);

								});

						})
				)
				.chain(s::flush)
		)
			.onItemOrFailure()
			.transformToUni((datasource, throwable) -> {
				if (throwable != null) {
					logger.error("Error while saving datasource", throwable);
					return Uni.createFrom().completionStage(message.nack(throwable));
				}
				else {
					return Uni.createFrom().completionStage(message.ack());
				}
			});
	}

	private static List<DocTypeField> _mergeDocTypeList(
		List<DocTypeField> docTypeFieldList, List<DocTypeField> docTypeFields) {
		return Stream.concat(
			docTypeFields.stream(),
			docTypeFieldList
				.stream()
				.filter(
					dtf -> docTypeFields
						.stream()
						.noneMatch(dtf2 -> dtf2.getName().equals(dtf.getName()))
				)
			)
			.collect(Collectors.toList());
	}

	private Uni<List<DocTypeField>> _indexDocAndReturnMappings(
		String indexName, IndexRequest indexRequest, List<String> docTypes) {
		return _indexDocument(indexRequest)
			.flatMap(indexResponse -> _getMappings(indexName))
			.map(response -> response.mappings().get(indexName).sourceAsMap())
			.map(IndexerProcessor::_toFlatFields)
			.map(root -> {
				List<DocTypeField> docTypeFields = new ArrayList<>();
				_toDocTypeFields(
					root, new ArrayList<>(), docTypeFields, docTypes);
				return docTypeFields;
			});
	}

	private Uni<GetMappingsResponse> _getMappings(String indexName) {
		return Uni
			.createFrom()
			.emitter(
				sink -> client
					.indices()
					.getMappingAsync(
						new GetMappingsRequest().indices(indexName),
						RequestOptions.DEFAULT,
						UniActionListener.of(sink))
			);
	}

	private Uni<IndexResponse> _indexDocument(IndexRequest indexRequest) {
		return Uni
			.createFrom()
			.emitter(
				sink -> client.indexAsync(
					indexRequest, RequestOptions.DEFAULT,
					UniActionListener.of(sink)));
	}

	private static void _toDocTypeFields(
		Field root, List<String> acc, List<DocTypeField> docTypeFields,
		List<String> docTypes) {

		String name = root.getName();

		if (!root.isRoot()) {
			acc.add(name);
		}

		String type = root.getType();

		if (type != null) {

			String fieldName = String.join(".", acc);

			if (docTypes.stream().anyMatch(dt -> fieldName.startsWith(dt + "."))) {
				DocTypeField docTypeField = new DocTypeField();
				docTypeField.setName(fieldName);
				docTypeField.setFieldType(FieldType.fromString(type));
				docTypeField.setBoost(1.0);
				docTypeField.setSearchable(false);
				docTypeField.setDescription("this doc type field is auto generated");
				docTypeFields.add(docTypeField);
			}
		}

		for (Field subField : root.getSubFields()) {
			_toDocTypeFields(
				subField, new ArrayList<>(acc), docTypeFields, docTypes);
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

	private JsonObject _toIndexDocument(
		IngestionPayload ingestionPayload) {

		JsonObject jsonObject = JsonObject.mapFrom(ingestionPayload);

		Object datasourcePayload =
			jsonObject.remove("datasourcePayload");

		if (datasourcePayload != null) {
			JsonObject datasourcePayloadJsonObject =
				(JsonObject)datasourcePayload;

			for (Map.Entry<String, Object> keyValue :
				datasourcePayloadJsonObject) {

				jsonObject.put(keyValue.getKey(), keyValue.getValue());

			}
		}

		return jsonObject;

	}

	private JsonObject _messagePayloadToJson(Message<?> message) {
		Object obj = message.getPayload();

		return obj instanceof JsonObject
			? (JsonObject) obj
			: new JsonObject(new String((byte[]) obj));

	}

	@Inject
	DatasourceService datasourceService;

	@Inject
	DocTypeService docTypeService;

	@Inject
	RestHighLevelClient client;

	@Inject
	DataIndexService dataIndexService;

	@Inject
	Logger logger;

}
