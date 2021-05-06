package io.openk9.index.writer.mappings.publisher.binding;

import io.openk9.ingestion.api.Binding;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = Binding.class
)
public class MappingsBinding implements Binding {

	@Override
	public Exchange getExchange() {
		return Exchange.of(
			"index-writer-mappings.fanout",
			Exchange.Type.fanout);
	}

	@Override
	public String getRoutingKey() {
		return "#";
	}

	@Override
	public String getQueue() {
		return "index-writer-mappings";
	}

}
