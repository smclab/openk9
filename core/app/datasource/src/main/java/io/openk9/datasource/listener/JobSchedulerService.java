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

package io.openk9.datasource.listener;

import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverContext;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.service.SchedulerService;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.Message;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.GetComposableIndexTemplateRequest;
import org.opensearch.client.indices.GetComposableIndexTemplatesResponse;
import org.opensearch.client.indices.PutComposableIndexTemplateRequest;
import org.opensearch.cluster.metadata.ComposableIndexTemplate;
import org.opensearch.cluster.metadata.Template;
import org.opensearch.core.action.ActionListener;
import org.opensearch.core.rest.RestStatus;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class JobSchedulerService {

	private static final Logger log =
		Logger.getLogger(JobSchedulerService.class);

	private static final String CALL_PLUGIN_DRIVER =
		"JobSchedulerService#callPluginDriver";
	private static final String COPY_INDEX_TEMPLATE =
		"JobSchedulerService#copyIndexTemplate";
	private static final String FETCH_DATASOURCE_CONNECTION =
		"JobSchedulerService#fetchDatasourceConnection";
	private static final String PERSIST_SCHEDULER =
		"JobSchedulerService#persistScheduler";
	private static final String FETCH_EMBEDDING_MODEL =
		"JobScheduler#fetchEmbeddingModel";
	private static final String TRIGGER_DATASOURCE =
		"JobScheduler#triggerDatasource";

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	SchedulerService schedulerService;

	public static CompletableFuture<Void> callHttpPluginDriverClient(
		Scheduler scheduler,
		OffsetDateTime lastIngestionDate) {

		var request = new CallHttpPluginDriverRequest(
			scheduler,
			lastIngestionDate
		);

		return EventBusInstanceHolder.getEventBus()
			.<Void>request(CALL_PLUGIN_DRIVER, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<Void> copyIndexTemplate(Scheduler scheduler) {

		CopyIndexTemplateRequest request =
			new CopyIndexTemplateRequest(scheduler);

		return EventBusInstanceHolder.getEventBus()
			.<Void>request(COPY_INDEX_TEMPLATE, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<Datasource> fetchDatasourceConnection(
		String tenantId, long datasourceId) {

		var request = new FetchDatasourceConnectionRequest(
			tenantId,
			datasourceId
		);

		return EventBusInstanceHolder.getEventBus()
			.<Datasource>request(FETCH_DATASOURCE_CONNECTION, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<EmbeddingModel> fetchEmbeddingModel(
		String tenantId) {

		var request = new FetchEmbeddingModelRequest(tenantId);

		return EventBusInstanceHolder.getEventBus()
			.<EmbeddingModel>request(FETCH_EMBEDDING_MODEL, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<Scheduler> persistScheduler(
		String tenantId, Scheduler scheduler) {

		var request = new PersistSchedulerRequest(tenantId, scheduler);

		return EventBusInstanceHolder.getEventBus()
			.<Scheduler>request(PERSIST_SCHEDULER, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<TriggerType> getTriggerType(
		Datasource datasource, boolean reindex) {

		var request = new TriggerDatasourceRequest(datasource, reindex);

		return EventBusInstanceHolder.getEventBus()
			.<TriggerType>request(TRIGGER_DATASOURCE, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	@ConsumeEvent(CALL_PLUGIN_DRIVER)
	Uni<Void> callPluginDriver(CallHttpPluginDriverRequest request) {

		var scheduler = request.scheduler();
		var lastIngestionDate = request.lastIngestionDate();

		var datasource = scheduler.getDatasource();
		var tenantId = datasource.getTenant();
		var pluginDriver = datasource.getPluginDriver();

		var httpPluginDriverInfo = Json.decodeValue(
			pluginDriver.getJsonConfig(), HttpPluginDriverInfo.class);

		return httpPluginDriverClient.invoke(
			httpPluginDriverInfo,
			HttpPluginDriverContext
				.builder()
				.timestamp(lastIngestionDate)
				.tenantId(tenantId)
				.datasourceId(datasource.getId())
				.scheduleId(scheduler.getScheduleId())
				.datasourceConfig(new JsonObject(datasource.getJsonConfig()).getMap())
				.build()
		).replaceWithVoid();

	}

	@ConsumeEvent(COPY_INDEX_TEMPLATE)
	Uni<Void> copyIndexTemplate(CopyIndexTemplateRequest request) {

		var scheduler = request.scheduler();

		DataIndex oldDataIndex = scheduler.getOldDataIndex();
		DataIndex newDataIndex = scheduler.getNewDataIndex();
		String newDataIndexName = newDataIndex.getIndexName();

		IndicesClient indices = restHighLevelClient.indices();

		var getIndexTemplateRequest = new GetComposableIndexTemplateRequest(
			oldDataIndex.getIndexName() + "-template");

		return Uni.createFrom()
			.emitter(emitter -> indices.getIndexTemplateAsync(
					getIndexTemplateRequest,
					RequestOptions.DEFAULT,
					new ActionListener<>() {

						@Override
						public void onResponse(GetComposableIndexTemplatesResponse indexTemplate) {
							var iterator = indexTemplate.getIndexTemplates().values().iterator();

							if (iterator.hasNext()) {
								ComposableIndexTemplate composableIndexTemplate = iterator.next();

								PutComposableIndexTemplateRequest templateRequest =
									new PutComposableIndexTemplateRequest();

								Template template = composableIndexTemplate.template();

								ComposableIndexTemplate newComposableIndexTemplate =
									new ComposableIndexTemplate(
										List.of(newDataIndexName),
										new Template(
											template.settings(),
											template.mappings(),
											template.aliases()
										),
										composableIndexTemplate.composedOf(),
										composableIndexTemplate.priority(),
										composableIndexTemplate.version(),
										composableIndexTemplate.metadata()
									);

								templateRequest
									.name(newDataIndexName + "-template")
									.indexTemplate(newComposableIndexTemplate);

								indices.putIndexTemplateAsync(
									templateRequest,
									RequestOptions.DEFAULT,
									new ActionListener<>() {
										@Override
										public void onResponse(AcknowledgedResponse acknowledgedResponse) {
											emitter.complete(null);
										}

										@Override
										public void onFailure(Exception e) {
											emitter.fail(e);
										}
									}
								);
							}
						}

						@Override
						public void onFailure(Exception e) {
							if (e instanceof OpenSearchStatusException
								&& ((OpenSearchStatusException) e).status() == RestStatus.NOT_FOUND) {

								log.warn("Cannot Copy Index Template", e);

								emitter.complete(null);
							}
							else {
								emitter.fail(e);
							}
						}
					}
				)
			);
	}

	@ConsumeEvent(FETCH_DATASOURCE_CONNECTION)
	Uni<Datasource> fetchDatasourceConnection(
		FetchDatasourceConnectionRequest request) {

		return sessionFactory.withTransaction(
			request.tenantId(), (s, t) ->
				s.createQuery(
						"select d " +
						"from Datasource d " +
						"join fetch d.pluginDriver " +
						"left join fetch d.dataIndex di " +
						"left join fetch di.vectorIndex vi " +
						"left join fetch di.docTypes " +
						"where d.id = :id", Datasource.class)
					.setParameter("id", request.datasourceId())
					.getSingleResult()
		);
	}

	@ConsumeEvent(FETCH_EMBEDDING_MODEL)
	Uni<EmbeddingModel> fetchEmbeddingModel(FetchEmbeddingModelRequest request) {

		var tenantId = request.tenantId();

		return sessionFactory.withTransaction(tenantId, (s, t) -> s
			.createNamedQuery(EmbeddingModel.FETCH_CURRENT, EmbeddingModel.class)
			.getSingleResult()
		);
	}

	@ConsumeEvent(PERSIST_SCHEDULER)
	Uni<Scheduler> persistScheduler(PersistSchedulerRequest request) {

		return sessionFactory.withTransaction(request.tenantId(), (s, t) -> {

			var scheduler = request.scheduler();

			copyDocTypes(s, scheduler);

			return s.persist(scheduler).map(unused -> scheduler);
		});
	}

	@ConsumeEvent(TRIGGER_DATASOURCE)
	Uni<TriggerType> triggerDatasource(TriggerDatasourceRequest request) {

		var datasource = request.datasource();
		var tenantId = datasource.getTenant();
		var reindex = request.reindex();

		return sessionFactory.withTransaction(
			tenantId,
			(s, t) -> s.createQuery(
					"select s " +
					"from Scheduler s " +
					"where s.datasource.id = :datasourceId " +
					"and s.status in " + Scheduler.RUNNING_STATES,
					Scheduler.class
				)
				.setParameter("datasourceId", datasource.getId())
				.getSingleResultOrNull()
				.flatMap(scheduler -> {

					if (scheduler != null) {

						if (reindex) {
							schedulerService.cancelScheduling(tenantId, scheduler.getId());

							log.warnf(
								"Trying to cancel the Scheduler with id %s for datasource %s" +
									" in %s state to start Reindex",
								scheduler.getId(),
								datasource.getId(),
								scheduler.getStatus()
							);

							return Uni.createFrom().item(TriggerType.REINDEX);
						}
						else {
							log.warnf(
								"A Scheduler with id %s for datasource %s is %s",
								scheduler.getId(),
								datasource.getId(),
								scheduler.getStatus()
							);

							return Uni.createFrom().item(TriggerType.IGNORE);
						}
					}

					if (reindex != null) {
						return Uni.createFrom().item(reindex
							? TriggerType.REINDEX
							: TriggerType.TRIGGER);
					}
					else {
						return Uni.createFrom().item(TriggerType.TRIGGER);
					}

				})
		);

	}

	private static void copyDocTypes(Mutiny.Session s, Scheduler scheduler) {
		DataIndex oldDataIndex = scheduler.getOldDataIndex();
		DataIndex newDataIndex = scheduler.getNewDataIndex();

		if (oldDataIndex != null && newDataIndex != null) {
			Set<DocType> docTypes = oldDataIndex.getDocTypes();
			if (docTypes != null && !docTypes.isEmpty()) {
				Set<DocType> refreshed = new LinkedHashSet<>();

				for (DocType docType : docTypes) {
					refreshed.add(s.getReference(docType));
				}
				newDataIndex.setDocTypes(refreshed);
			}
		}
	}

	private record FetchDatasourceConnectionRequest(
		String tenantId, long datasourceId
	) {}

	private record FetchEmbeddingModelRequest(String tenantId) {}

	private record PersistSchedulerRequest(
		String tenantId, Scheduler scheduler
	) {}

	private record CopyIndexTemplateRequest(Scheduler scheduler) {}

	private record TriggerDatasourceRequest(Datasource datasource, Boolean reindex) {}

	private record CallHttpPluginDriverRequest(
		Scheduler scheduler, OffsetDateTime lastIngestionDate
	) {}

}