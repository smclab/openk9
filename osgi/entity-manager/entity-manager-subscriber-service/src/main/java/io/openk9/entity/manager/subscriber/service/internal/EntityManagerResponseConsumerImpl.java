package io.openk9.entity.manager.subscriber.service.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.entity.manager.pub.sub.api.MessageResponse;
import io.openk9.entity.manager.subscriber.api.EntityManagerResponseConsumer;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.ReceiverReactor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Component(
	immediate = true,
	service = EntityManagerResponseConsumer.class
)
public class EntityManagerResponseConsumerImpl
	implements EntityManagerResponseConsumer {

	@interface Config {
		long replayDurationMs() default 60000;
	}

	@Activate
	@Modified
	void activate(Config config) {

		_sink =
			Sinks
				.many()
				.replay()
				.limit(Duration.ofMillis(config.replayDurationMs()));

		_disposable =
			_receiverReactor
				.consumeAutoAck(_binding.getQueue())
				.map(delivery -> _cborFactory.fromCBOR(
					delivery.getBody(), MessageResponse.class))
				.doOnNext(_sink::tryEmitNext)
				.subscribe();

	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
		_sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
	}

	@Override
	public Mono<MessageResponse> stream(int prefetch, String ingestionId) {
		return _sink
			.asFlux()
			.filter(messageResponse ->
				messageResponse
					.getResponse()
					.getIngestionId()
					.equals(ingestionId))
			.next();
	}

	@Override
	public Publisher<MessageResponse> genericStream(int prefetch, String ingestionId) {
		return stream(prefetch, ingestionId);
	}

	private Disposable _disposable;

	private Sinks.Many<MessageResponse> _sink;

	@Reference
	private ReceiverReactor _receiverReactor;

	@Reference
	private CBORFactory _cborFactory;

	@Reference(target = "(component.name=io.openk9.entity.manager.pub.sub.binding.internal.EntityManagerResponseBinding)")
	private Binding _binding;

}
