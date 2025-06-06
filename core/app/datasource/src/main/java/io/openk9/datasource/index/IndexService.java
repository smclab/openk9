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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.datasource.index.exception.CannotCreateComponentTemplateException;
import io.openk9.datasource.index.exception.CannotCreateIndexTemplateException;
import io.openk9.datasource.index.exception.DeleteIndexException;
import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.index.response.CatResponse;
import io.openk9.datasource.util.UniActionListener;

import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpEntity;
import org.jboss.logging.Logger;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.ResponseListener;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.client.indices.GetMappingsRequest;
import org.opensearch.client.indices.PutComposableIndexTemplateRequest;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.cluster.PutComponentTemplateRequest;
import org.opensearch.core.xcontent.MediaType;
import org.opensearch.core.xcontent.MediaTypeRegistry;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.builder.SearchSourceBuilder;

@ApplicationScoped
public class IndexService {

	private final static Logger log = Logger.getLogger(IndexService.class);
	public static final String TEMPLATE_SUFFIX = "-template";

	@Inject
	RestHighLevelClient restHighLevelClient;
	@Inject
	OpenSearchClient openSearchClient;

	public Uni<Void> createIndexTemplate(
		PutComposableIndexTemplateRequest request) {

		return VertxContextSupport.executeBlocking(() -> {

			IndicesClient indices = restHighLevelClient.indices();
			var templateName = request.name();

			try {
				var response = indices.putIndexTemplate(
					request,
					RequestOptions.DEFAULT
				);

				if (response.isAcknowledged()) {
					return null;
				}
				else {
					log.errorf(
						"indexTemplate %s creation is not acknowledged",
						templateName
					);

					throw new CannotCreateIndexTemplateException("Not acknowledged");
				}
			}
			catch (CannotCreateIndexTemplateException k9Exception) {
				throw k9Exception;
			}
			catch (OpenSearchStatusException osse) {
				log.errorf(osse, "Cannot create indexTemplate %s", templateName);
				throw new CannotCreateIndexTemplateException(osse);
			}
			catch (Exception e) {
				log.errorf(
					e,
					"Error occurred when trying to create indexTemplate %s",
					templateName
				);
				throw new CannotCreateIndexTemplateException(e);
			}

		});
	}

	public Uni<Void> deleteIndex(IndexName indexName) {

		return VertxContextSupport.executeBlocking(() -> {
			try {
				// delete index or throw an exception
				var delete = openSearchClient.indices().delete(req -> req
					.index(indexName.toString())
					.ignoreUnavailable(true));

				if (!delete.acknowledged()) {
					log.errorf(
						"Error deleting index %s, cluster didn't acknowledge",
						indexName
					);

					throw new DeleteIndexException("not acknowledged");
				}

				deleteIndexTemplate(indexName);

				return null;

			}
			catch (DeleteIndexException k9Exception) {
				throw k9Exception;
			}
			catch (Exception e) {
				log.errorf(e, "Error deleting index %s", indexName.value());

				throw new DeleteIndexException(e);
			}

		});
	}

