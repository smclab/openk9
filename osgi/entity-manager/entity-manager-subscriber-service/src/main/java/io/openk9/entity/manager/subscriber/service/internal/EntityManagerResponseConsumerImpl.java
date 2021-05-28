package io.openk9.entity.manager.subscriber.service.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.entity.manager.pub.sub.api.MessageResponse;
import io.openk9.entity.manager.subscriber.api.EntityManagerResponseConsumer;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.ReceiverReactor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = EntityManagerResponseConsumer.class
)
public class EntityManagerResponseConsumerImpl
	implements EntityManagerResponseConsumer {

	@Override
	public Mono<MessageResponse> stream(int prefetch, String ingestionId) {
		return _receiverReactor
			.consumeManualAck(_binding.getQueue(), prefetch)
			.flatMap(acknowledgableDelivery -> {

				String ingestionRoutingKey = "ingestion-id." + ingestionId;

				if (acknowledgableDelivery
					.getEnvelope()
					.getRoutingKey()
					.equals(ingestionRoutingKey)) {

					acknowledgableDelivery.ack();

					MessageResponse messageResponse =
						_cborFactory.fromCBOR(
							acknowledgableDelivery.getBody(),
							MessageResponse.class);

					return Mono.just(messageResponse);

				}

				acknowledgableDelivery.nack(true);

				return Mono.empty();

			})
			.next();
	}

	@Override
	public Publisher<MessageResponse> genericStream(int prefetch, String ingestionId) {
		return stream(prefetch, ingestionId);
	}

	@Reference
	private ReceiverReactor _receiverReactor;
	@Reference
	private CBORFactory _cborFactory;

	@Reference(target = "(component.name=io.openk9.entity.manager.pub.sub.binding.internal.EntityManagerResponseBinding)")
	private Binding _binding;

}
