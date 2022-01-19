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
@Transactional
public class DatasourceProcessor {

	@Incoming("ingestion")
	@Outgoing("ingestion-datasource")
	@Blocking("datasource")
	public IngestionDatasourcePayload process(JsonObject jsonObject) throws Exception {

		IngestionPayload ingestionPayload =
			jsonObject.mapTo(IngestionPayload.class);

		long datasourceId = ingestionPayload.getDatasourceId();

		Datasource datasource =
			Datasource.findById(datasourceId);

		Tenant tenant = Tenant.findById(datasource.getTenantId());

		ingestionPayload.setTenantId(tenant.getTenantId());

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

		datasource.setLastIngestionDate(
			Instant.ofEpochMilli(
				ingestionPayload.getParsingDate()));

		datasource.persistAndFlush();

		return IngestionDatasourcePayload.of(
			ingestionPayload, datasourceContext);

	}

}
