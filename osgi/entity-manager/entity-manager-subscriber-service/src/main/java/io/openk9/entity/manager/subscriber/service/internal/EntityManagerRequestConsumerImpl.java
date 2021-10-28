package io.openk9.entity.manager.subscriber.service.internal;

import io.openk9.entity.manager.subscriber.api.EntityManagerRequestConsumer;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.ReceiverReactor;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.ObjectNode;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	public Flux<ObjectNode> stream() {
		return stream(_defaultPrefetch);
	}

	@Override
	public Flux<ObjectNode> stream(int prefetch) {
		return _receiverReactor
			.consumeAutoAck(_binding.getQueue(), prefetch)
			.map(delivery -> _jsonFactory.fromJsonToJsonNode(delivery.getBody()).toObjectNode());
	}

	@Override
	public Publisher<ObjectNode> genericRequest() {
		return stream();
	}

	@Override
	public Publisher<ObjectNode> genericRequest(int prefetch) {
		return stream(prefetch);
	}

	private int _defaultPrefetch;

	@Reference
	private ReceiverReactor _receiverReactor;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference(target = "(component.name=io.openk9.entity.manager.pub.sub.binding.internal.EntityManagerRequestBinding)")
	private Binding _binding;

	private static final Logger _log = LoggerFactory.getLogger(
		EntityManagerRequestConsumerImpl.class);

}
