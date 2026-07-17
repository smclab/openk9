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

package io.openk9.resources.validator;

import io.openk9.resources.validator.dto.BinaryPayload;
import io.openk9.resources.validator.dto.DataPayload;
import io.openk9.resources.validator.dto.ResourcesValidatorDataPayload;
import io.quarkus.runtime.Startup;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.opensearch.index.query.QueryBuilders.existsQuery;
import static org.opensearch.index.query.QueryBuilders.matchQuery;

@ApplicationScoped
@Startup
public class ResourcesValidatorProcessor {

	public JsonObject consume(ResourcesValidatorDataPayload resourcesValidatorPayload) {


		String replyTo = resourcesValidatorPayload.getReplyTo();

		DataPayload payload = resourcesValidatorPayload.getPayload();

		String indexName = payload.getIndexName();

		Long datasourceId = payload.getDatasourceId();

		String contentId = payload.getContentId();

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.must(matchQuery("datasourceId", datasourceId));

		boolQueryBuilder.must(matchQuery("contentId", contentId));

		boolQueryBuilder.must(existsQuery("hashCodes"));

		SearchRequest searchRequest = new SearchRequest(indexName);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.query(boolQueryBuilder);

		searchSourceBuilder.fetchField("hashCodes");

		searchRequest.source(searchSourceBuilder);

		List<BinaryPayload> binaries =
			payload
				.getResources().getBinaries();

		String rawContent = payload.getRawContent();

		List<Integer> hashCodes = _getHashCodes(rawContent, binaries);

		try {

			GetIndexRequest request = new GetIndexRequest(indexName);

			boolean exists = restHighLevelClient.indices().exists(request,
				RequestOptions.DEFAULT);

			if (exists) {

				SearchResponse searchResponse =
					restHighLevelClient.search(
						searchRequest, RequestOptions.DEFAULT);

				for (SearchHit hit : searchResponse.getHits()) {

					Map<String, Object> sourceAsMap = hit.getSourceAsMap();

					Object documentHashCodes = sourceAsMap.get("hashCodes");

					if (documentHashCodes instanceof Collection) {

						Collection<Integer> documentHashCodesList =
							(Collection<Integer>) documentHashCodes;

						if (hashCodes.size() == documentHashCodesList.size() &&
							hashCodes.containsAll(documentHashCodesList)) {

							logger.infof(
								"Duplicate content '%s' already indexed, dropping message",
								contentId);

							return JsonObject.of("_openk9SkipDocument", true);
						}
					}
				}
			}
			else {
				logger.debugf(
					"Index '%s' does not exist yet, passing item to next enrich step",
					indexName);
			}

			return JsonObject.of("hashCodes", hashCodes, "_openk9SkipDocument", false);

		}
		catch (IOException e) {
			logger.errorf(e, "Failed to process resource '%s'", contentId);
			throw new RuntimeException();
		}

	}

	private List<Integer> _getHashCodes(String rawContent,  List<BinaryPayload> binaries) {

		if (rawContent == null && binaries == null) {
			return List.of();
		}

		List<Integer> hashCodes = new ArrayList<>();

		if (rawContent != null) {
			hashCodes.add(rawContent.hashCode());
		}

		if (binaries != null) {

			for (BinaryPayload binaryPayload : binaries) {

				String url = binaryPayload.getUrl();

				try (InputStream inputStream = openStream(url)) {

					byte[] sourceBytes = IOUtils.toByteArray(inputStream);

					String encodedString =
						Base64.getEncoder().encodeToString(sourceBytes);

					hashCodes.add(encodedString.hashCode());
				}
				catch (Exception e) {
					logger.errorf(
						e, "Failed to read binary while computing hash codes");
				}
			}

		}

		return hashCodes;
	}

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	Logger logger;

	// The binary is fetched from the pre-signed URL injected by the datasource;
	// the URL is a bearer credential and must never be logged.
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	private static InputStream openStream(String url) throws Exception {
		HttpResponse<InputStream> response = HTTP_CLIENT.send(
			HttpRequest.newBuilder(URI.create(url)).GET().build(),
			HttpResponse.BodyHandlers.ofInputStream());

		if (response.statusCode() >= 300) {
			throw new IllegalStateException(
				"Binary fetch failed with HTTP " + response.statusCode());
		}

		return response.body();
	}

}
