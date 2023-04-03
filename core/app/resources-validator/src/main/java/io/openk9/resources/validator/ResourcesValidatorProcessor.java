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

import io.openk9.resources.validator.client.datasource.DatasourceClient;
import io.openk9.resources.validator.client.filemanager.FileManagerClient;
import io.openk9.resources.validator.dto.BinaryPayload;
import io.openk9.resources.validator.dto.DataPayload;
import io.openk9.resources.validator.dto.ResourcesValidatorDataPayload;
import io.quarkus.runtime.Startup;
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

	public void consume(ResourcesValidatorDataPayload resourcesValidatorPayload) {


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

							datasourceClient.sentToPipeline(replyTo, "{}");

							logger.info("Send message to datasource with token: " + replyTo);

							return;
						}

					}

				}
			}
			else {
				logger.info("Index wit name: " + indexName + " not exist. Item go to next enrich step.");
			}

			payload.setHashCodes(hashCodes);

			datasourceClient.sentToPipeline(replyTo, payload.toString());

			logger.info("Send message to datasource with token: " + replyTo);

		}
		catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

	}

	private List<Integer> _getHashCodes(String rawContent,  List<BinaryPayload> binaries, String schemaName) {

		if (rawContent == null && (binaries == null || binaries.isEmpty())) {
			return List.of();
		}

		List<Integer> hashCodes = new ArrayList<>();

		if (binaries != null) {

			binaries.forEach(binaryPayload -> {

				try {
					String resourceId = binaryPayload.getResourceId();

					InputStream inputStream =
						fileManagerClient.download(resourceId, schemaName);

					byte[] sourceBytes;

					try {
						sourceBytes = IOUtils.toByteArray(inputStream);
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}

					String encodedString =
						Base64.getEncoder().encodeToString(sourceBytes);

					hashCodes.add(encodedString.hashCode());
				}
				catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

			});
		}

		if (rawContent != null) {
			hashCodes.add(rawContent.hashCode());
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

	@Inject
	@RestClient
	DatasourceClient datasourceClient;

}
