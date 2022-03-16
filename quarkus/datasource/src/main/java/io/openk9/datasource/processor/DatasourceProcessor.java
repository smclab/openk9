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
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Startup
public class DatasourceProcessor {

	@Incoming("ingestion")
	@Blocking
	public CompletionStage<Void> precess(Message<?> message) {

		try {

			Object obj = message.getPayload();

			JsonObject jsonObject =
				obj instanceof JsonObject
					? (JsonObject) obj
					: new JsonObject(new String((byte[]) obj));

			long datasourceId = jsonObject.getLong("datasourceId");

			Datasource datasource = _await(Datasource.findById(datasourceId));

			EnrichPipeline enrichPipeline =
				_await(
					EnrichPipeline
						.findByDatasourceId(datasourceId)
						.onItem()
						.ifNull()
						.continueWith(EnrichPipeline::new)
				);

			List<EnrichItem> enrichItemList;

			if (enrichPipeline.getEnrichPipelineId() != null) {

				enrichItemList = _await(
					EnrichItem
						.findByEnrichPipelineId(
							enrichPipeline.getEnrichPipelineId())
						.onItem()
						.ifNull()
						.continueWith(List::of)
				);

			}
			else {
				enrichItemList = List.of();
			}

			Tenant tenant = _await(Tenant.findById(datasource.getTenantId()));

			IngestionPayload ingestionPayload =
				jsonObject.mapTo(IngestionPayload.class);

			ingestionPayload.setTenantId(tenant.getTenantId());

			DatasourceContext datasourceContext =
				DatasourceContext.of(
					datasource, tenant,
					enrichPipeline,
					enrichItemList
				);

			IngestionDatasourcePayload ingestionDatasourcePayload =
				IngestionDatasourcePayload.of(
					ingestionPayload,
					datasourceContext);

			logger.info("persist: " + datasource);

			datasource.setLastIngestionDate(
				Instant.ofEpochMilli(
					jsonObject.getLong("parsingDate")));

			_await(Panache.withTransaction(datasource::persist));

			_await(Panache.flush());

			ingestionDatasourceEmitter.send(ingestionDatasourcePayload);

		}
		catch (Exception e) {
			return message.nack(e);
		}

		return message.ack();

	}

	private static <T> T _await(Uni<T> uni) {
		return uni.await().indefinitely();
	}

	@Inject
	Logger logger;

	@Inject
	@Channel("ingestion-datasource")
	Emitter<IngestionDatasourcePayload> ingestionDatasourceEmitter;

}
