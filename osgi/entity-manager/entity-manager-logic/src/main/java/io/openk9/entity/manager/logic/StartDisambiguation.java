package io.openk9.entity.manager.logic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Sinks;

@Component(
	immediate = true,
	service = StartDisambiguation.class
)
public class StartDisambiguation {

	@Activate
	void activate(BundleContext bundleContext) {
		_many = Sinks
			.unsafe()
			.many()
			.unicast()
			.onBackpressureBuffer();
	}

	@Deactivate
	void deactivate() {
		_many.tryEmitComplete();
	}

	public void disambiguate(
		RequestContext request, MonoSink<EntityContext> emitter) {

		_many.emitNext(
			InternalDisambiguation.of(request, emitter),
			Sinks.EmitFailureHandler.FAIL_FAST);

	}

	public Flux<InternalDisambiguation> flux() {
		return _many.asFlux();
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class InternalDisambiguation {
		private RequestContext request;
		private MonoSink<EntityContext> emitter;
	}

	private Sinks.Many<InternalDisambiguation> _many;

}
