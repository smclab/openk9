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
import java.net.HttpURLConnection;
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
import io.openk9.datasource.index.exception.IndexMappingException;
import io.openk9.datasource.index.exception.PutMappingException;
import io.openk9.datasource.index.exception.PutSettingsException;
import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.index.model.MappingsKey;
import io.openk9.datasource.index.response.CatResponse;
import io.openk9.datasource.util.UniActionListener;

import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.http.HttpEntity;
import org.jboss.logging.Logger;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.client.indices.GetMappingsRequest;
import org.opensearch.client.indices.PutComposableIndexTemplateRequest;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.cluster.PutComponentTemplateRequest;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.core.xcontent.MediaType;
import org.opensearch.core.xcontent.MediaTypeRegistry;
import org.opensearch.index.IndexNotFoundException;
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

	/**
	 * Reads the settings declared in the index template of an index, which are
	 * not necessarily the settings the index is running with.
	 *
	 * @param indexName the name of the index whose index template is read
	 * @return a {@link Uni} emitting the settings the index template declares,
	 * failing with an {@link IndexNotFoundException} when the index template
	 * does not exist
	 */
	public Uni<String> getIndexTemplateSettings(IndexName indexName) {
		var indexTemplateName = indexName + TEMPLATE_SUFFIX;

		return Uni
			.createFrom()
			.item(() -> {
				try {
					return openSearchClient.indices().getIndexTemplate(
						builder -> builder.name(indexTemplateName)
					);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(response -> response.indexTemplates().stream()
				.filter(indexTemplateItem -> indexTemplateItem.name().equals(indexTemplateName))
				.findFirst()
				.map(indexTemplateItem -> indexTemplateItem.indexTemplate().template())
				.map(indexTemplate -> asJson(indexTemplate.settings()))
				.orElseThrow(() -> new IndexNotFoundException(indexTemplateName))
			);
	}

	/**
	 * Renders the settings of an index template as JSON.
	 * <p>
	 * The client hands them over as a map whose values carry their own JSON,
	 * so printing the map yields {@code {index={"analysis":...}}}, which is
	 * neither JSON nor anything an editor can show.
	 *
	 * @param settings the settings as the client returns them
	 * @return the same settings as a JSON object
	 */
	private static String asJson(Map<String, ?> settings) {

		var json = new JsonObject();

		settings.forEach((key, value) ->
			json.put(key, Json.decodeValue(String.valueOf(value))));

		return json.encode();
	}

	private void deleteIndexTemplate(IndexName indexName) {
		// delete index-template (best-effort)
		var indexTemplateName = indexName + TEMPLATE_SUFFIX;
		try {
			openSearchClient.indices().deleteIndexTemplate(req -> req
				.name(indexTemplateName));
		}
		catch (OpenSearchException e) {
			log.debugf(e, "Index-template: %s was already deleted or does not exist", indexTemplateName);
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

	/**
	 * Applies the given mappings to a live index.
	 * <p>
	 * OpenSearch accepts additive changes and the few updatable parameters:
	 * {@code search_analyzer} can be changed on an existing field, while
	 * changing {@code type}, the index-time {@code analyzer}, {@code
	 * term_vector} or {@code index_options} is rejected. A rejection is
	 * reported as is, without being interpreted, and rejects the whole
	 * document: a single field that cannot be applied leaves the index
	 * untouched.
	 * <p>
	 * An index is materialized from its template at the first write, so before
	 * that there is no index to update: the operation then completes without
	 * doing anything, and the caller is expected to update the template, which
	 * in that state is the only thing that exists.
	 *
	 * @param indexName the live index to update
	 * @param mappings the mappings to apply
	 * @return a {@link Uni} that completes with {@code true} when the cluster
	 * acknowledges the operation and with {@code false} when the index does not
	 * exist yet, and fails with {@link PutMappingException} otherwise
	 */
	public Uni<Boolean> putMapping(
		IndexName indexName, Map<MappingsKey, Object> mappings) {

		return VertxContextSupport.executeBlocking(() -> {
			try {
				var response = sendRequest(
					"PUT",
					String.format("/%s/_mapping", indexName),
					Json.encode(mappings)
				);

				warnIfNotAcknowledged(response, "mapping", indexName);

				return Boolean.TRUE;
			}
			catch (Exception e) {
				if (isIndexNotFound(e)) {
					logIndexNotFound(indexName);

					return Boolean.FALSE;
				}

				log.errorf(e, "Cannot update the mapping of index %s", indexName);

				throw new PutMappingException(e);
			}
		});
	}

	/**
	 * Applies the given settings to a live index.
	 * <p>
	 * Static settings, {@code analysis.*} above all, are refused by OpenSearch
	 * while the index is open, and the refusal rejects the whole document, the
	 * dynamic keys included: {@code closeIndex} closes the index around the
	 * update and reopens it afterwards, even when the update fails. Closing an
	 * index makes it neither searchable nor writable for the duration of the
	 * operation.
	 * <p>
	 * An index is materialized from its template at the first write, so before
	 * that there is no index to update: the operation then completes without
	 * doing anything, and the caller is expected to update the template, which
	 * in that state is the only thing that exists.
	 *
	 * @param indexName the live index to update
	 * @param settings the settings to apply
	 * @param closeIndex whether to close and reopen the index around the update
	 * @return a {@link Uni} that completes with {@code true} when the cluster
	 * acknowledges the operation and with {@code false} when the index does not
	 * exist yet, and fails with {@link PutSettingsException} otherwise, the
	 * index being left closed included
	 */
	public Uni<Boolean> putSettings(
		IndexName indexName, Map<String, Object> settings, boolean closeIndex) {

		return VertxContextSupport.executeBlocking(() -> {

			var indices = openSearchClient.indices();
			var index = indexName.toString();

			boolean closed = false;
			boolean leftClosed = false;
			Exception failure = null;

			try {
				if (closeIndex) {
					indices.close(request -> request.index(index));

					closed = true;
				}

				var response = sendRequest(
					"PUT",
					String.format("/%s/_settings", index),
					Json.encode(settings)
				);

				warnIfNotAcknowledged(response, "settings", indexName);
			}
			catch (Exception e) {
				failure = e;
			}

			if (closed) {
				try {
					var reopened = indices.open(request -> request.index(index));

					// with the default parameters shardsAcknowledged follows the
					// primary shard being active, which is the condition for the
					// index to be searchable again
					if (!reopened.acknowledged() || !reopened.shardsAcknowledged()) {
						leftClosed = true;

						log.errorf(
							"Reopening index %s is not acknowledged", indexName);
					}
				}
				catch (Exception e) {
					leftClosed = true;

					log.errorf(e, "Index %s is left closed", indexName);

					if (failure == null) {
						failure = e;
					}
					else {
						failure.addSuppressed(e);
					}
				}
			}

			if (failure == null && !leftClosed) {
				return Boolean.TRUE;
			}

			if (failure != null && isIndexNotFound(failure)) {
				logIndexNotFound(indexName);

				return Boolean.FALSE;
			}

			if (leftClosed) {
				// the caller asked for a bounded downtime and has to be told
				// that it is not over, which is the urgent half of the failure
				var message = String.format(
					"the settings of index %s were %s and the index could not be "
						+ "reopened: it stays closed, and unsearchable, until it "
						+ "is opened again",
					indexName,
					failure == null ? "updated" : "not updated"
				);

				log.error(message, failure);

				throw new PutSettingsException(message, failure);
			}

			log.errorf(failure, "Cannot update the settings of index %s", indexName);

			throw new PutSettingsException(failure);
		});
	}

	/**
	 * Reads the settings a live index is running with, which are not
	 * necessarily the ones its index template declares.
	 *
	 * @param indexName the live index to read
	 * @return a {@link Uni} emitting the settings of the index, or {@code null}
	 * when the index does not exist yet, and failing with an
	 * {@link IndexMappingException} otherwise
	 */
	public Uni<JsonObject> getIndexSettings(IndexName indexName) {

		return VertxContextSupport.executeBlocking(() -> {
			try {
				var response = sendRequest(
					"GET",
					String.format("/%s/_settings", indexName),
					null
				);

				return new JsonObject(response)
					.getJsonObject(indexName.toString(), new JsonObject())
					.getJsonObject("settings", new JsonObject());
			}
			catch (Exception e) {
				if (isIndexNotFound(e)) {
					logIndexNotFound(indexName);

					return null;
				}

				log.errorf(e, "Cannot read the settings of index %s", indexName);

				throw new IndexMappingException(e);
			}
		});
	}

	/**
	 * Tells the one refusal a close can fix from every other one.
	 * <p>
	 * Unlike {@link #isIndexNotFound}, this reads the wording of the message,
	 * because OpenSearch answers 400 to every rejected settings update and
	 * which settings are static is knowledge only the engine has. It is the
	 * single place where a message is interpreted instead of being reported as
	 * is, and a change of wording costs a useless close, not a wrong result.
	 *
	 * @param failure the failure of a settings update
	 * @return {@code true} when the update was refused because the index is
	 * open
	 */
	public static boolean requiresClosedIndex(Throwable failure) {

		var message = failure.getMessage();

		return message != null && message.contains("non dynamic settings");
	}

	/**
	 * Tells the failure of a missing index from every other one, on the status
	 * OpenSearch answered with and not on the wording of the message.
	 * <p>
	 * The close of {@code putSettings} goes through the typed client, which
	 * reports it as an {@link OpenSearchException}, while everything that
	 * carries a document goes through {@link #sendRequest}.
	 */
	private static boolean isIndexNotFound(Exception e) {

		if (e instanceof IndexMappingException mappingException) {
			return mappingException.getStatus() == HttpURLConnection.HTTP_NOT_FOUND;
		}

		return e instanceof OpenSearchException openSearchException
			&& openSearchException.status() == HttpURLConnection.HTTP_NOT_FOUND;
	}

	private static boolean isAcknowledged(String responseBody) {
		return responseBody != null
			&& !responseBody.isBlank()
			&& new JsonObject(responseBody).getBoolean("acknowledged", Boolean.FALSE);
	}

	private static void logIndexNotFound(IndexName indexName) {
		log.infof(
			"Index %s does not exist yet, only its template is updated",
			indexName
		);
	}

	/**
	 * Warns that an update of the live index was not acknowledged, which means
	 * it was applied but not confirmed by every node within the timeout of the
	 * master: imprecise, not false, so it does not fail the operation.
	 */
	private static void warnIfNotAcknowledged(
		String responseBody, String what, IndexName indexName) {

		if (!isAcknowledged(responseBody)) {
			log.warnf(
				"Updating the %s of index %s is not acknowledged", what, indexName);
		}
	}

	/**
	 * Sends a request through the generic client, which carries the body as it
	 * is.
	 * <p>
	 * A typed request parses the document into its own model and serializes it
	 * back, so whatever the model does not cover is dropped without an error.
	 * Measured on opensearch-java 2.26.0: {@code IndexSettings} only reads
	 * {@code index.highlight.max_analyzed_offset} in its dotted form, and
	 * silently loses the nested one that {@code docTypesToSettings} produces.
	 * Settings also come from the caller as arbitrary JSON, so the model cannot
	 * be expected to cover them. Everything that carries a mappings or a
	 * settings document therefore goes through here, where the body is passed
	 * as it is.
	 *
	 * @param method the HTTP method
	 * @param endpoint the OpenSearch endpoint, leading slash included
	 * @param body the JSON body, or {@code null} when the request has none
	 * @return the response body
	 * @throws IndexMappingException if OpenSearch does not answer with a 2xx,
	 * reporting its own explanation as is
	 */
	private String sendRequest(String method, String endpoint, String body)
		throws IOException {

		var builder = Requests.builder()
			.endpoint(endpoint)
			.method(method);

		if (body != null) {
			builder.json(body);
		}

		try (var response = openSearchClient.generic().execute(builder.build())) {

			var status = response.getStatus();

			var responseBody = response.getBody()
				.map(content -> content.bodyAsString())
				.orElse("");

			if (status < 200 || status > 299) {
				throw new IndexMappingException(
					String.format(
						"%s %s returned %d: %s", method, endpoint, status, responseBody),
					status
				);
			}

			return responseBody;
		}
	}

}
