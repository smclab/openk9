package io.openk9.entity.manager.subscriber.api;

import io.openk9.entity.manager.pub.sub.api.MessageRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public interface EntityManagerRequestConsumer {

	Flux<MessageRequest> stream();

	Flux<MessageRequest> stream(int prefetch);

	default Publisher<MessageRequest> genericRequest() {
		return stream();
	}

	default Publisher<MessageRequest> genericRequest(int prefetch) {
		return stream(prefetch);
	}

}
