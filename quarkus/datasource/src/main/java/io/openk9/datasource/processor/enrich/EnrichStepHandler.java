package io.openk9.datasource.processor.enrich;

import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.EnrichPipelinePayload;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.service.EnrichPipelineService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class EnrichStepHandler {

	public Uni<Void> consume(EnrichStep enrichStep) {

		Long enrichItemId = enrichStep.getEnrichItemId();

		if (enrichItemId == null || enrichItemId == 0) {
			return emitToIndexWriter(enrichStep);
		}

		Uni<EnrichItem> enrichItemUni =
			enrichItemService.findById(enrichItemId);

		Uni<EnrichItem> nextEnrichItemUni =
			enrichPipelineService.findNextEnrichItem(
				enrichStep.getEnrichPipelineId(), enrichStep.getEnrichItemId());

		return Uni
			.combine()
			.all()
			.unis(enrichItemUni, nextEnrichItemUni)
			.asTuple()
			.flatMap(t2 -> {

				EnrichItem enrichItem = t2.getItem1();

				if (enrichItem == null) {
					return emitToIndexWriter(enrichStep);
				}

				String serviceName = enrichItem.getServiceName();
				String jsonConfig = enrichItem.getJsonConfig();

				EnrichItem nextEnrichItem = t2.getItem2();

				String replayTo =
					nextEnrichItem != null
						? ROUTING_KEY_PREFIX + enrichStep.getEnrichPipelineId() + "." + nextEnrichItem.getId()
						: "io.openk9.index-writer";

				EnrichItem.EnrichItemType type = enrichItem.getType();

				switch (type) {
					case SYNC:
						logger.warn("SYNC type to be implemented, fallback to ASYNC");
					case ASYNC:
					default:
						return Uni
							.createFrom()
							.emitter((emitter) -> {
								try {
									enrichPipelineEmitter.send(
										Message.of(
											EnrichPipelinePayload.of(
												enrichStep.getPayload(),
												new JsonObject(jsonConfig).getMap(),
												replayTo
											),
											Metadata.of(
												OutgoingRabbitMQMetadata
													.builder()
													.withRoutingKey(serviceName)
													.withDeliveryMode(2)
													.build()
											)
										)
									);
									emitter.complete(null);
								}
								catch (Exception e) {
									emitter.fail(e);
								}

							});
				}


		});

	}

	private Uni<Void> emitToIndexWriter(EnrichStep enrichStep) {
		return Uni
			.createFrom()
			.completionStage(
				() -> indexWriterEmitter.send(
					EnrichPipelinePayload.of(
						enrichStep.getPayload(),
						Map.of(), "")
				)
			);
	}

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	@NoArgsConstructor
	@RegisterForReflection
	public static class EnrichStep {
		private DataPayload payload;
		private Long enrichPipelineId;
		private Long enrichItemId;
	}

	@Channel("enrich-pipeline-outgoing")
	@Inject
	Emitter<EnrichPipelinePayload> enrichPipelineEmitter;

	@Channel("index-writer-outgoing")
	@Inject
	Emitter<EnrichPipelinePayload> indexWriterEmitter;

	@Inject
	EnrichItemService enrichItemService;

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	Logger logger;

	public static final String ROUTING_KEY_PREFIX = "io.openk9.enrich.pipeline.";


}
