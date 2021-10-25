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
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class DatasourceProcessor {

	@Incoming("ingestion")
	@Outgoing("ingestion-datasource")
	@Blocking
	@Transactional
	public IngestionDatasourcePayload process(byte[] json) {

		JsonObject jsonObject = new JsonObject(new String(json));

		IngestionPayload ingestionPayload =
			jsonObject.mapTo(IngestionPayload.class);

		long datasourceId = ingestionPayload.getDatasourceId();

		Datasource datasource =
			Datasource.findById(datasourceId);

		datasource.setLastIngestionDate(Instant.now());

		datasource.persist();

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

}
