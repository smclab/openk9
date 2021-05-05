package io.openk9.datasource.queue;

import io.openk9.model.IngestionPayload;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component(
	immediate = true,
	service = DatasourceHotFlux.class
)
public class DatasourceHotFlux {

	@Activate
	void activate() {
		_hotFlux = Sinks.many().multicast().onBackpressureBuffer();
	}

	@Deactivate
	void deactivate() {
		_hotFlux.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
		_hotFlux = null;
	}

	public void send(IngestionPayload ingestionPayload) {
		_hotFlux.emitNext(ingestionPayload, Sinks.EmitFailureHandler.FAIL_FAST);
	}

	public Flux<IngestionPayload> stream() {
		return _hotFlux.asFlux();
	}

	private Sinks.Many<IngestionPayload> _hotFlux;

}
