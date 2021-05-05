package io.openk9.datasource.queue;

import io.openk9.ingestion.api.Binding;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = Binding.class
)
public class DatasourceBinding implements Binding {

	@Override
	public Exchange getExchange() {
		return Exchange.of("openk9.topic", Exchange.Type.topic);
	}

	@Override
	public String getRoutingKey() {
		return "io.openk9";
	}

	@Override
	public String getQueue() {
		return "datasource";
	}

}
