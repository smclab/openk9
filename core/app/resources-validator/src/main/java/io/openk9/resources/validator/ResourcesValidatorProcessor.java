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

import io.openk9.resources.validator.client.filemanager.FileManagerClient;
import io.openk9.resources.validator.dto.BinaryPayload;
import io.openk9.resources.validator.dto.DataPayload;
import io.openk9.resources.validator.dto.ResourcesValidatorDataPayload;
import io.quarkus.runtime.Startup;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@ApplicationScoped
@Startup
public class ResourcesValidatorProcessor {

	public JsonObject consume(ResourcesValidatorDataPayload resourcesValidatorPayload) {


		String replyTo = resourcesValidatorPayload.getReplyTo();

		DataPayload payload = resourcesValidatorPayload.getPayload();

		String schemaName = payload.getTenantId();

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

		List<Integer> hashCodes = _getHashCodes(rawContent, binaries, schemaName);

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

							binaries.forEach(binaryPayload -> {

								String resourceId =
									binaryPayload.getResourceId();

								fileManagerClient.delete(
									resourceId, schemaName);
							});

							logger.info(
								"document found. dropped message with contentId: "
								+ contentId);

							return JsonObject.of("toIndex", true);
						}
					}
				}
			}
			else {
				logger.info("Index wit name: " + indexName + " not exist. Item go to next enrich step.");
			}

			return JsonObject.of("hashCodes", hashCodes, "toIndex", false);

		}
		catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException();
		}

	}

	private List<Integer> _getHashCodes(String rawContent,  List<BinaryPayload> binaries, String schemaName) {

		if (rawContent == null && binaries == null) {
			return List.of();
		}

		List<Integer> hashCodes = new ArrayList<>();

		if (rawContent != null) {
			hashCodes.add(rawContent.hashCode());
		}

		if (binaries != null) {

			for (BinaryPayload binaryPayload : binaries) {

				String resourceId = binaryPayload.getResourceId();

				try (InputStream inputStream =
						 fileManagerClient.download(resourceId, schemaName);) {

					byte[] sourceBytes = IOUtils.toByteArray(inputStream);

					String encodedString =
						Base64.getEncoder().encodeToString(sourceBytes);

					hashCodes.add(encodedString.hashCode());
				}
				catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		}

		return hashCodes;
	}

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	@RestClient
	FileManagerClient fileManagerClient;

	@Inject
	Logger logger;

}
