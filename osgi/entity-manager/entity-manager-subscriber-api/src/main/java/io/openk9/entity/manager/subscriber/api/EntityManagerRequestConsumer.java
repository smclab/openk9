package io.openk9.entity.manager.subscriber.api;

import io.openk9.json.api.ObjectNode;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public interface EntityManagerRequestConsumer {

	Flux<ObjectNode> stream();

	Flux<ObjectNode> stream(int prefetch);

	default Publisher<ObjectNode> genericRequest() {
		return stream();
	}

	default Publisher<ObjectNode> genericRequest(int prefetch) {
		return stream(prefetch);
	}

}
