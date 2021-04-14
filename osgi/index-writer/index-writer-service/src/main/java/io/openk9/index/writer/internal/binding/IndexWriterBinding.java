package io.openk9.index.writer.internal.binding;

import io.openk9.ingestion.api.Binding;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = Binding.class
)
public class IndexWriterBinding implements Binding {

	@Override
	public Exchange getExchange() {
		return Exchange.of(
			"index-writer-topic",
			Exchange.Type.topic);
	}

	@Override
	public String getRoutingKey() {
		return "#";
	}

	@Override
	public String getQueue() {
		return "index-writer";
	}

}
