package io.openk9.datasource.processor.indexwriter;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.EnrichPipelinePayload;
import io.openk9.datasource.util.MessageUtil;
import io.openk9.datasource.util.UniActionListener;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class IndexerProcessor {

	@Incoming("index-writer")
	@ActivateRequestContext
	public Uni<Void> process(Message<?> message) {

		EnrichPipelinePayload enrichPipelinePayload =
			MessageUtil.toObj(message, EnrichPipelinePayload.class);

		DataPayload payload = enrichPipelinePayload.getPayload();

		long datasourceId = payload.getDatasourceId();

		if (logger.isInfoEnabled()) {
			logger.info(
				"process message for datasourceId: " + datasourceId +
				" ingestionId: " + payload.getIngestionId());
		}

		return _indexPayload(payload, _getIndexName(datasourceId))
			.onItemOrFailure()
			.transformToUni((response, throwable) -> {

				if (response != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Index Response: " + response);
					}
				}

				if (throwable != null) {
					logger.error("Error indexing payload", throwable);
					return Uni.createFrom().completionStage(() -> message.nack(throwable));
				}
				else {
					return Uni.createFrom().completionStage(message::ack);
				}

			});

	}

	private Uni<DocWriteRequest<?>> _getDocWriteRequest(
		DataPayload dataPayload, String indexName) {

		return Uni.createFrom().deferred(() -> {

			SearchRequest searchRequest = new SearchRequest(indexName);

			MatchQueryBuilder matchQueryBuilder =
				QueryBuilders.matchQuery("contentId", dataPayload.getContentId());

			SearchSourceBuilder searchSourceBuilder =
				new SearchSourceBuilder();

			searchSourceBuilder.query(matchQueryBuilder);

			searchRequest.source(searchSourceBuilder);

			return Uni.createFrom().<SearchResponse>emitter(
				(sink) -> client.searchAsync(
					searchRequest, RequestOptions.DEFAULT,
					UniActionListener.of(sink)
				)
			);

		})
			.onItemOrFailure()
			.transform((searchResponse, throwable) -> {

				if (throwable != null) {
					logger.error(throwable.getMessage());
				}

				IndexRequest indexRequest = new IndexRequest(indexName);

				if (searchResponse != null && searchResponse.getHits().getHits().length > 0) {

					logger.info("found document for contentId: " + dataPayload.getContentId());

					String documentId = searchResponse.getHits().getAt(0).getId();

					String[] documentTypes = dataPayload.getDocumentTypes();

					if (documentTypes == null || documentTypes.length == 0) {

						logger.info("delete document for contentId: " + dataPayload.getContentId());

						return new DeleteRequest(indexName, documentId);
					}

					indexRequest.id(documentId);
				}

				logger.info("index document for contentId: " + dataPayload.getContentId());

				JsonObject jsonObject = JsonObject.mapFrom(dataPayload);

				indexRequest.source(jsonObject.toString(), XContentType.JSON);

				return indexRequest;

			});

	}


	private Uni<?> _indexPayload(
		DataPayload payload, Uni<DataIndex> indexNameUni) {

		return indexNameUni
			.call(indexName -> _getDocWriteRequest(payload, indexName.getName())
				.flatMap(docWriteRequest -> Uni.createFrom().emitter(sink -> {

					BulkRequest bulkRequest = new BulkRequest();

					bulkRequest.add(docWriteRequest);

					bulkRequest.setRefreshPolicy(
						WriteRequest.RefreshPolicy.WAIT_UNTIL);

					client.bulkAsync(
						bulkRequest,
						RequestOptions.DEFAULT,
						UniActionListener.of(sink));

					})
				)
			)
			.invoke(dataIndex -> {

				String[] documentTypes = payload.getDocumentTypes();

				indexerEvents.sendEvent(dataIndex, List.of(documentTypes));

			});
	}

	private Uni<DataIndex> _getIndexName(long datasourceId) {

		return sessionFactory.withTransaction(
			s -> Uni.createFrom().deferred(() ->
				s
					.find(Datasource.class, datasourceId)
					.onItem()
					.ifNotNull()
					.transformToUni(d ->
						Mutiny2
							.fetch(s, d.getDataIndex())
							.flatMap(di -> {
								if (di == null) {

									String indexName =
										d.getId() + "-data-" +
										UUID.randomUUID();

									DataIndex dataIndex = DataIndex.of(
										indexName, "auto-generated",
										new ArrayList<>());

									d.setDataIndex(dataIndex);

									return s
										.persist(d)
										.map(__ -> dataIndex)
										.call(s::flush)
										.invoke(persistedDataIndex -> {
											if (logger.isInfoEnabled()) {
												logger.info(
													"creating index: " + persistedDataIndex);
											}
										});
								}
								else {
									return Uni.createFrom().item(di);
								}
							})
					)
			)
		);
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	Logger logger;

	@Inject
	IndexerEvents indexerEvents;

	@Inject
	Mutiny.SessionFactory sessionFactory;

}
