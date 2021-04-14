package io.openk9.entity.manager.internal.queue;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.entity.manager.model.Entity;
import io.openk9.ingestion.api.OutboundMessage;
import io.openk9.ingestion.api.OutboundMessageFactory;
import io.openk9.ingestion.api.SenderReactor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component(
	immediate = true,
	service = ElasticSearchWriterBus.class
)
public class ElasticSearchWriterBus {

	@interface Config {
		String exchange() default "index-writer-topic";
		String routingKey() default "entity";
	}

	@Activate
	void activate(Config config) {
		_exchange = config.exchange();
		_routingKey = config.routingKey();

		_many = Sinks.many().unicast().onBackpressureBuffer();

		_disposable = _many
			.asFlux()
			.flatMap(this::_send)
			.subscribe();

	}

	@Modified
	void modified(Config config) {
		deactivate();
		activate(config);
	}

	@Deactivate
	void deactivate() {
		_many.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
		_disposable.dispose();
		_many = null;
		_disposable = null;
		_exchange = null;
		_routingKey = null;
	}

	public void send(Entity entity) {
		_many.emitNext(entity, Sinks.EmitFailureHandler.FAIL_FAST);
	}

	private Publisher<Void> _send(Entity entity) {

		OutboundMessage outboundMessage =
			_outboundMessageFactory.createOutboundMessage(
				builder -> builder
					.body(_cborFactory.toCBOR(entity))
					.exchange(_exchange)
					.routingKey(entity.getTenantId() + "-" + _routingKey)
			);

		return _senderReactor.send(Mono.just(outboundMessage));

	}

	private String _exchange;

	private String _routingKey;

	private Sinks.Many<Entity> _many;

	private Disposable _disposable;

	@Reference
	private CBORFactory _cborFactory;

	@Reference
	private SenderReactor _senderReactor;

	@Reference
	private OutboundMessageFactory _outboundMessageFactory;

}
