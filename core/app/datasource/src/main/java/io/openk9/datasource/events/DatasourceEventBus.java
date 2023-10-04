package io.openk9.datasource.events;

import io.quarkus.runtime.Startup;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Startup
public class DatasourceEventBus {

	public void sendEvent(DatasourceMessage datasourceMessage) {
		quoteRequestEmitter.send(
			Message.of(
				datasourceMessage,
				Metadata.of(OutgoingRabbitMQMetadata
					.builder()
					.withDeliveryMode(2)
					.build()
				)
			)
		);
	}

	@Inject
	@Channel("datasource-events-requests")
	Emitter<DatasourceMessage> quoteRequestEmitter;

}
