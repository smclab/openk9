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

package io.openk9.datasource.internal.repository;


import io.openk9.model.Datasource;
import io.openk9.model.EnrichPipeline;
import io.openk9.datasource.repository.EnrichPipelineRepository;
import io.openk9.repository.http.api.RepositoryHttpExtender;
import io.openk9.sql.api.InitSql;
import io.openk9.sql.api.client.Criteria;
import io.openk9.sql.api.client.DatabaseClient;
import io.openk9.sql.api.entity.BaseReactiveRepository;
import io.openk9.sql.api.entity.EntityMapper;
import io.openk9.sql.api.entity.ReactiveRepository;
import io.openk9.sql.api.event.EntityEventBus;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Component(
	immediate = true,
	service = {
		EnrichPipelineRepository.class,
		RepositoryHttpExtender.class
	}
)
public class EnrichPipelineRepositoryImpl
	extends BaseReactiveRepository<EnrichPipeline, Long>
	implements EnrichPipelineRepository, RepositoryHttpExtender {

	@Override
	public ReactiveRepository getReactiveRepository() {
		return this;
	}

	public Mono<EnrichPipeline> removeEnrichPipeline(Long enrichPipelineId) {
		return super.delete(enrichPipelineId);
	}
	public Mono<EnrichPipeline> findByDatasourceId(Long datasourceId) {
		return super.findOneBy(
			Criteria.where("datasourceId").is(datasourceId));
	}
	public Mono<EnrichPipeline> findByEnrichPipelineIdAndDatasourceId(
		Long datasourceId, Long enrichPipelineId) {

		return super.findOneBy(
			Criteria
				.where("datasourceId")
				.is(datasourceId)
				.and(
					Criteria
						.where("enrichPipelineId")
						.is(enrichPipelineId)
				)
		);
	}
	public Mono<EnrichPipeline> addEnrichPipeline(
		EnrichPipeline enrichpipeline) {

        return super.insert(enrichpipeline);
	}
	public Mono<EnrichPipeline> addEnrichPipeline(
		Boolean active, Long datasourceId, String name) {

		return super.insert(
			EnrichPipeline
				.builder()
				.active(active)
				.datasourceId(datasourceId)
				.name(name)
				.build()
			);
	}
	public Mono<EnrichPipeline> updateEnrichPipeline(
		EnrichPipeline enrichpipeline) {

		return super.update(enrichpipeline);
	}
	public Mono<EnrichPipeline> updateEnrichPipeline(
		Boolean active, Long datasourceId, Long enrichPipelineId,
		String name) {

		return super.update(
			EnrichPipeline.of(enrichPipelineId, active, datasourceId, name)
		);

	}
	public Mono<EnrichPipeline> removeEnrichPipeline(EnrichPipeline enrichpipeline) {
        return super.delete(enrichpipeline.getEnrichPipelineId());
	}

	@Override
	public Long parsePrimaryKey(String primaryKey) {
		return Long.valueOf(primaryKey);
	}

	@Override
	public Class<EnrichPipeline> entityClass() {
		return EnrichPipeline.class;
	}

	@Override
	public Class<Long> primaryKeyType() {
		return Long.class;
	}

	@Override
	public String primaryKeyName() {
		return "enrichPipelineId";
	}

	@Override
	public String tableName() {
		return TABLE_NAME;
	}

	@Override
	public Long getPrimaryKey(EnrichPipeline enrichPipeline) {
		return enrichPipeline.getEnrichPipelineId();
	}

	@Override
	public BiFunction<Row, RowMetadata, EnrichPipeline> entityMapping() {
		return (row, rowMetadata) -> {

			Long enrichPipelineId = row.get("enrichPipelineId", Long.class);
			Boolean active = row.get("active", Boolean.class);
			Long datasourceId = row.get("datasourceId", Long.class);
			String name = row.get("name", String.class);

			return EnrichPipeline.of(
				enrichPipelineId, active, datasourceId, name);
		};
	}

	@Reference
	public void setDatabaseClient(DatabaseClient databaseClient) {
		_databaseClient = databaseClient;
	}

	@Reference(
		target = "(|" +
				 "(entity.mapper=io.openk9.model.EnrichPipeline)" +
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

	@Reference(
		service = InitSql.Executed.class,
		target = "(init-sql=io.openk9.datasource.internal.InitSqlImpl)",
		bind = "setExecuted"
	)
	public void setExecuted(InitSql.Executed executed) {}

	public static final String TABLE_NAME = "ENRICHPIPELINE";

}

