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

import io.openk9.entity.manager.model.index.EntityIndex;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EntityService {

	public EntityIndex get(long tenantId, long id) throws IOException {

		GetRequest getRequest = new GetRequest(
			tenantId + "-entity", Long.toString(id));

		GetResponse getResponse = _restHighLevelClient.get(
			getRequest, RequestOptions.DEFAULT);

		if (getResponse.isExists()) {
			String sourceAsString = getResponse.getSourceAsString();
			JsonObject json = new JsonObject(sourceAsString);
			return json.mapTo(EntityIndex.class);
		}
		return null;
	}

	public void index(EntityIndex entity) throws IOException {

		IndexRequest request =
			new IndexRequest(entity.getTenantId() + "-entity");

		String entityId = entity.getId();

		request.id(entityId);

		Map<String, Object> json = new HashMap<>();

		json.put("name", entity.getName());
		json.put("tenantId", entity.getTenantId());
		json.put("id", entityId);
		json.put("type", entity.getType());
		json.put("graphId", entity.getGraphId());

		request.source(json, XContentType.JSON);

		_indexerBus.emit(request);

	}

	public void awaitIndex(EntityIndex entity) throws IOException {

		IndexRequest request =
			new IndexRequest(entity.getTenantId() + "-entity");

		String entityId = entity.getId();

		request.id(entityId);

		Map<String, Object> json = new HashMap<>();

		json.put("name", entity.getName());
		json.put("tenantId", entity.getTenantId());
		json.put("id", entityId);
		json.put("type", entity.getType());
		json.put("graphId", entity.getGraphId());

		request.source(json, XContentType.JSON);

		request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

		IndexResponse index =
			_restHighLevelClient.index(request, RequestOptions.DEFAULT);

		if (_logger.isDebugEnabled()) {
			_logger.debug(index);
		}

	}

	public EntityIndex searchByNameAndType(
		String tenantId, String name, String type) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.must(QueryBuilders.matchQuery("name", name));
		boolQueryBuilder.must(QueryBuilders.matchQuery("type", type));

		List<EntityIndex> search = search(tenantId, boolQueryBuilder, 0, 1);

		if (search.isEmpty()) {
			return null;
		}

		return search.get(0);
	}

	public List<EntityIndex> search(String tenantId, QueryBuilder queryBuilder, int from, int size) {

		SearchRequest searchRequest = new SearchRequest(tenantId + "-entity");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(queryBuilder);
		searchSourceBuilder.size(from);
		searchSourceBuilder.size(size);
		searchRequest.source(searchSourceBuilder);

		try {

			SearchResponse searchResponse =
				_restHighLevelClient.search(searchRequest,
					RequestOptions.DEFAULT);
			SearchHits hits = searchResponse.getHits();
			List<EntityIndex> results = new ArrayList<>(hits.getHits().length);
			for (SearchHit hit : hits.getHits()) {
				String sourceAsString = hit.getSourceAsString();
				JsonObject json = new JsonObject(sourceAsString);
				EntityIndex entityIndex = json.mapTo(EntityIndex.class);
				entityIndex.setScore(hit.getScore());
				results.add(entityIndex);
			}
			return results;
		}
		catch (Exception e) {
			_logger.error(e.getMessage());
		}
		return List.of();

	}

	@Inject
	RestHighLevelClient _restHighLevelClient;

	@Inject
	IndexerBus _indexerBus;

	@Inject
	Logger _logger;

}
