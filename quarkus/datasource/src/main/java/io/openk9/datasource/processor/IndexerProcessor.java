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
			.flatMap(datasource ->
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
								_createDocAndReturnMappings(indexName, indexRequest);

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
			.replaceWith(Uni.createFrom().completionStage(() -> message.ack()));

	}

	private Uni<List<DocTypeField>> _createDocAndReturnMappings(
		String indexName, IndexRequest indexRequest) {
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
			.map(response -> response.mappings().get(indexName).sourceAsMap()
			)
			.map(IndexerProcessor::_toFlatFields)
			.map(IndexerProcessor::_toDocTypeFields);
	}

	private static List<DocTypeField> _toDocTypeFields(List<Field> fields) {

		List<DocTypeField> newFields = new ArrayList<>();

		StringBuilder tmp = null;

		for (Field field : fields) {

			if (tmp == null) {
				tmp = new StringBuilder(field.getName());
			}
			else {
				tmp.append(".").append(field.getName());
			}

			if (field.getType() != null) {

				DocTypeField docTypeField = new DocTypeField();

				docTypeField.setName(tmp.toString());
				docTypeField.setFieldType(
					FieldType.fromString(field.getType()));
				docTypeField.setDescription(tmp.toString());
				docTypeField.setBoost(1.0);

				newFields.add(docTypeField);

				if (field.getSubName() != null) {

					tmp.append(".").append(field.getSubName());

					docTypeField = new DocTypeField();

					docTypeField.setName(tmp.toString());
					docTypeField.setDescription(tmp.toString());
					docTypeField.setFieldType(
						FieldType.fromString(field.getType()));
					docTypeField.setBoost(1.0);
					docTypeField.setSearchable(false);
					newFields.add(docTypeField);

				}

				tmp = null;

			}

		}
		return newFields;
	}

	private static List<Field> _toFlatFields(Map<String, Object> mappings) {
		return _toFlatFields(mappings, new ArrayList<>());
	}

	private static List<Field> _toFlatFields(
		Map<String, Object> mappings, List<Field> acc) {

		for (Map.Entry<String, Object> kv : mappings.entrySet()) {

			String key = kv.getKey();
			Object value = kv.getValue();

			if (key.equals("properties")) {
				acc = _toFlatFields((Map<String, Object>) value, acc);
			}
			else if (value instanceof Map && ((Map)value).size() == 1) {
				Map<String, Object> map = (Map<String, Object>)value;
				if (map.containsKey("type")) {
					acc.add(
						Field
							.builder()
							.name(key)
							.type((String)map.get("type"))
							.build()
					);
				}
				else {
					acc.add(
						Field
							.builder()
							.name(key)
							.build()
					);
					acc = _toFlatFields((Map<String, Object>) value, acc);
				}
			}
			else if (value instanceof Map && ((Map)value).size() > 1) {
				Map<String, Object> localMap = ((Map<String, Object>)value);

				Field.FieldBuilder builder = Field.builder();

				builder.name(key);

				if (localMap.containsKey("type")) {
					builder.type((String)localMap.get("type"));
				}
				if (localMap.containsKey("fields")) {
					Map<String, Object> fields =
						(Map<String, Object>)localMap.get("fields");

					for (Map.Entry<String, Object> leafKV : fields.entrySet()) {
						String leafKey = leafKV.getKey();
						Map<String, Object> leafMap =
							(Map<String, Object>)leafKV.getValue();

						builder.subName(leafKey);

						builder.subType((String)leafMap.get("type"));
					}

				}

				acc.add(builder.build());

			}

		}

		return acc;

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
