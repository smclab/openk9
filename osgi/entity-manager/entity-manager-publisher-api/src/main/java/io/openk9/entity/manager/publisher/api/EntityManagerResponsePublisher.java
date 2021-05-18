package io.openk9.entity.manager.publisher.api;

import io.openk9.entity.manager.pub.sub.api.MessageResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface EntityManagerResponsePublisher {

	Mono<Void> publish(MessageResponse messageResponse);

	default Publisher<Void> genericPublish(MessageResponse messageResponse) {
		return publish(messageResponse);
	}

}
