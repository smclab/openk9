package io.openk9.entity.manager.publisher.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.entity.manager.pub.sub.api.MessageResponse;
import io.openk9.entity.manager.publisher.api.EntityManagerResponsePublisher;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.OutboundMessageFactory;
import io.openk9.ingestion.api.Sender;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = EntityManagerResponsePublisher.class
)
public class EntityManagerResponsePublisherImpl
	implements EntityManagerResponsePublisher {

	@Override
	public Mono<Void> publish(MessageResponse messageRequest) {
		return Mono.from(
			_sender.send(
				Mono.just(
					_outboundMessageFactory.createOutboundMessage(
						builder ->
							builder
								.exchange(_binding.getExchange().getName())
								.routingKey("ingestion-id." + messageRequest.getResponse().getIngestionId())
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

	@Reference(target = "(component.name=io.openk9.entity.manager.pub.sub.binding.internal.EntityManagerResponseBinding)")
	private Binding _binding;
	
}
