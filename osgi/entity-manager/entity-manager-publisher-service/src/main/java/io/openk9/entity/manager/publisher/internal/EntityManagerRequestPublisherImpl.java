package io.openk9.entity.manager.publisher.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.entity.manager.pub.sub.api.MessageRequest;
import io.openk9.entity.manager.publisher.api.EntityManagerRequestPublisher;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.OutboundMessageFactory;
import io.openk9.ingestion.api.Sender;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = EntityManagerRequestPublisher.class
)
public class EntityManagerRequestPublisherImpl
	implements EntityManagerRequestPublisher {

	@Override
	public Mono<Void> publish(MessageRequest messageRequest) {
		return Mono.from(
				_sender.send(
					Mono.just(
						_outboundMessageFactory.createOutboundMessage(
							builder ->
								builder
									.exchange(_binding.getExchange().getName())
									.routingKey(_binding.getRoutingKey())
									.body(_cborFactory.toCBOR(messageRequest))
						)
					)
				)
		);
	}

	@Reference
	private Sender _sender;

	@Reference
	private OutboundMessageFactory _outboundMessageFactory;

	@Reference
	private CBORFactory _cborFactory;

	@Reference(target = "(component.name=io.openk9.entity.manager.pub.sub.binding.internal.EntityManagerRequestBinding)")
	private Binding _binding;

}
