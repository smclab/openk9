package io.openk9.datasource.processor;


import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.processor.payload.DatasourceContext;
import io.openk9.datasource.processor.payload.IngestionDatasourcePayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class DatasourceProcessor {

	@Incoming("ingestion")
	@Outgoing("datasource")
	public IngestionDatasourcePayload process(JsonObject jsonObject) {

		IngestionPayload ingestionPayload =
			jsonObject.mapTo(IngestionPayload.class);

		long datasourceId = ingestionPayload.getDatasourceId();

		Datasource datasource =
			Datasource.findById(datasourceId);

		EnrichPipeline enrichPipeline =
			EnrichPipeline.findByDatasourceId(datasourceId);

		List<EnrichItem> enrichItemList = EnrichItem.findByEnrichPipelineId(
			enrichPipeline.getEnrichPipelineId());

		Tenant tenant = Tenant.findById(ingestionPayload.getTenantId());

		DatasourceContext datasourceContext = DatasourceContext.of(
			datasource, tenant, enrichPipeline, enrichItemList
		);

		return IngestionDatasourcePayload.of(
			ingestionPayload, datasourceContext);

	}

}
