package io.openk9.plugin.driver.manager.ingestion;

import io.openk9.ingestion.api.Binding;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = Binding.class
)
public class IngestionDatasourceBinding implements Binding {

	@Override
	public Exchange getExchange() {
		return Exchange.of("io.openk9.ingestion.topic", Exchange.Type.topic);
	}

	@Override
	public String getRoutingKey() {
		return "io.openk9.ingestion.datasource";
	}

	@Override
	public String getQueue() {
		return "ingestion-datasource";
	}

}
