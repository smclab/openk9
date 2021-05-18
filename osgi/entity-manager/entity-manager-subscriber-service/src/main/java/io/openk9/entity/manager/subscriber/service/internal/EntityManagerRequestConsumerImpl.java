package io.openk9.entity.manager.subscriber.service.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.entity.manager.pub.sub.api.MessageRequest;
import io.openk9.entity.manager.subscriber.api.EntityManagerRequestConsumer;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.ReceiverReactor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Component(
	immediate = true,
	service = EntityManagerRequestConsumer.class
)
public class EntityManagerRequestConsumerImpl
	implements EntityManagerRequestConsumer {

	@interface Config {
		int defaultPrefetch() default 250;
	}

	@Activate
	void activate(Config config) {
		_defaultPrefetch = config.defaultPrefetch();
	}

	@Modified
	void modified(Config config) {
		activate(config);
	}

	@Override
	public Flux<MessageRequest> stream() {
		return stream(_defaultPrefetch);
	}

	@Override
	public Flux<MessageRequest> stream(int prefetch) {
		return _receiverReactor
			.consumeAutoAck(_binding.getQueue(), prefetch)
			.map(delivery -> _cborFactory.fromCBOR(delivery.getBody(), MessageRequest.class));
	}

	@Override
	public Publisher<MessageRequest> genericRequest() {
		return stream();
	}

	@Override
	public Publisher<MessageRequest> genericRequest(int prefetch) {
		return stream(prefetch);
	}

	private int _defaultPrefetch;

	@Reference
	private ReceiverReactor _receiverReactor;

	@Reference
	private CBORFactory _cborFactory;

	@Reference(target = "(component.name=io.openk9.entity.manager.pub.sub.binding.internal.EntityManagerRequestBinding)")
	private Binding _binding;

}
