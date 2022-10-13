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
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
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

	private Uni<?> _indexPayload(
		DataPayload payload, Uni<DataIndex> indexNameUni) {

		return indexNameUni
			.call(dataIndex -> Uni.createFrom().emitter(sink -> {

				IndexRequest indexRequest = new IndexRequest(dataIndex.getName());

				JsonObject jsonObject = JsonObject.mapFrom(payload);

				indexRequest.source(
					jsonObject.toString(),
					XContentType.JSON
				);

				indexRequest.setRefreshPolicy(
					WriteRequest.RefreshPolicy.WAIT_UNTIL);

				client.indexAsync(
					indexRequest,
					RequestOptions.DEFAULT,
					UniActionListener.of(sink));

			}))
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
										new ArrayList<>(), d);

									d.setDataIndex(dataIndex);

									dataIndex.setDatasource(d);

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
