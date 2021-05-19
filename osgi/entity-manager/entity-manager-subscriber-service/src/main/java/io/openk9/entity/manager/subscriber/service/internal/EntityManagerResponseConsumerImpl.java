package io.openk9.entity.manager.subscriber.service.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.entity.manager.pub.sub.api.MessageResponse;
import io.openk9.entity.manager.subscriber.api.EntityManagerResponseConsumer;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.BindingRegistry;
import io.openk9.ingestion.api.QueueService;
import io.openk9.ingestion.api.ReceiverReactor;
import io.openk9.osgi.util.AutoCloseables;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = EntityManagerResponseConsumer.class
)
public class EntityManagerResponseConsumerImpl
	implements EntityManagerResponseConsumer {

	@Override
	public Mono<MessageResponse> stream(int prefetch, String ingestionId) {
		return Mono.defer(() -> {

			String ingestionRoutingKey = "ingestion-id." + ingestionId;

			String ingestionQueueName = "ingestion-id-" + ingestionId;

			AutoCloseables.AutoCloseableSafe closeableSafe =
				_bindingRegistry
					.register(
						_binding.getExchange(), ingestionRoutingKey,
						ingestionQueueName);

			Flux<MessageResponse> response =
				_receiverReactor
					.consumeAutoAck(ingestionQueueName, prefetch)
					.map(delivery -> _cborFactory.fromCBOR(delivery.getBody(),
						MessageResponse.class))
					.filter(
						messageResponse -> messageResponse.getResponse().getIngestionId().equals(
							ingestionId));

			return Mono.usingWhen(
				response.next(),
				Mono::just,
				ignore -> Mono
					.fromRunnable(closeableSafe::close)
					.then(_queueService.deleteQueue(ingestionQueueName))
			);

		});
	}

	@Override
	public Publisher<MessageResponse> genericStream(int prefetch, String ingestionId) {
		return stream(prefetch, ingestionId);
	}

	@Reference
	private ReceiverReactor _receiverReactor;

	@Reference
	private BindingRegistry _bindingRegistry;

	@Reference
	private CBORFactory _cborFactory;

	@Reference(target = "(component.name=io.openk9.entity.manager.pub.sub.binding.internal.EntityManagerResponseBinding)")
	private Binding _binding;

	@Reference
	private QueueService _queueService;

	private static final Logger _log = LoggerFactory.getLogger(
		EntityManagerResponseConsumerImpl.class);

}
