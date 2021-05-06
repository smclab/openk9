package io.openk9.datasource.queue;

import io.openk9.ingestion.api.Binding;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = Binding.class
)
public class IngestionBinding implements Binding {

	@Override
	public Exchange getExchange() {
		return Exchange.of("io.openk9.ingestion.topic", Exchange.Type.topic);
	}

	@Override
	public String getRoutingKey() {
		return "io.openk9.ingestion";
	}

	@Override
	public String getQueue() {
		return "ingestion";
	}

}
