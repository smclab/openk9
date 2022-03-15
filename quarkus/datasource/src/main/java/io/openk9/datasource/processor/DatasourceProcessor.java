package io.openk9.datasource.processor;


import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.processor.payload.DatasourceContext;
import io.openk9.datasource.processor.payload.IngestionDatasourcePayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
@Startup
public class DatasourceProcessor {

	@PostConstruct
	public void start() {
		_cancellable = ingestionMulti
			.onItem().call((message) -> {

				Object obj = message.getPayload();

				JsonObject jsonObject =
					obj instanceof JsonObject
						? (JsonObject) obj
						: new JsonObject(new String((byte[]) obj));

				long datasourceId = jsonObject.getLong("datasourceId");

				Uni<Datasource> datasourceUni =
					Datasource.findById(datasourceId);

				return datasourceUni
					.chain(datasource ->
						_getEnrichPipelineByDatasourceId(
							datasource.getDatasourceId())
							.chain(enrichPipeline -> {

								Uni<List<EnrichItem>> enrichItemUni;

								if (enrichPipeline.getEnrichPipelineId() !=
									null) {

									enrichItemUni = EnrichItem
										.findByEnrichPipelineId(
											enrichPipeline.getEnrichPipelineId())
										.onItem()
										.ifNull()
										.continueWith(List::of);

								}
								else {
									enrichItemUni =
										Uni.createFrom().item(List.of());
								}

								return Uni
									.combine()
									.all()
									.unis(
										Tenant.findById(
											datasource.getTenantId()),
										enrichItemUni)
									.combinedWith(
										(tenantObj, enrichItemList) -> {

											Tenant tenant =
												(Tenant) tenantObj;

											IngestionPayload
												ingestionPayload =
												jsonObject.mapTo(
													IngestionPayload.class);

											ingestionPayload.setTenantId(
												tenant.getTenantId());

											DatasourceContext
												datasourceContext =
												DatasourceContext.of(
													datasource, tenant,
													enrichPipeline,
													enrichItemList
												);

											return IngestionDatasourcePayload.of(
												ingestionPayload,
												datasourceContext);
										});

							}))
					.call(() -> Datasource
						.<Datasource>findById(datasourceId)
						.flatMap(datasource -> {

							datasource.setLastIngestionDate(
								Instant.ofEpochMilli(
									jsonObject.getLong("parsingDate")));

							return Panache.withTransaction(datasource::persistAndFlush);

						})
					)
					.onFailure().invoke(t -> logger.error(t.getMessage(), t))
					.call(ingestionDatasourceEmitter::send)
					.eventually(message::ack);
			})
			.subscribe()
			.with(message -> {});
	}

	private Uni<EnrichPipeline> _getEnrichPipelineByDatasourceId(
		long datasourceId) {
		return EnrichPipeline
			.findByDatasourceId(datasourceId)
			.onItem()
			.ifNull()
			.continueWith(EnrichPipeline::new);
	}

	@PreDestroy
	public void stop() {
		_cancellable.cancel();
	}

	private Cancellable _cancellable;

	@Inject
	Logger logger;

	@Inject
	@Channel("ingestion-datasource")
	MutinyEmitter<IngestionDatasourcePayload> ingestionDatasourceEmitter;

	@Inject
	@Channel("ingestion")
	Multi<Message<?>> ingestionMulti;


}
