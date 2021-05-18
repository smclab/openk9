package io.openk9.entity.manager.subscriber.api;

import io.openk9.entity.manager.pub.sub.api.MessageResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface EntityManagerResponseConsumer {
	Mono<MessageResponse> stream(int prefetch, String ingestionId);

	default Publisher<MessageResponse> genericStream(int prefetch, String ingestionId) {
		return stream(prefetch, ingestionId);
	}

}
