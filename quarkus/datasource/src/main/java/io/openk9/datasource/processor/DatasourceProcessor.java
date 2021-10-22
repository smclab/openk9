package io.openk9.datasource.processor;


import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.processor.payload.DatasourceContext;
import io.openk9.datasource.processor.payload.IngestionDatasourcePayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Path;
import java.util.List;

@ApplicationScoped
@Path("/test")
public class DatasourceProcessor {

	@Channel("datasource")
	Emitter<IngestionDatasourcePayload> datasourceEmitter;

	@Channel("ingestion")
	Multi<JsonObject> _ingestionQueue;

	@PostConstruct
	public void process() {
		_disposable = _ingestionQueue
			.flatMap(jsonObject -> {

				IngestionPayload ingestionPayload =
					jsonObject.mapTo(IngestionPayload.class);

				long datasourceId = ingestionPayload.getDatasourceId();

				Uni<Datasource> datasourceUni =
					Datasource.findById(datasourceId);

				Uni<Tuple2<EnrichPipeline, List<EnrichItem>>> t2 =
					EnrichPipeline
						.findByDatasourceId(datasourceId)
						.flatMap(enrichPipeline ->
							EnrichItem.findByEnrichPipelineId(
									enrichPipeline.getEnrichPipelineId())
								.map(enrichItems -> Tuple2.of(
									enrichPipeline, enrichItems))
						);

				Uni<Tenant> tenantUni =
					Tenant.findById(ingestionPayload.getTenantId());

				return Uni
					.combine()
					.all()
					.unis(datasourceUni, tenantUni, t2)
					.combinedWith((item1, item2, item3) -> DatasourceContext.of(
							item1, item2, item3.getItem1(), item3.getItem2()
						)
					)
					.map(datasourceContext -> IngestionDatasourcePayload.of(
						ingestionPayload, datasourceContext))
					.toMulti();
			})
			.subscribe()
			.with(datasourceEmitter::send);

	}

	@PreDestroy
	public void destroy() {
		_disposable.cancel();
	}

	private Cancellable _disposable;


}
