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

package io.openk9.datasource.service;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.FieldValidator;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.graphql.dto.DatasourceConnectionDTO;
import io.openk9.datasource.mapper.DatasourceMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.model.dto.VectorIndexDTO;
import io.openk9.datasource.service.exception.K9Error;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

@ApplicationScoped
public class DatasourceService extends BaseK9EntityService<Datasource, DatasourceDTO> {
	private static final String UPDATE_DATASOURCE = "DatasourceService#updateDatasource";
	private static final Logger log = Logger.getLogger(DatasourceService.class);
	private static EventBus eventBus;
	@Inject
	DataIndexService dataIndexService;
	@Inject
	EnrichPipelineService enrichPipelineService;
	@Inject
	PluginDriverService pluginDriverService;
	@Inject
	SchedulerService schedulerService;
	@Inject
	VectorIndexService vectorIndexService;

	DatasourceService(DatasourceMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{Datasource_.NAME, Datasource_.DESCRIPTION};
	}

	@Override
	public Class<Datasource> getEntityClass() {
		return Datasource.class;
	}

	public static CompletableFuture<Void> updateDatasource(
		String tenantId,
		long datasourceId,
		OffsetDateTime lastIngestionDate,
		Long newDataIndexId) {

		var eventBus = CDI.current().select(EventBus.class).get();

		return eventBus.request(UPDATE_DATASOURCE, new UpdateDatasourceRequest(
				tenantId, datasourceId, lastIngestionDate, newDataIndexId))
			.map(message -> (Void) message.body())
			.subscribeAsCompletionStage();
	}

	@ConsumeEvent(UPDATE_DATASOURCE)
	Uni<Void> _updateDatasource(UpdateDatasourceRequest request) {

		var tenantId = request.tenantId();
		var datasourceId = request.datasourceId();
		var lastIngestionDate = request.lastIngestionDate();
		var newDataIndexId = request.newDataIndexId();

		return sessionFactory.withTransaction(
			tenantId, (s, t) -> s.find(Datasource.class, datasourceId)
				.flatMap(datasource -> {
					datasource.setLastIngestionDate(lastIngestionDate);

					if (newDataIndexId != null) {
						var newDataIndex = s.getReference(DataIndex.class, newDataIndexId);

						log.infof(
							"replacing dataindex %s for datasource %s on tenant %s",
							newDataIndexId, datasourceId, tenantId
						);

						datasource.setDataIndex(newDataIndex);
					}

					return s.persist(datasource);
				})
		);
	}

	public Uni<DataIndex> getDataIndex(Datasource datasource) {
		return sessionFactory.withTransaction(s -> s.fetch(datasource.getDataIndex()));
	}

	public Uni<Set<DataIndex>> getDataIndexes(Datasource datasource) {
		return sessionFactory.withTransaction(s -> s.fetch(datasource.getDataIndexes()));
	}

	public Uni<DataIndex> getDataIndex(long datasourceId) {
		return sessionFactory.withTransaction(s -> findById(
			s,
			datasourceId
		).flatMap(datasource -> s.fetch(datasource.getDataIndex())));
	}

	public Uni<List<DataIndex>> getDataIndexes(long datasourceId) {
		return sessionFactory.withTransaction(s -> findById(s, datasourceId)
			.flatMap(datasource -> s.fetch(datasource.getDataIndexes()))
			.map(ArrayList::new));
	}

	public Uni<Connection<DataIndex>> getDataIndexConnection(
		Long id,
		String after,
		String before,
		Integer first,
		Integer last,
		String searchText,
		Set<SortBy> sortByList,
		boolean notEqual) {

		return findJoinConnection(
			id,
			Datasource_.DATA_INDEXES,
			DataIndex.class,
			dataIndexService.getSearchFields(),
			after,
			before,
			first,
			last,
			searchText,
			sortByList,
			notEqual
		);
	}

	public Uni<Connection<Scheduler>> getSchedulerConnection(
		Long id,
		String after,
		String before,
		Integer first,
		Integer last,
		String searchText,
		Set<SortBy> sortByList,
		boolean notEqual) {

		return findJoinConnection(
			id,
			Datasource_.SCHEDULERS,
			Scheduler.class,
			schedulerService.getSearchFields(),
			after,
			before,
			first,
			last,
			searchText,
			sortByList,
			notEqual
		);
	}

	public Uni<EnrichPipeline> getEnrichPipeline(Datasource datasource) {
		return sessionFactory.withTransaction(s -> s.fetch(datasource.getEnrichPipeline()));
	}

