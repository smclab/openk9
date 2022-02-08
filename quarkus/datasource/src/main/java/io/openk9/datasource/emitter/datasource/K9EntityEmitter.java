package io.openk9.datasource.emitter.datasource;

import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.model.K9Entity;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class K9EntityEmitter {

	public void sendDelete(K9Entity k9Entity) {
		_send(k9Entity, Type.DELETE);
	}

	public void sendInsert(K9Entity k9Entity) {
		_send(k9Entity, Type.INSERT);
	}

	public void sendUpdate(K9Entity k9Entity) {
		_send(k9Entity, Type.UPDATE);
	}

	private void _send(K9Entity k9Entity, Type type) {
		_k9EntityEmitter.send(
			Message.of(
				k9Entity,
				Metadata.of(
					new OutgoingRabbitMQMetadata.Builder()
						.withContentType("application/json")
						.withExpiration("10000")
						.withRoutingKey(type.generateRoutingKey(k9Entity))
						.build()
				)
			)
		);
	}

	@Inject
	@Channel("openk9-datasource-direct")
	Emitter<Message<K9Entity>> _k9EntityEmitter;

	@Inject
	Instance<SchedulerInitializer> _schedulerInitializer;

	enum Type {
		DELETE {
			@Override
			public String generateRoutingKey(K9Entity k9Entity) {
				return "datasource-DELETE-" + k9Entity.getClass().getName();
			}
		},
		INSERT {
			@Override
			public String generateRoutingKey(K9Entity k9Entity) {
				return "datasource-INSERT-" + k9Entity.getClass().getName();
			}
		},
		UPDATE {
			@Override
			public String generateRoutingKey(K9Entity k9Entity) {
				return "datasource-UPDATE-" + k9Entity.getClass().getName();
			}
		};

		public abstract String generateRoutingKey(K9Entity k9Entity);

	}


}
