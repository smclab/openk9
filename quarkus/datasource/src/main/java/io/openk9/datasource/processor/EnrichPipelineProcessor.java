package io.openk9.datasource.processor;

import io.openk9.datasource.processor.enrich.EnrichStepHandler;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.MessageUtil;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class EnrichPipelineProcessor {

	@Incoming("enrich-pipeline-incoming")
	public Uni<Void> process(Message<DataPayload> message) {

		Optional<IncomingRabbitMQMetadata> metadata =
			message.getMetadata(IncomingRabbitMQMetadata.class);

		return Uni
			.createFrom()
			.optional(metadata)
			.flatMap(m -> {

				String routingKey = m.getRoutingKey();

				Tuple2<Long, Long> t2 =
					_getEnrichPipelineIdAndEnrichItemId(routingKey);

				DataPayload payload = MessageUtil.toObj(
					message, DataPayload.class);

				return enrichStepHandler.consume(
					EnrichStepHandler.EnrichStep
						.builder()
						.enrichPipelineId(t2.getItem1())
						.enrichItemId(t2.getItem2())
						.payload(payload)
						.build()
				);

			})
			.onItemOrFailure()
			.transformToUni((item, failure) -> {

				if (failure != null) {
					logger.error(failure.getMessage(), failure);
					return Uni.createFrom().completionStage(
						() -> message.nack(failure));
				}

				return Uni.createFrom().completionStage(message::ack);

			});

	}

	private static Tuple2<Long, Long> _getEnrichPipelineIdAndEnrichItemId(
		String routingKey) {

		Matcher matcher = _ROUTING_KEY_PATTERN.matcher(routingKey);

		if (matcher.matches()) {
			return Tuple2.of(
				Long.parseLong(matcher.group(1)),
				Long.parseLong(matcher.group(2))
			);
		}

		throw new IllegalArgumentException("routingKey: " + routingKey);

	}

	@Inject
	EnrichStepHandler enrichStepHandler;

	@Inject
	Logger logger;

	private static final Pattern _ROUTING_KEY_PATTERN =
		Pattern.compile("io\\.openk9\\.enrich\\.pipeline\\.([0-9]+)\\.([0-9]+)");

}