	public Uni<EnrichPipeline> getEnrichPipeline(long datasourceId) {
		return sessionFactory.withTransaction(s -> findById(
			s,
			datasourceId
		).flatMap(datasource -> s.fetch(datasource.getEnrichPipeline())));
	}

	public Uni<Tuple2<Datasource, DataIndex>> setDataIndex(long datasourceId, long dataIndexId) {
		return sessionFactory.withTransaction(s -> findById(s, datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> dataIndexService
				.findById(s, dataIndexId)
				.onItem()
				.ifNotNull()
				.transformToUni(dataIndex -> {
					datasource.setDataIndex(dataIndex);
					return persist(s, datasource).map(d -> Tuple2.of(d, dataIndex));
				})));
	}

	public Uni<Datasource> unsetDataIndex(long datasourceId) {
		return sessionFactory.withTransaction(s -> findById(s, datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> {
				datasource.setDataIndex(null);
				return persist(s, datasource);
			}));
	}

	public Uni<Tuple2<Datasource, EnrichPipeline>> setEnrichPipeline(
		long datasourceId, long enrichPipelineId) {
		return sessionFactory.withTransaction(s -> findById(s, datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> enrichPipelineService
				.findById(s, enrichPipelineId)
				.onItem()
				.ifNotNull()
				.transformToUni(enrichPipeline -> {
					datasource.setEnrichPipeline(enrichPipeline);
					return persist(s, datasource).map(d -> Tuple2.of(d, enrichPipeline));
				})));
	}

	public Uni<Datasource> unsetEnrichPipeline(long datasourceId) {
		return sessionFactory.withTransaction(s -> findById(s, datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> {
				datasource.setEnrichPipeline(null);
				return persist(s, datasource);
			}));
	}

	public Uni<PluginDriver> getPluginDriver(long datasourceId) {
		return sessionFactory.withTransaction(s -> findById(
			s,
			datasourceId
		).flatMap(datasource -> s.fetch(datasource.getPluginDriver())));
	}

	public Uni<Tuple2<Datasource, PluginDriver>> setPluginDriver(
		long datasourceId, long pluginDriverId) {

		return sessionFactory.withTransaction(s -> findById(s, datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> pluginDriverService
				.findById(s, pluginDriverId)
				.flatMap(pluginDriver -> {
					datasource.setPluginDriver(pluginDriver);
					return persist(s, datasource).map(d -> Tuple2.of(d, pluginDriver));
				})));
	}

	public Uni<Datasource> unsetPluginDriver(long datasourceId) {
		return sessionFactory.withTransaction(s -> findById(s, datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> {
				datasource.setPluginDriver(null);
				return persist(s, datasource);
			}));
	}

	public Uni<Tuple2<Datasource, PluginDriver>> createDatasourceAndAddPluginDriver(
		DatasourceDTO datasourceDTO, long pluginDriverId) {

		return sessionFactory.withTransaction(s -> pluginDriverService
			.findById(s, pluginDriverId)
			.onItem()
			.ifNotNull()
			.transformToUni(pluginDriver -> {
				Datasource dataSource = mapper.create(datasourceDTO);
				dataSource.setPluginDriver(pluginDriver);
				return persist(s, dataSource).map(d -> Tuple2.of(d, pluginDriver));
			}));

	}

	public Uni<Datasource> findDatasourceByIdWithPluginDriver(long datasourceId) {
		return sessionFactory.withTransaction((s) -> s.createQuery(
			"select d " + "from Datasource d " + "left join fetch d.pluginDriver where d.id = :id",
			Datasource.class
		).setParameter("id", datasourceId).getSingleResult());
	}

	public Uni<List<DataIndex>> getDataIndexOrphans(long datasourceId) {
		return sessionFactory.withTransaction((s) -> s.createQuery(
			"select di "
			+ "from DataIndex di "
			+ "inner join di.datasource d on di.datasource = d and d.dataIndex <> di "
			+ "where d.id = :id",
			DataIndex.class
		).setParameter("id", datasourceId).getResultList());
	}

