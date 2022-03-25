package io.openk9.tika;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import io.openk9.tika.api.Message;
import io.openk9.tika.api.OutgoingMessage;
import io.openk9.tika.api.Publisher;
import io.openk9.tika.config.TikaConfiguration;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.smallrye.mutiny.subscription.Cancellable;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public class IncomingProcessorImpl {

	public void onApplicationStart(@Observes StartupEvent event) {

		setupQueues();

		_unicastProcessor = UnicastProcessor.create();

		_cancellable = _unicastProcessor
			.onItem()
			.call(outgoingMessage -> Uni
				.createFrom()
				.emitter(emitter -> {
					try {
						channel.basicPublish(
							outgoingMessage.getExchange(),
							outgoingMessage.getRoutingKey(), null,
							outgoingMessage.getBody());
						emitter.complete(null);
					}
					catch (Exception e) {
						emitter.fail(e);
					}
				})
			)
			.subscribe()
			.with(outgoingMessage -> {
			});

	}

	public void onApplicationShutdown(@Observes ShutdownEvent event)
		throws IOException, TimeoutException {
		_cancellable.cancel();
		channel.close();
	}

	@Produces
	@ApplicationScoped
	public Publisher createPublisher() {
		return _unicastProcessor::onNext;
	}

	@Produces
	@ApplicationScoped
	@Named("io.openk9.tika")
	public Multi<Message> createReceiving() {

		return Multi
			.createFrom()
			.<Message>emitter(multiEmitter -> {

				try {

					String consumerTag = channel.basicConsume(
						tikaConfiguration.getCurrentQueueName(), false,
						new DefaultConsumer(channel) {
							@Override
							public void handleDelivery(
								String consumerTag,
								Envelope envelope,
								AMQP.BasicProperties properties,
								byte[] body) throws IOException {

								multiEmitter.emit(
									new MessageImpl(
										channel, consumerTag, envelope,
										properties, body)
								);

							}

							@Override
							public void handleCancel(String consumerTag)
								throws IOException {
								multiEmitter.complete();
							}

							@Override
							public void handleShutdownSignal(
								String consumerTag,
								ShutdownSignalException sig) {
								multiEmitter.fail(sig);
							}

						});

				multiEmitter.onTermination(() -> {
					try {
						channel.basicCancel(consumerTag);
					}
					catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				});

			}
			catch (Exception e) {
				multiEmitter.fail(e);
			}
		});

	}

	private void setupQueues() {
		try {
			Connection connection = rabbitMQClient.connect();
			channel = connection.createChannel();
			channel.queueDeclare(
				tikaConfiguration.getCurrentQueueName(), true, false, false,
				Map.of(
					"x-dead-letter-exchange", "DLX",
					"x-dead-letter-routing-key",
					tikaConfiguration.getCurrentRoutingKey(),
					"x-expires", tikaConfiguration.getXExpires()
				)
			);
			channel.queueBind(
				tikaConfiguration.getCurrentQueueName(),
				tikaConfiguration.getCurrentExchange(),
				tikaConfiguration.getCurrentRoutingKey());
			channel.basicQos(1);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Inject
	Logger logger;

	@Inject
	TikaConfiguration tikaConfiguration;

	@Inject
	RabbitMQClient rabbitMQClient;

	private Channel channel;

	private UnicastProcessor<OutgoingMessage> _unicastProcessor;

	private Cancellable _cancellable;

	@RequiredArgsConstructor
	private static class MessageImpl implements Message {

		@Override
		public void ackBlock() {
			try {
				channel.basicAck(envelope.getDeliveryTag(), false);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Uni<Void> ack() {

			return Uni
				.createFrom()
				.emitter(uniEmitter -> {
					try {
						channel.basicAck(envelope.getDeliveryTag(), false);
						uniEmitter.complete(null);
					}
					catch (IOException e) {
						uniEmitter.fail(e);
					}
				});
		}

		@Override
		public void nackBlock() {
			try {
				channel.basicNack(envelope.getDeliveryTag(), false, true);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Uni<Void> nack() {
			return Uni
				.createFrom()
				.emitter(uniEmitter -> {
					try {
						channel.basicNack(envelope.getDeliveryTag(), false, true);
						uniEmitter.complete(null);
					}
					catch (IOException e) {
						uniEmitter.fail(e);
					}
				});
		}

		@Override
		public byte[] body() {
			return body;
		}

		@Override
		public Envelope envelope() {
			return envelope;
		}

		@Override
		public AMQP.BasicProperties properties() {
			return properties;
		}

		@Override
		public String consumerTag() {
			return consumerTag;
		}

		private final Channel channel;
		private final String consumerTag;
		private final Envelope envelope;
		private final AMQP.BasicProperties properties;
		private final byte[] body;

	}

}
