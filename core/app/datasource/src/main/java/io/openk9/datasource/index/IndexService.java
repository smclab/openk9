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

import io.openk9.datasource.index.response.CatResponse;
import io.openk9.datasource.util.UniActionListener;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpEntity;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class IndexService {

	@Inject
	RestHighLevelClient restHighLevelClient;

	public Uni<Map<String, Object>> getMappings(String indexName) {
		return Uni
			.createFrom()
			.item(() -> {
				try {
					return restHighLevelClient.indices().getMapping(
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

	public Uni<CatResponse> get_catIndicesFirst(String indexName) {
		return get_catIndices(indexName).map(catResponses -> {
			if (catResponses.isEmpty()) {
				return null;
			}
			return catResponses.get(0);
		});
	}

	public Uni<List<CatResponse>> get_catIndices(Collection<String> indexNames) {
		return get_catIndices(indexNames.toArray(String[]::new));
	}

	public Uni<String> getSettings(String indexName) {
		return Uni
			.createFrom()
			.item(() -> {
				try {
					return restHighLevelClient.indices().getSettings(
						new GetSettingsRequest().indices(indexName),
						RequestOptions.DEFAULT
					);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(response -> response.getIndexToSettings().get(indexName).toString());
	}

	private static List<CatResponse> responseToCatResponses(Response response) {

		JsonArray catResponseArr = parseEntity(response.getEntity());

		List<CatResponse> catResponseList =
			new ArrayList<>(catResponseArr.size());

		for (Object catResponse : catResponseArr) {
			if (catResponse instanceof JsonObject) {
				JsonObject jsonObject = (JsonObject) catResponse;
				catResponseList.add(
					jsonObject.mapTo(CatResponse.class));
			}
		}

		return catResponseList;

	}

	private static JsonArray parseEntity(final HttpEntity entity) {

		if (entity == null) {
			throw new IllegalStateException("Response body expected but not returned");
		}
		if (entity.getContentType() == null) {
			throw new IllegalStateException("Elasticsearch didn't return the [Content-Type] header, unable to parse response body");
		}
		XContentType xContentType = XContentType.fromMediaTypeOrFormat(entity.getContentType().getValue());
		if (xContentType == null) {
			throw new IllegalStateException("Unsupported Content-Type: " + entity.getContentType().getValue());
		}

		try (InputStream inputStream = entity.getContent()) {
			String stringContent = new String(inputStream.readAllBytes());
			return new JsonArray(stringContent);
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

	}

	public Uni<Long> indexCount(Collection<String> indexNames) {
		return indexCount(indexNames.toArray(String[]::new));
	}

	public Uni<List<CatResponse>> get_catIndices(String...indexNames) {

		return getOnlyExistsIndexNames(List.of(indexNames))
			.flatMap(existIndexNames -> {

				if (existIndexNames.isEmpty()) {
					return Uni.createFrom().item(List.of());
				}

				String indexName = String.join(",", existIndexNames);

				return Uni
						.createFrom()
						.<Response>emitter((sink) -> {

							RestClient lowLevelClient = restHighLevelClient.getLowLevelClient();

							Request
								catRequest = new Request("GET", "/_cat/indices/" + indexName);

							catRequest.addParameter("format", "JSON");
							catRequest.addParameter("v", "true");
							catRequest.addParameter("bytes", "b");

							lowLevelClient.performRequestAsync(
								catRequest,
								new ResponseListener() {
									@Override
									public void onSuccess(Response response) {
										sink.complete(response);
									}

									@Override
									public void onFailure(Exception exception) {
										sink.fail(exception);
									}
								});
						})
						.map(IndexService::responseToCatResponses);
				}
			);
	}

	public Uni<List<String>> getOnlyExistsIndexNames(List<String> indexNames) {
		return getExistsAndIndexNames(indexNames)
			.onItem()
			.transformToUni(existsAndIndexNames -> {
				List<String> onlyExistsIndexNames = new ArrayList<>();
				for (Tuple2<Boolean, String> existsAndIndexName : existsAndIndexNames) {
					if (existsAndIndexName.getItem1()) {
						onlyExistsIndexNames.add(existsAndIndexName.getItem2());
					}
				}
				return Uni.createFrom().item(onlyExistsIndexNames);
			});
	}


	public Uni<List<Tuple2<Boolean, String>>> getExistsAndIndexNames(List<String> indexNames) {

		List<Uni<Tuple2<Boolean, String>>> existIndexNames =
			new ArrayList<>(indexNames.size());

		for (String indexName : indexNames) {
			Uni<Tuple2<Boolean, String>> existIndexName =
				indexExist(indexName)
					.onItemOrFailure()
					.transform((exist, t) -> {

						if (t != null) {
							logger.error("Error while checking index exist", t);
							return false;
						}

						return exist;

					})
					.map(exist -> Tuple2.of(exist, indexName));
			existIndexNames.add(existIndexName);
		}
		return Uni
			.join()
			.all(existIndexNames)
			.andCollectFailures();

	}

	public Uni<Long> indexCount(String...indexName) {
		return Uni
			.createFrom()
			.<CountResponse>emitter(
				emitter -> restHighLevelClient
					.countAsync(
						new CountRequest(indexName), RequestOptions.DEFAULT,
						UniActionListener.of(emitter)
					)
			)
			.onItemOrFailure()
			.transformToUni((countResponse, throwable) -> {
				if (throwable != null) {
					logger.error("Error getting index count", throwable);
					return Uni.createFrom().nullItem();
				}
				return Uni.createFrom().item(countResponse.getCount());
			});
	}

	public Uni<Boolean> indexExist(String name) {
		return Uni
			.createFrom()
			.<Boolean>emitter(
				emitter -> restHighLevelClient
					.indices()
					.existsAsync(
						new GetIndexRequest(name), RequestOptions.DEFAULT,
						UniActionListener.of(emitter)
					)
			)
			.onItemOrFailure()
			.transformToUni((response, throwable) -> {
				if (throwable != null) {
					logger.error("Error getting index exist", throwable);
					return Uni.createFrom().nullItem();
				}
				return Uni.createFrom().item(response);
			});
	}

	@Inject
	Logger logger;

}
