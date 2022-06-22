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

package io.openk9.datasource.event.repo;

import io.openk9.datasource.event.config.EventConfig;
import io.openk9.datasource.event.model.Event;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EventRepositoryImpl implements EventRepository {

	@PostConstruct
	public void init() {

		many =
			Sinks
				.unsafe()
				.many()
				.unicast()
				.onBackpressureBuffer();

		_disposable = many
			.asFlux()
			.bufferTimeout(100, Duration.ofSeconds(5))
			.flatMap(indexRequests -> {

				BulkRequest bulkRequest = new BulkRequest();

				for (IndexRequest indexRequest : indexRequests) {
					bulkRequest.add(indexRequest);
				}

				return Mono.create(sink -> client.bulkAsync(
					bulkRequest, RequestOptions.DEFAULT,
					new ActionListener<>() {
						@Override
						public void onResponse(BulkResponse bulkItemResponses) {
							sink.success(bulkItemResponses);
						}

						@Override
						public void onFailure(Exception e) {
							sink.error(e);
						}
					}));
			})
			.subscribe();

	}

	@PreDestroy
	public void shutdown() {
		_disposable.dispose();
		many.tryEmitComplete();
	}

	@Override
	public void batchSave(Event event) {
		many.tryEmitNext(_getIndexRequest(event));
	}

	@Override
	public void syncSave(Event event) throws IOException {

		IndexRequest indexRequest = _getIndexRequest(event);

		client.index(indexRequest, RequestOptions.DEFAULT);

	}

	@Override
	public Uni<Void> asyncSave(Event event) {

		IndexRequest indexRequest = _getIndexRequest(event);

		return Uni
			.createFrom()
			.emitter(emitter -> client.indexAsync(
				indexRequest, RequestOptions.DEFAULT,
				new ActionListener<>() {
					@Override
					public void onResponse(IndexResponse indexResponse) {
						emitter.complete(indexResponse);
					}

					@Override
					public void onFailure(Exception e) {
						emitter.fail(e);
					}
				}))
			.replaceWithVoid();
	}

	@Override
	public Uni<LocalDateTime> findLastIngestionDate(
		String className, String classPK) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.must(
			QueryBuilders.matchQuery("className.keyword", className));
		boolQueryBuilder.must(
			QueryBuilders.matchQuery("classPK.keyword", classPK));

		SearchRequest searchRequest = new SearchRequest(config.getIndexName());

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.sort("created", SortOrder.DESC);
		searchSourceBuilder.fetchSource("parsingDate", null);
		searchSourceBuilder.size(1);

		searchRequest.source(searchSourceBuilder);

		return Uni
			.createFrom()
			.<LocalDateTime>emitter(emitter -> client.searchAsync(
			searchRequest, RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(SearchResponse searchResponse) {

					if (searchResponse.getHits().getHits().length > 0) {

						SearchHit searchHit = searchResponse.getHits().getAt(0);

						EventParsingDate eventParsingDate =
							Json.decodeValue(
								searchHit.getSourceAsString(),
								EventParsingDate.class);

						emitter.complete(eventParsingDate.getParsingDate());

					}
					else {
						emitter.complete(null);
					}

				}

				@Override
				public void onFailure(Exception e) {
					emitter.fail(e);
				}
			}
		))
			.replaceIfNullWith(() -> LocalDateTime.of(1970, 1, 1, 0, 0, 0));

	}

	@Override
	public Uni<Event> findById(String id) {
		return findById(id, null);
	}

	@Override
	public Uni<Event> findById(String id, String[] includeFields) {

		SearchRequest searchRequest = new SearchRequest(config.getIndexName());

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.query(QueryBuilders.matchQuery("id", id));

		searchSourceBuilder.size(1);
		searchSourceBuilder.fetchSource(includeFields, null);

		searchRequest.source(searchSourceBuilder);

		return Uni.createFrom().emitter(emitter -> client.searchAsync(
			searchRequest, RequestOptions.DEFAULT,
			new ActionListener<>() {
				@Override
				public void onResponse(SearchResponse searchResponse) {

					if (searchResponse.getHits().getHits().length > 0) {

						SearchHit searchHit = searchResponse.getHits().getAt(0);

						Event event =
							Json.decodeValue(
								searchHit.getSourceAsString(),
								Event.class);

						emitter.complete(event);

					}
					else {

						emitter.complete(null);

					}

				}

				@Override
				public void onFailure(Exception e) {
					emitter.fail(e);
				}
			}));
	}

	@Override
	public Uni<List<Event>> search(SearchSourceBuilder searchSourceBuilder) {

		SearchRequest searchRequest = new SearchRequest(config.getIndexName());

		searchRequest.source(searchSourceBuilder);

		return Uni.createFrom().emitter(emitter -> client.searchAsync(
			searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {

				@Override
				public void onResponse(SearchResponse searchResponse) {
					SearchHits hits = searchResponse.getHits();

					List<Event> events = new ArrayList<>();

					for (SearchHit searchHit : hits) {
						Event event = Json.decodeValue(
							searchHit.getSourceAsString(),
							Event.class);
						events.add(event);
					}

					emitter.complete(events);

				}

				@Override
				public void onFailure(Exception e) {
					emitter.fail(e);
				}

			}));
	}

	private IndexRequest _getIndexRequest(Event event) {
		IndexRequest indexRequest = new IndexRequest(config.getIndexName());
		return indexRequest.source(Json.encode(event), XContentType.JSON);
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	EventConfig config;

	private Sinks.Many<IndexRequest> many;

	private Disposable _disposable;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class EventParsingDate {
		private LocalDateTime parsingDate;
	}

}