	public Uni<Response<Datasource>> createDatasourceConnection(
		DatasourceConnectionDTO datasourceConnection) {

		return Uni
			.createFrom()
			.item(() -> {
				checkExclusiveFields(datasourceConnection);

				var constraintViolations = validator.validate(datasourceConnection);

				if (!constraintViolations.isEmpty()) {
					throw new ConstraintViolationException(constraintViolations);
				}

				return datasourceConnection;
			})
			.flatMap(datasourceConnectionDTO -> sessionFactory.withTransaction((session, transaction) ->
				getOrCreatePluginDriver(session, datasourceConnection)
					.flatMap(pluginDriver -> getOrCreateEnrichPipeline(
							session,
							datasourceConnection
						).flatMap(enrichPipeline -> {
							var datasource = mapper.create(datasourceConnection);

							datasource.setPluginDriver(pluginDriver);
							datasource.setEnrichPipeline(enrichPipeline);

							return create(session, datasource);
						}).flatMap(datasource -> dataIndexService
							.createByDatasource(session, datasource)
							.flatMap(dataIndex -> {
								if (datasourceConnection.getVectorIndexConfigurations() != null) {
									return vectorIndexService.create(
											VectorIndexDTO.builder()
												.name(dataIndex.getName() + "-vector-index")
												.configurations(datasourceConnection.getVectorIndexConfigurations())
												.build())
										.flatMap(vectorIndex -> dataIndexService.bindVectorDataIndex(
											dataIndex.getId(), vectorIndex.getId()))
										.flatMap(dataIndexWithVectorIndex -> {
											datasource.setDataIndex(dataIndexWithVectorIndex);
											return persist(session, datasource);
										});
								}
								else {
									datasource.setDataIndex(dataIndex);
									return persist(session, datasource);
								}
							})
						)
					)
			))
			.onItemOrFailure()
			.transformToUni((datasource, throwable) -> {
				if (throwable != null) {
					if (throwable instanceof ConstraintViolationException constraintViolations) {
						var fieldValidators =
							constraintViolations.getConstraintViolations().stream()
								.map(constraintViolation -> FieldValidator.of(constraintViolation
									.getPropertyPath()
									.toString(), constraintViolation.getMessage()))
								.collect(Collectors.toList());
						return Uni.createFrom().item(Response.of(null, fieldValidators));
					}
					if (throwable instanceof ValidationException validationException) {
						return Uni.createFrom().item(Response.of(
							null,
							List.of(FieldValidator.of("error", validationException.getMessage()))
						));
					}
					return Uni.createFrom().failure(new K9Error(throwable));
				}
				else {
					return Uni.createFrom().item(Response.of(datasource, null));
				}
			});
	}

	private Uni<PluginDriver> getOrCreatePluginDriver(
		Mutiny.Session session,
		DatasourceConnectionDTO datasourceConnectionDTO) {

		var pluginDriverDto = datasourceConnectionDTO.getPluginDriver();

		if (pluginDriverDto != null) {
			return pluginDriverService.create(session, pluginDriverDto);
		}
		else {
			return pluginDriverService.findById(
				session,
				datasourceConnectionDTO.getPluginDriverId()
			);
		}
	}

	private Uni<EnrichPipeline> getOrCreateEnrichPipeline(
		Mutiny.Session session,
		DatasourceConnectionDTO datasourceConnectionDTO) {

		var pipelineDto = datasourceConnectionDTO.getPipeline();
		var pipelineId = datasourceConnectionDTO.getPipelineId();

		if (pipelineDto != null) {
			return enrichPipelineService.create(session, pipelineDto);
		}
		else if (pipelineId != null) {
			return enrichPipelineService.findById(session, pipelineId);
		}
		else {
			return Uni.createFrom().nullItem();
		}
	}

	private void checkExclusiveFields(DatasourceConnectionDTO datasourceConnectionDTO)
	throws ValidationException {

		var pluginDriver = datasourceConnectionDTO.getPluginDriver();
		var pluginDriverId = datasourceConnectionDTO.getPluginDriverId();


		if (pluginDriver == null && pluginDriverId == null) {
			throw new ValidationException(
				"Request must defines one of pluginDriverId or pluginDriver");
		}

		if (pluginDriver != null && pluginDriverId != null) {
			throw new ValidationException(
				"Ambiguous Request: defines pluginDriver or pluginDriverId, exclusively");
		}

		var pipeline = datasourceConnectionDTO.getPipeline();
		var pipelineId = datasourceConnectionDTO.getPipelineId();

		if (pipeline != null && pipelineId != null) {
			throw new ValidationException(
				"Ambiguous Request: defines pipeline or pipelineId, exclusively");
		}

	}

	public record UpdateDatasourceRequest(
		String tenantId, long datasourceId, OffsetDateTime lastIngestionDate, Long newDataIndexId
	) {}
}
