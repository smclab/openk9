package io.openk9.entity.manager.publisher.api;

import io.openk9.entity.manager.pub.sub.api.MessageRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface EntityManagerRequestPublisher {

	Mono<Void> publish(MessageRequest messageRequest);

	default Publisher<Void> genericPublish(MessageRequest messageRequest) {
		return publish(messageRequest);
	}

}
