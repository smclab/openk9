package io.openk9.datasource.processor.enrich;

import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.pipeline.actor.EnrichPipelineActorSystem;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.EnrichPipelinePayload;
import io.openk9.datasource.script.GroovyService;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.service.EnrichPipelineService;
import io.openk9.datasource.sql.TransactionInvoker;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vavr.control.Try;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

		String tenantId = enrichStep.getPayload().getTenantId();

		return transactionInvoker.withStatelessTransaction(tenantId, () -> {

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

					String validationScript = enrichItem.getValidationScript();

					if (StringUtils.isNotBlank(validationScript)) {

						Try<Boolean> executeScriptCondition =
							groovyService.executeScriptCondition(
								validationScript, enrichStep.getPayload());

						Boolean validation =
							executeScriptCondition
								.onFailure(throwable -> logger.error(
									"Error executing validation script: " +
									validationScript, throwable))
								.getOrElse(false);

						if (!validation) {
							logger.warn("validation for enrichItem: " + enrichItemId + " failed skip to next enrichItem");
							return consume(
								EnrichStep
									.builder()
									.enrichItemId(t2.getItem2().getId())
									.enrichPipelineId(enrichStep.getEnrichPipelineId())
									.payload(enrichStep.getPayload())
									.build()
							);
						}
						else {
							logger.info("Validation script executed successfully for enrich item: " + enrichItemId);
						}

					}

					String serviceName = enrichItem.getServiceName();
					String jsonConfig = enrichItem.getJsonConfig();

					EnrichItem nextEnrichItem = t2.getItem2();

					String replayTo =
						nextEnrichItem != null
							? ROUTING_KEY_PREFIX + enrichStep.getEnrichPipelineId() + "." + nextEnrichItem.getId()
							: "io.openk9.index-writer";

					EnrichItem.EnrichItemType type = enrichItem.getType();

					boolean async = type == EnrichItem.EnrichItemType.ASYNC;

					Map<String, Object> enrichItemJsonConfig =
						new JsonObject(jsonConfig).getMap();

					return enrichPipelineActorSystem.call(
						async,
						serviceName,
						JsonObject.of(
							"payload", enrichStep.getPayload(),
							"enrichItemConfig", enrichItemJsonConfig
						)
					)
						.invoke(response ->
							enrichPipelineEmitter.send(
								Message.of(
									EnrichPipelinePayload.of(
										response.mapTo(DataPayload.class),
										enrichItemJsonConfig,
										replayTo
									),
									Metadata.of(
										OutgoingRabbitMQMetadata
											.builder()
											.withRoutingKey(replayTo)
											.withDeliveryMode(2)
											.build()
									)
								)
							)
						).replaceWithVoid();

				});
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

	@Inject
	TransactionInvoker transactionInvoker;

	@Inject
	GroovyService groovyService;

	@Inject
	EnrichPipelineActorSystem enrichPipelineActorSystem;


	public static final String ROUTING_KEY_PREFIX = "io.openk9.enrich.pipeline.";


}
