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

	public void sendEvent(DatasourceEvent datasourceEvent) {
		quoteRequestEmitter.send(
			Message.of(
				datasourceEvent,
				Metadata.of(
					OutgoingRabbitMQMetadata
						.builder()
						.withExpiration("300000")
						.build()
				)
			)
		);
	}

	@Inject
	@Channel("datasource-events-requests")
	Emitter<DatasourceEvent> quoteRequestEmitter;

}
