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

package com.openk9.datasource.internal.repository;


import com.openk9.datasource.model.Datasource;
import com.openk9.datasource.model.EnrichPipeline;
import com.openk9.datasource.repository.DatasourceRepository;
import com.openk9.datasource.repository.EnrichItemRepository;
import com.openk9.datasource.repository.EnrichPipelineRepository;
import com.openk9.datasource.repository.TenantRepository;
import com.openk9.datasource.util.DatasourceContext;
import com.openk9.repository.http.api.RepositoryHttpExtender;
import com.openk9.sql.api.client.Criteria;
import com.openk9.sql.api.client.DatabaseClient;
import com.openk9.sql.api.client.Page;
import com.openk9.sql.api.client.Sort;
import com.openk9.sql.api.entity.BaseReactiveRepository;
import com.openk9.sql.api.entity.EntityMapper;
import com.openk9.sql.api.event.EntityEventBus;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.Collections;
import java.util.function.BiFunction;

@Component(
	immediate = true,
	service = {
		DatasourceRepository.class,
		RepositoryHttpExtender.class
	}
)
public class DatasourceRepositoryImpl
	extends BaseReactiveRepository<Datasource, Long>
	implements DatasourceRepository, RepositoryHttpExtender {

	public Mono<Datasource> removeDatasource(Long datasourceId) {
		return super.delete(datasourceId);
	}
	public Mono<Datasource> findByName(String name) {
		return super.findOneBy(Criteria.where("name").is(name));
	}
	public Flux<Datasource> findByTenantId(Long tenantId) {
		return super.findBy(Criteria.where("tenantId").is(tenantId));
	}

	@Override
	public Flux<Datasource> findByTenantIdAndIsActive(Long tenantId) {
		return super.findBy(
			Criteria
				.where("active").isTrue()
				.and("tenantId").is(tenantId)
		);
	}

	@Override
	public Flux<Datasource> findByTenantIdAndNotActive(Long tenantId) {
		return super.findBy(
			Criteria
				.where("active").isFalse()
				.and("tenantId").is(tenantId)
		);
	}

	@Override
	public Flux<Datasource> findByTenantId(Long tenantId, Page page) {
		return super.findBy(
			Criteria.where("tenantId").is(tenantId), page);
	}

	public Mono<Datasource> addDatasource(Datasource datasource) {
        return super.insert(datasource);
	}

	@Override
	public Mono<Datasource> addDatasource(
		Boolean active, String description, String jsonConfig,
		Instant lastIngestionDate, String name, Long tenantId, String scheduling,
		String driverServiceName) {

		return super.insert(
			Datasource
				.builder()
				.active(active)
				.description(description)
				.jsonConfig(jsonConfig)
				.lastIngestionDate(lastIngestionDate)
				.name(name)
				.tenantId(tenantId)
				.scheduling(scheduling)
				.driverServiceName(driverServiceName)
				.build()
		);
	}
	public Mono<Datasource> updateDatasource(Datasource datasource) {
		return super.update(datasource);
	}

	@Override
	public Mono<Void> updateLastIngestionDate(
		Long datasourceId, Instant lastIngestionDate) {

		return _databaseClient
			.update()
			.from(TABLE_NAME)
			.value("lastIngestionDate", lastIngestionDate)
			.matching(Criteria.where("datasourceId").is(datasourceId))
			.then();
	}

	public Mono<Datasource> updateDatasource(
		Boolean active, Long datasourceId, String description, String jsonConfig,
		Instant lastIngestionDate, String name, Long tenantId, String scheduling,
		String driverServiceName) {

		return super.update(
			Datasource.of(
				datasourceId, active, description, jsonConfig,
				lastIngestionDate, name, tenantId, scheduling,
				driverServiceName)
		);
	}
	public Mono<Datasource> removeDatasource(Datasource datasource) {
        return super.delete(datasource.getDatasourceId());
	}
	public Mono<Datasource> findByPrimaryKey(Long datasourceId) {
		return super.findOneBy(
			Criteria.where(primaryKeyName()).is(datasourceId));
	}

	public Flux<Datasource> findAll() {
		return super.findAll();
	}

	@Override
	public Flux<Datasource> findAll(boolean active) {
		return findBy(Criteria.where("active").is(active));
	}

	@Override
	public Long parsePrimaryKey(String primaryKey) {
		return Long.valueOf(primaryKey);
	}

	@Override
	public Mono<DatasourceContext> findContext(Long datasourceId) {
		return findByPrimaryKey(datasourceId)
			.zipWhen(d ->
				_tenantRepository.findByPrimaryKey(d.getTenantId()))
			.zipWhen(
				t2 -> _enrichPipelineRepository
					.findByDatasourceId(t2.getT1().getDatasourceId())
					.defaultIfEmpty(_EMPTY),
					(t2, ep) -> Tuples.of(t2.getT1(), t2.getT2(), ep)
			)
			.zipWhen(
				t3 -> _enrichItemRepository
					.findByActiveAndEnrichPipelineId(
						true, t3.getT3().getEnrichPipelineId(),
						Sort.asc("_position")
					)
					.collectList()
					.defaultIfEmpty(Collections.emptyList()),
				(t3, list) -> DatasourceContext.of(
					t3.getT1(), t3.getT2(),
					t3.getT3() == _EMPTY ? null : t3.getT3(), list));
	}

	@Override
	public Class<Datasource> entityClass() {
		return Datasource.class;
	}

	@Override
	public Class<Long> primaryKeyType() {
		return Long.class;
	}

	@Override
	public String primaryKeyName() {
		return "datasourceId";
	}

	@Override
	public String tableName() {
		return TABLE_NAME;
	}

	@Override
	public Long getPrimaryKey(Datasource datasource) {
		return datasource.getDatasourceId();
	}

	@Override
	public BiFunction<Row, RowMetadata, Datasource> entityMapping() {
		return (row, rowMetadata) -> {

			Long datasourceId = row.get("datasourceId", Long.class);
			Boolean active = row.get("active", Boolean.class);
			String description = row.get("description", String.class);
			String jsonConfig = row.get("jsonConfig", String.class);
			Instant lastIngestionDate = row.get("lastIngestionDate", Instant.class);
			String name = row.get("name", String.class);
			Long tenantId = row.get("tenantId", Long.class);
			String scheduling = row.get("scheduling", String.class);
			String driverServiceName =
				row.get("driverServiceName", String.class);

			return Datasource.of(
				datasourceId, active, description, jsonConfig,
				lastIngestionDate, name, tenantId, scheduling, driverServiceName);
		};
	}

	@Override
	public DatasourceRepository getReactiveRepository() {
		return this;
	}

	@Reference
	public void setDatabaseClient(DatabaseClient databaseClient) {
		_databaseClient = databaseClient;
	}

	@Reference(
		target = "(|" +
				 "(entity.mapper=com.openk9.datasource.model.Datasource)" +
				 "(entity.mapper=default)" +
				 ")",
		service = EntityMapper.class,
		policyOption = ReferencePolicyOption.GREEDY,
		bind = "setEntityMapper"
	)
	public void setEntityMapper(EntityMapper entityMapper) {
		_updateMapper = entityMapper.toMap(Datasource.class);
		_insertMapper = entityMapper.toMapWithoutPK(Datasource.class);
	}

	@Reference
	public void setEntityEventBus(EntityEventBus entityEventBus) {
		_entityEventBus = entityEventBus;
	}

	@Reference
	private EnrichItemRepository _enrichItemRepository;

	@Reference
	private EnrichPipelineRepository _enrichPipelineRepository;

	@Reference
	private TenantRepository _tenantRepository;

	public static final String TABLE_NAME = "DATASOURCE";

	private static final EnrichPipeline _EMPTY =
		EnrichPipeline.builder().build();

}

