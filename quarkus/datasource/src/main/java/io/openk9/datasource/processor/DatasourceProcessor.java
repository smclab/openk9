/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.processor;


import io.openk9.datasource.event.sender.EventSender;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.processor.payload.DatasourceContext;
import io.openk9.datasource.processor.payload.IngestionDatasourcePayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
@Startup
public class DatasourceProcessor {

	@PostConstruct
	void init() {
		_disposable = Flux
			.from(ingestionChannel)
			.concatMap(this::process)
			.subscribe();
	}

	@PreDestroy
	void destroy() {
		_disposable.dispose();
	}
	private Publisher<Void> process(Message<?> message) {

		return Uni.createFrom().item(message)
			.onItem().call(m -> _consumeIngestionMessage(_messagePayloadToJson(m)))
			.onItem().transformToUni(x -> Uni.createFrom().completionStage(message.ack()))
			.toMulti();

	}

	public Uni<DatasourceContext> getDatasourceContext(long datasourceId) {

		Uni<Datasource> datasourceUni =
			Datasource.findById(datasourceId);

		return datasourceUni.flatMap(datasource ->
			EnrichPipeline
				.findByDatasourceId(datasource.getDatasourceId())
				.onItem()
				.ifNull()
				.continueWith(EnrichPipeline::new)
				.flatMap(enrichPipeline -> {
					Uni<List<EnrichItem>> enrichItemUni;

					if (enrichPipeline.getEnrichPipelineId() != null) {

						enrichItemUni = EnrichItem
							.findByEnrichPipelineId(
								enrichPipeline.getEnrichPipelineId())
							.onItem()
							.ifNull()
							.continueWith(List::of);

					}
					else {
						enrichItemUni = Uni.createFrom().item(List.of());
					}

					return Uni
						.combine()
						.all()
						.unis(
							Tenant.findById(datasource.getTenantId()),
							enrichItemUni)
						.combinedWith((tenantObj, enrichItemList) ->
							DatasourceContext.of(
								datasource, (Tenant)tenantObj,
								enrichPipeline, enrichItemList
						));

				})
		);

	}

	private Uni<Void> _consumeIngestionMessage(JsonObject jsonObject) {

		return Uni.createFrom().deferred(() -> {

			JsonObject ingestionPayloadJson =
				jsonObject.getJsonObject("ingestionPayload");

			long datasourceId = ingestionPayloadJson.getLong("datasourceId");

			Uni<DatasourceContext> datasourceContextUni =
				getDatasourceContext(datasourceId);

			return datasourceContextUni.map(dc -> {

				IngestionPayload ingestionPayload =
					ingestionPayloadJson.mapTo(
						IngestionPayload.class);

				return IngestionDatasourcePayload.of(
					ingestionPayload, dc);

			})
				.onItem()
				.invoke(ingestionDatasourceEmitter::send)
				.onFailure()
				.invoke((t) -> logger.error(
				"Error while processing ingestion message", t))
				.replaceWithVoid();
		});

	}

	private JsonObject _messagePayloadToJson(Message<?> message) {
		Object obj = message.getPayload();

		return obj instanceof JsonObject
			? (JsonObject) obj
			: new JsonObject(new String((byte[]) obj));

	}


	@Inject
	@Channel("ingestion")
	Publisher<Message<?>> ingestionChannel;

	@Inject
	EventBus bus;

	@Inject
	Logger logger;

	@Inject
	@Channel("ingestion-datasource")
	Emitter<IngestionDatasourcePayload> ingestionDatasourceEmitter;

	@Inject
	EventSender _eventSender;

	private Disposable _disposable;

}