	public Uni<Void> deleteIndices(Set<IndexName> indexNames) {

		return VertxContextSupport.executeBlocking(() -> {

			var indices = indexNames.stream()
				.map(IndexName::toString)
				.toList();

			var acknowledgedResponse = openSearchClient.indices()
				.delete(req -> req
					.index(indices)
					.ignoreUnavailable(true)
				);

			if (!acknowledgedResponse.acknowledged()) {
				throw new DeleteIndexException(String.format(
					"Error deleting indices: %s", indices));
			}

			for (IndexName indexName : indexNames) {
				deleteIndexTemplate(indexName);
			}

			return null;
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
							log.error("Error while checking index exist", t);
							return false;
						}

						return exist;

					})
					.map(exist -> Tuple2.of(exist, indexName));
			existIndexNames.add(existIndexName);
		}

		return Uni.join()
			.all(existIndexNames)
			.usingConcurrencyOf(1)
			.andCollectFailures();

	}

	public Uni<List<String>> getDocumentTypes(IndexName indexName) {

		SearchRequest searchRequest = new SearchRequest(indexName.toString());

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.size(0);

		searchSourceBuilder.aggregation(AggregationBuilders
			.terms("documentTypes")
			.field("documentTypes.keyword")
			.size(1000)
		);

		searchRequest.source(searchSourceBuilder);


		return VertxContextSupport.executeBlocking(() -> {
			var searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

			return searchResponse
				.getAggregations()
				.<Terms>get("documentTypes")
				.getBuckets()
				.stream()
				.map(MultiBucketsAggregation.Bucket::getKeyAsString)
				.toList();
		});

	}

	public Uni<Map<String, Object>> getMappings(IndexName indexName) {
		return Uni
			.createFrom()
			.item(() -> {
				try {
					return restHighLevelClient.indices().getMapping(
						new GetMappingsRequest().indices(indexName.toString()),
						RequestOptions.DEFAULT
					);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(response -> response.mappings()
				.get(indexName.toString())
				.sourceAsMap()
			);
	}

	public Uni<List<CatResponse>> get_catIndices(Collection<String> indexNames) {
		return get_catIndices(indexNames.toArray(String[]::new));
	}

	public Uni<String> getSettings(IndexName indexName) {
		return Uni
			.createFrom()
			.item(() -> {
				try {
					return restHighLevelClient.indices().getSettings(
						new GetSettingsRequest().indices(indexName.toString()),
						RequestOptions.DEFAULT
					);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(response -> response.getIndexToSettings()
				.get(indexName.toString())
				.toString());
	}

	private void deleteIndexTemplate(IndexName indexName) {
		// delete index-template (best-effort)
		var indexTemplateName = indexName + TEMPLATE_SUFFIX;
		try {
			openSearchClient.indices().deleteIndexTemplate(req -> req
				.name(indexTemplateName));
		}
		catch (Exception e) {
			log.warnf(e, "Error deleting index-template %s", indexTemplateName);
		}
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
			throw new IllegalStateException(
				"Opensearch didn't return the [Content-Type] header, unable to parse response body");
		}

		var mediaTypeValue = entity.getContentType().getValue();
		if (mediaTypeValue != null &&
			(mediaTypeValue = mediaTypeValue.toLowerCase(Locale.ROOT)).contains("vnd.opensearch")) {
			mediaTypeValue = mediaTypeValue.replaceAll("vnd.opensearch\\+", "").replaceAll(
				"\\s*;\\s*compatible-with=\\d+",
				""
			);
		}
		MediaType mediaType = MediaTypeRegistry.fromMediaType(mediaTypeValue);
		if (mediaType == null) {
			throw new IllegalStateException("Unsupported Content-Type: " + mediaTypeValue);
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

				// synchronous call on worker thread
				return VertxContextSupport.executeBlocking(() -> {
						RestClient lowLevelClient = restHighLevelClient.getLowLevelClient();

						Request
							catRequest = new Request("GET", "/_cat/indices/" + indexName);

						catRequest.addParameter("format", "JSON");
						catRequest.addParameter("v", "true");
						catRequest.addParameter("bytes", "b");

						return lowLevelClient.performRequest(catRequest);
					})
					.map(IndexService::responseToCatResponses);
			});
	}

	public Uni<CatResponse> get_catIndicesFirst(String indexName) {
		return get_catIndices(indexName).map(catResponses -> {
			if (catResponses.isEmpty()) {
				return null;
			}
			return catResponses.get(0);
		});
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
					log.error("Error getting index count", throwable);
					return Uni.createFrom().nullItem();
				}
				return Uni.createFrom().item(countResponse.getCount());
			});
	}

	public Uni<Boolean> indexExist(String name) {
		// synchronous call on worker thread
		return VertxContextSupport.executeBlocking(() ->
				restHighLevelClient
					.indices()
					.exists(new GetIndexRequest(name), RequestOptions.DEFAULT)
			)
			.onItemOrFailure()
			.transformToUni((response, throwable) -> {
				if (throwable != null) {
					log.error("Error getting index exist", throwable);
					return Uni.createFrom().nullItem();
				}
				return Uni.createFrom().item(response);
			});
	}

	public Uni<Void> putComponentTemplate(PutComponentTemplateRequest putComponentTemplateRequest) {

		var componentTemplateName = putComponentTemplateRequest.name();

		var cluster = openSearchClient.cluster();

		return VertxContextSupport.executeBlocking(() -> {
			try {
				var response =
					cluster.putComponentTemplate(putComponentTemplateRequest);

				if (response.acknowledged()) {
					if (log.isDebugEnabled()) {
						log.debugf(
							"componentTemplate %s successfully created.",
							componentTemplateName
						);
					}

					return null;
				}
				else {
					log.errorf(
						"Cluster didn't acknowledge the operation for componentTemplate %s",
						componentTemplateName
					);

					throw new CannotCreateComponentTemplateException("not acknowledged");
				}
			}
			catch (IOException e) {
				log.errorf(
					e,
					"Error when trying to create a componentTemplate %s.",
					componentTemplateName
				);

				throw new CannotCreateComponentTemplateException(e);
			}
		});
	}

}
