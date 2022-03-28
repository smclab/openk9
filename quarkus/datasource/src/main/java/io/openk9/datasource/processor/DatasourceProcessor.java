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
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Startup
public class DatasourceProcessor {

	@ConsumeEvent(value = _EVENT_NAME)
	@ActivateRequestContext
	Uni<Void> consumeIngestionMessage(JsonObject jsonObject) {

		long datasourceId = jsonObject.getLong("datasourceId");

		Uni<Datasource> datasourceUni = Datasource.findById(datasourceId);

		return Panache.withTransaction(() ->
			datasourceUni
				.flatMap(datasource ->
					EnrichPipeline
						.findByDatasourceId(datasource.getDatasourceId())
						.onItem()
						.ifNull()
						.continueWith(EnrichPipeline::new)
						.flatMap(enrichPipeline -> {

							Uni<List<EnrichItem>> enrichItemUni;

							if (enrichPipeline.getEnrichPipelineId() != null) {

								enrichItemUni = EnrichItem
									.findByEnrichPipelineId(
										enrichPipeline.getEnrichPipelineId())
									.onItem()
									.ifNull()
									.continueWith(List::of);

							}
							else {
								enrichItemUni = Uni.createFrom().item(List.of());
							}

							return Uni
								.combine()
								.all()
								.unis(
									Tenant.findById(datasource.getTenantId()),
									enrichItemUni)
								.combinedWith((tenantObj, enrichItemList) -> {

									Tenant tenant = (Tenant)tenantObj;

									IngestionPayload ingestionPayload =
										jsonObject.mapTo(IngestionPayload.class);

									ingestionPayload.setTenantId(tenant.getTenantId());

									DatasourceContext datasourceContext = DatasourceContext.of(
										datasource, tenant, enrichPipeline, enrichItemList
									);

									return IngestionDatasourcePayload.of(
										ingestionPayload, datasourceContext);
								});

						}))
				.eventually(() -> Datasource
					.<Datasource>findById(datasourceId)
					.flatMap(datasource -> {

						datasource.setLastIngestionDate(
							Instant.ofEpochMilli(
								jsonObject.getLong("parsingDate")));

						return datasource.persist();

					})
				)
				.onItem().invoke(ingestionDatasourceEmitter::send)
				.replaceWithVoid()
		);

	}

	@Incoming("ingestion")
	public CompletionStage<Void> precess(Message<?> message) {

		Object obj = message.getPayload();

		JsonObject jsonObject =
			obj instanceof JsonObject
				? (JsonObject) obj
				: new JsonObject(new String((byte[]) obj));

		bus.requestAndForget(_EVENT_NAME, jsonObject);

		return message.ack();

	}

	private static <T> T _await(Uni<T> uni) {
		return uni.await().indefinitely();
	}

	@Inject
	EventBus bus;

	@Inject
	Logger logger;

	@Inject
	@Channel("ingestion-datasource")
	Emitter<IngestionDatasourcePayload> ingestionDatasourceEmitter;

	private static final String _EVENT_NAME = "handle_ingestion_event";

}
