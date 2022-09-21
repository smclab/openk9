package io.openk9.datasource.processor;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.openk9.datasource.processor.util.Field;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.DocTypeService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.common.xcontent.XContentType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class IndexerProcessor {

	@Incoming("index-writer")
	public Uni<Void> process(Message<?> message) {

		IngestionIndexWriterPayload payload =
			_messagePayloadToJson(message)
				.mapTo(IngestionIndexWriterPayload.class);

		IngestionPayload ingestionPayload = payload.getIngestionPayload();

		return datasourceService
			.findById(ingestionPayload.getDatasourceId())
			.onItem()
			.ifNotNull()
			.transformToUni(datasource ->
				datasourceService.getDataIndex(datasource)
					.flatMap(dataIndex -> {

						if (dataIndex == null) {

							String indexName =
								datasource.getId() + "-data-" + UUID.randomUUID();

							IndexRequest indexRequest = new IndexRequest(indexName);

							JsonObject jsonObject = _toIndexDocument(ingestionPayload);

							indexRequest.source(
								jsonObject.toString(),
								XContentType.JSON
							);

							indexRequest.setRefreshPolicy(
								WriteRequest.RefreshPolicy.WAIT_UNTIL);

							Uni<List<DocTypeField>> createDocumentUni =
								_createDocAndReturnMappings(
									indexName, indexRequest,
									List.of(ingestionPayload.getDocumentTypes()));

							Uni<Map<String, List<DocTypeField>>> mapDocTypeNameDocTypeFieldList =
								createDocumentUni
									.map(list ->
										list
											.stream()
											.map(dtf ->
												Tuple2.of(
													dtf.getName().contains(".")
														? dtf
															.getName()
															.substring(0, dtf.getName().indexOf("."))
														: dtf.getName(),
													dtf
												)
											)
											.collect(Collectors.groupingBy(
												Tuple2::getItem1,
												Collectors.mapping(
													Tuple2::getItem2,
													Collectors.toList())))
									);

							Uni<Map<DocType, List<DocTypeField>>> mapUni =
								mapDocTypeNameDocTypeFieldList
									.flatMap(dt -> {
										List<Uni<Tuple2<DocType, ? extends List<DocTypeField>>>>
											collect =
												dt
													.entrySet()
													.stream()
													.map(e ->
														docTypeService
															.findByName(e.getKey())
															.flatMap(docType -> {

																Set<DocTypeField> docTypeFields =
																	new HashSet<>(e.getValue());

																if (docType == null) {

																	DocType newDocType = new DocType();

																	newDocType.setDocTypeFields(docTypeFields);
																	newDocType.setDescription(e.getKey());
																	newDocType.setName(e.getKey());

																	for (DocTypeField docTypeField : docTypeFields) {
																		docTypeField.setDocType(newDocType);
																	}

																	return Uni
																		.createFrom()
																		.item(
																			Tuple2.<DocType, List<DocTypeField>>of(
																				newDocType,
																				e.getValue()
																			)
																		);
																}
																else {

																	return docTypeService
																		.getDocTypeFields(docType)
																		.map(dtfl ->
																			Stream.concat(
																				dtfl.stream(),
																				docTypeFields
																					.stream()
																					.filter(dtf -> dtfl
																						.stream()
																						.noneMatch(dtf2 -> dtf2.getName().equals(dtf.getName())))
																				)
																				.collect(Collectors.toSet())
																		)
																		.map(s -> {

																			docType.setDocTypeFields(s);

																			return Tuple2.of(
																				docType,
																				new ArrayList<>(s)
																			);
																		});
																}
															})
													)
													.collect(Collectors.toList());

										return Uni
											.combine()
											.all()
											.unis(collect)
											.combinedWith(e -> e
												.stream()
												.map(
													e1 -> (Tuple2<DocType, List<DocTypeField>>) e1)
												.collect(Collectors.toMap(
													Tuple2::getItem1,
													Tuple2::getItem2
												))
											);
									});

							return mapUni.flatMap(m -> {

								DataIndex di = new DataIndex();

								di.setName(indexName);
								di.setDocTypes(m.keySet());


								datasource.setDataIndex(di);

								return datasourceService.merge(datasource);

							})
								.replaceWithVoid();
						}
						else {

							IndexRequest indexRequest = new IndexRequest(dataIndex.getName());

							JsonObject jsonObject = _toIndexDocument(ingestionPayload);

							indexRequest.source(
								jsonObject.toString(),
								XContentType.JSON
							);

							indexRequest.setRefreshPolicy(
								WriteRequest.RefreshPolicy.WAIT_UNTIL);

							return Uni
								.createFrom()
								.<IndexResponse>emitter(
									sink -> client.indexAsync(
										indexRequest, RequestOptions.DEFAULT,
										new ActionListener<>() {
											@Override
											public void onResponse(
												IndexResponse indexResponse) {
												sink.complete(indexResponse);
											}

											@Override
											public void onFailure(Exception e) {
												sink.fail(e);
											}
										}))
								.replaceWithVoid();

						}

					})
			)
			.onFailure()
			.call((t) -> Uni.createFrom().completionStage(() -> message.nack(t)))
			.replaceWith(Uni.createFrom().completionStage(message::ack));

	}

	private Uni<List<DocTypeField>> _createDocAndReturnMappings(
		String indexName, IndexRequest indexRequest, List<String> docTypes) {
		return Uni
			.createFrom()
			.<IndexResponse>emitter(
				sink -> client.indexAsync(
					indexRequest, RequestOptions.DEFAULT,
					new ActionListener<>() {
						@Override
						public void onResponse(
							IndexResponse indexResponse) {
							sink.complete(indexResponse);
						}

						@Override
						public void onFailure(Exception e) {
							sink.fail(e);
						}
					}))
			.flatMap(indexResponse ->
				Uni
					.createFrom()
					.<GetMappingsResponse>emitter(
						sink -> client
							.indices()
							.getMappingAsync(
								new GetMappingsRequest().indices(
									indexName),
								RequestOptions.DEFAULT,
								new ActionListener<>() {
									@Override
									public void onResponse(
										GetMappingsResponse getMappingsResponse) {
										sink.complete(
											getMappingsResponse);
									}

									@Override
									public void onFailure(
										Exception e) {
										sink.fail(e);
									}
								})
					)
			)
			.map(response -> response.mappings().get(indexName).sourceAsMap())
			.map(IndexerProcessor::_toFlatFields)
			.map(root -> {
				List<DocTypeField> docTypeFields = new ArrayList<>();
				_toDocTypeFields(
					root, new ArrayList<>(), docTypeFields, docTypes);
				return docTypeFields;
			});
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
				subField,
				new ArrayList<>(acc),
				docTypeFields,
				docTypes);
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

}
