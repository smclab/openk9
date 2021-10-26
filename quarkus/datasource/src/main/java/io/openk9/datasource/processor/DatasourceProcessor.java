package io.openk9.datasource.processor;


import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.processor.payload.DatasourceContext;
import io.openk9.datasource.processor.payload.IngestionDatasourcePayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class DatasourceProcessor {

	@PersistenceContext
	private EntityManager em;

	@PostConstruct
	public void start() {

		_many = Sinks
			.unsafe()
			.many()
			.multicast()
			.onBackpressureBuffer();

		_disposable = _many
			.asFlux()
			.groupBy(Datasource::getDatasourceId)
			.flatMap(group -> group.sample(Duration.ofSeconds(30)))
			.subscribeOn(Schedulers.fromExecutor(managedExecutor))
			.subscribe(em::persist);

	}

	@PreDestroy
	public void stop() {
		_disposable.dispose();
		_many.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
	}


	@Incoming("ingestion")
	@Outgoing("ingestion-datasource")
	@Blocking("ingestion-datasource-pool")
	public IngestionDatasourcePayload process(byte[] json) {

		JsonObject jsonObject = new JsonObject(new String(json));

		IngestionPayload ingestionPayload =
			jsonObject.mapTo(IngestionPayload.class);

		long datasourceId = ingestionPayload.getDatasourceId();

		Datasource datasource =
			Datasource.findById(datasourceId);

		datasource.setLastIngestionDate(
			Instant.ofEpochMilli(
				ingestionPayload.getParsingDate()));

		_many.tryEmitNext(datasource);

		Tenant tenant = Tenant.findById(datasource.getTenantId());

		EnrichPipeline enrichPipeline =
			EnrichPipeline.findByDatasourceId(datasourceId);

		List<EnrichItem> enrichItemList;

		if (enrichPipeline != null) {
			enrichItemList = EnrichItem.findByEnrichPipelineId(
				enrichPipeline.getEnrichPipelineId());
		}
		else {
			enrichPipeline = EnrichPipeline.builder().build();
			enrichItemList = List.of();
		}

		DatasourceContext datasourceContext = DatasourceContext.of(
			datasource, tenant, enrichPipeline, enrichItemList
		);

		return IngestionDatasourcePayload.of(
			ingestionPayload, datasourceContext);

	}

	@Inject
	ManagedExecutor managedExecutor;

	private Sinks.Many<Datasource> _many;
	private Disposable _disposable;



}
