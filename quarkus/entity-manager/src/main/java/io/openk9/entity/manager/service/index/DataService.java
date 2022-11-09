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

package io.openk9.entity.manager.service.index;

import io.openk9.entity.manager.cache.model.IngestionEntity;
import io.openk9.entity.manager.model.index.DataEntityIndex;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class DataService {

	public boolean associateEntities(
		String tenantId, String currentIndexName, String ingestionId,
		List<IngestionEntity> ingestionEntities)
		throws IOException {

		MatchQueryBuilder query =
			QueryBuilders.matchQuery("ingestionId.keyword", ingestionId);

		SearchResponse searchResponse = search(currentIndexName, query);

		for (SearchHit hit : searchResponse.getHits()) {
			String indexName = hit.getIndex();
			String id = hit.getId();
			JsonObject dataDocument = new JsonObject(hit.getSourceAsString());

			JsonArray entities = dataDocument.getJsonArray("entities");

			if (entities == null) {
				entities = new JsonArray();
			}

			ingestionEntities
				.stream()
				.map(entity -> DataEntityIndex.of(
					entity.getId(), entity.getType(), entity.getContext()))
				.map(JsonObject::mapFrom)
				.forEach(entities::add);

			dataDocument.put("entities", entities);

			UpdateRequest updateRequest = new UpdateRequest(indexName, id);

			updateRequest.doc(dataDocument.toString(), XContentType.JSON);

			updateRequest.setRefreshPolicy(
				WriteRequest.RefreshPolicy.WAIT_UNTIL);

			UpdateResponse update =
				_restHighLevelClient.update(
					updateRequest,
					RequestOptions.DEFAULT);

			if (_logger.isDebugEnabled()) {
				_logger.debug(update.toString());
			}

			return true;

		}

		return false;

	}

	private SearchResponse search(
		String currentIndexName, QueryBuilder queryBuilder) throws IOException {

		SearchRequest searchRequest = new SearchRequest(currentIndexName);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);
		searchRequest.source(searchSourceBuilder);

		return _restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

	}

	@Inject
	RestHighLevelClient _restHighLevelClient;

	@Inject
	IndexerBus _indexerBus;

	@Inject
	Logger _logger;

}
