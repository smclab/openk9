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

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.processor.util.Field;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.util.UniActionListener;
import io.smallrye.mutiny.Uni;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.ResizeRequest;
import org.elasticsearch.client.indices.ResizeResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.jboss.logging.Logger;
import reactor.core.publisher.Mono;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class DatasourceIndexService {

	public Uni<List<DocType>> getDocTypes(
		String indexName, List<String> docTypes) {
		getMappings(indexName)
			.map(DatasourceIndexService::_toFlatFields)
			.map(field -> _toDocTypes(field, docTypes));

		return null;

	}

	public Uni<Map<String, Object>> getMappings(String indexName) {
		return Uni
			.createFrom()
			.<GetMappingsResponse>emitter(
				sink -> client
					.indices()
					.getMappingAsync(
						new GetMappingsRequest().indices(indexName),
						RequestOptions.DEFAULT,
						UniActionListener.of(sink))
			)
			.map(GetMappingsResponse::mappings)
			.map(m -> m.get(indexName))
			.map(MappingMetadata::getSourceAsMap);

	}

	private Uni<List<DocType>> _toDocTypes(
		Field field,  List<String> docTypes) {

		/*List<DocTypeField> docTypeFields = new ArrayList<>();

		_toDocTypeFields(
			field, new ArrayList<>(), docTypeFields, docTypes);

		Map<String, Set<DocTypeField>> docTypeNameMap =
			docTypeFields
				.stream()
				.collect(
					Collectors.groupingBy(
						d -> d.getName().substring(0, d.getName().indexOf(".")),
						Collectors.toSet())
				);

		List<Uni<DocType>> docTypeList = new ArrayList<>();

		for (String docTypeName : docTypeNameMap.keySet()) {

			Set<DocTypeField> currentDocTypeFields =
				docTypeNameMap.get(docTypeName);

			docTypeList.add(
				docTypeService.withTransaction(s ->
					docTypeService
						.findByName(docTypeName)
						.flatMap(docType -> {
							if (docType == null) {
								DocType newDocType = new DocType();
								newDocType.setName(docTypeName);
								newDocType.setDescription("auto-generated");
								newDocType.setDocTypeFields(currentDocTypeFields);
								return Uni.createFrom().item(newDocType);
							}
							else {
								Mutiny2
									.fetch(s, docType.getDocTypeFields())
									.invoke(docTypeFieldList -> {
										for (DocTypeField docTypeField : docTypeFieldList) {
											if (currentDocTypeFields
												.stream()
												.noneMatch(cdtf -> cdtf.getName().equals(docTypeField.getName()))) {

											}
										}
									});

							}
						})
				)
			)


		}*/


		return null;
	}

	private static void _toDocTypeFields(
		Field root, List<String> acc, List<DocTypeField> docTypeFields,
		List<String> supportedDocTypes) {

		String name = root.getName();

		if (!root.isRoot()) {
			acc.add(name);
		}

		String type = root.getType();

		if (type != null) {

			String fieldName = String.join(".", acc);

			if (supportedDocTypes.stream().anyMatch(
				docType -> fieldName.startsWith(docType + "."))) {

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
				subField, new ArrayList<>(acc), docTypeFields, supportedDocTypes);
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

	public Mono<Object> reindex(Datasource datasource) {

		/*return Mono.defer(() -> {

			PluginDriver pluginDriver = datasource.getPluginDriver();

			DataIndex dataIndex = datasource.getDataIndex();

			return pluginDriverDTOMono
				.map(response -> datasource.getTenantId() + "-" + response.getName() + "-data")
				.filterWhen(indexName -> _indexExists(indexName,  client.indices()))
				.flatMap(indexName -> _modifiedSettings(indexName, client.indices()).thenReturn(indexName))
				.flatMap(indexName -> _cloneIndex(indexName, client.indices()).thenReturn(indexName))
				.flatMap(targetIndex ->
					Mono.create(emitter -> client.indices().deleteAsync(
						new DeleteIndexRequest(targetIndex), RequestOptions.DEFAULT,
						new ActionListener<>() {
							@Override
							public void onResponse(AcknowledgedResponse deleteResponse) {
								emitter.success(deleteResponse);
							}

							@Override
							public void onFailure(Exception e) {
								emitter.error(e);
							}
						}))
				)
				.doOnNext(o -> logger.info("datasource " + datasource.getDatasourceId() + " " + o))
				.defaultIfEmpty(
					Mono.fromRunnable(() -> logger.info("default case for datasource " + datasource.getDatasourceId())));
			});*/

		return Mono.empty();

	}

	private Mono<?> _modifiedSettings(String indexName, IndicesClient indices) {

		UpdateSettingsRequest updateSettingsRequest =
			new UpdateSettingsRequest(indexName);

		updateSettingsRequest.settings(
			Settings.builder()
				.put("index.blocks.write", true)
				.build());

		return Mono.create(sink -> indices.putSettingsAsync(
			updateSettingsRequest, RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(AcknowledgedResponse resizeResponse) {
					sink.success(resizeResponse);
				}

				@Override
				public void onFailure(Exception e) {
					sink.error(e);
				}
			}));

	}

	private Mono<ResizeResponse> _cloneIndex(String indexName, IndicesClient indices) {

		ResizeRequest resizeRequest = new ResizeRequest(
			indexName + "-clone-" + System.currentTimeMillis(), indexName);

		return Mono.create(emitter ->
			indices.cloneAsync(
				resizeRequest, RequestOptions.DEFAULT,
				new ActionListener<>() {
					@Override
					public void onResponse(ResizeResponse resizeResponse) {
						emitter.success(resizeResponse);
					}

					@Override
					public void onFailure(Exception e) {
						emitter.error(e);
					}
				})
		);
	}

	private Mono<Boolean> _indexExists(String indexName, IndicesClient indices) {
		return Mono.create(emitter -> indices.existsAsync(
			new GetIndexRequest(indexName), RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(Boolean exists) {
					emitter.success(exists);
				}

				@Override
				public void onFailure(Exception e) {
					emitter.error(e);
				}
			}));
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	Logger logger;

	@Inject
	DocTypeService docTypeService;

}
