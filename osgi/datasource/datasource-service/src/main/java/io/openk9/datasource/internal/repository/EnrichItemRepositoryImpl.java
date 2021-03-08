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


import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.repository.EnrichItemRepository;
import io.openk9.repository.http.api.RepositoryHttpExtender;
import io.openk9.sql.api.client.Criteria;
import io.openk9.sql.api.client.DatabaseClient;
import io.openk9.sql.api.client.Page;
import io.openk9.sql.api.client.Sort;
import io.openk9.sql.api.entity.BaseReactiveRepository;
import io.openk9.sql.api.entity.EntityMapper;
import io.openk9.sql.api.entity.ReactiveRepository;
import io.openk9.sql.api.event.EntityEventBus;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

@Component(
	immediate = true,
	service = {
		EnrichItemRepository.class,
		RepositoryHttpExtender.class
	}
)
public class EnrichItemRepositoryImpl
	extends BaseReactiveRepository<EnrichItem, Long>
	implements EnrichItemRepository, RepositoryHttpExtender {

	public Mono<EnrichItem> removeEnrichItem(Long enrichItemId) {
		return super.delete(enrichItemId);
	}

	@Override
	public Flux<EnrichItem> findByEnrichPipelineId(Long enrichPipelineId) {
		return findByEnrichPipelineId(enrichPipelineId, Page.DEFAULT);
	}

	@Override
	public Flux<EnrichItem> findByEnrichPipelineId(
		Long enrichPipelineId, Page page) {
		return super.findBy(
			Criteria
				.where("enrichPipelineId")
				.is(enrichPipelineId),
			page
		);
	}

	public Flux<EnrichItem> findByActiveAndEnrichPipelineId(Boolean active, Long enrichPipelineId) {
		return findByActiveAndEnrichPipelineId(active, enrichPipelineId, Page.DEFAULT);
	}

	@Override
	public Flux<EnrichItem> findByActiveAndEnrichPipelineId(
		Boolean active, Long enrichPipelineId, Page page) {
		return super.findBy(
			Criteria
				.where("active")
				.is(active)
				.and("enrichPipelineId")
				.is(enrichPipelineId),
			page
		);

	}

	@Override
	public Flux<EnrichItem> findByActiveAndEnrichPipelineId(
		Boolean active, Long enrichPipelineId, Sort... sorts) {
		return super.findBy(
			Criteria
				.where("enrichPipelineId")
				.is(enrichPipelineId)
				.and("active")
				.is(active),
			sorts
		);
	}

	public Mono<EnrichItem> addEnrichItem(EnrichItem enrichitem) {
        return super.insert(enrichitem);
	}
	public Mono<EnrichItem> addEnrichItem(Integer _position, Boolean active, String jsonConfig, Long enrichPipelineId, String name, String serviceName) {
		return super.insert(
			EnrichItem
				.builder()
				._position(_position)
				.active(active)
				.jsonConfig(jsonConfig)
				.enrichPipelineId(enrichPipelineId)
				.name(name)
				.serviceName(serviceName)
				.build()
		);
	}
	public Mono<EnrichItem> updateEnrichItem(EnrichItem enrichitem) {
		return super.update(enrichitem);
	}
	public Mono<EnrichItem> updateEnrichItem(Integer _position, Boolean active, String jsonConfig, Long enrichItemId, Long enrichPipelineId, String name, String serviceName) {
		return super.update(
			EnrichItem.of(
				enrichItemId, _position, active, jsonConfig, enrichPipelineId,
				name, serviceName
			)
		);
	}
	public Mono<EnrichItem> removeEnrichItem(EnrichItem enrichitem) {
        return super.delete(enrichitem.getEnrichItemId());
	}

	@Override
	public Long parsePrimaryKey(String primaryKey) {
		return Long.valueOf(primaryKey);
	}

	@Override
	public Class<EnrichItem> entityClass() {
		return EnrichItem.class;
	}

	@Override
	public Class<Long> primaryKeyType() {
		return Long.class;
	}

	@Override
	public String primaryKeyName() {
		return "enrichItemId";
	}

	@Override
	public String tableName() {
		return TABLE_NAME;
	}

	@Override
	public Long getPrimaryKey(EnrichItem enrichItem) {
		return enrichItem.getEnrichItemId();
	}

	@Override
	public BiFunction<Row, RowMetadata, EnrichItem> entityMapping() {
		return (row, rowMetadata) -> {

			Long enrichItemId = row.get("enrichItemId", Long.class);
			Integer _position = row.get("_position", Integer.class);
			Boolean active = row.get("active", Boolean.class);
			String jsonConfig = row.get("jsonConfig", String.class);
			Long enrichPipelineId = row.get("enrichPipelineId", Long.class);
			String name = row.get("name", String.class);
			String serviceName = row.get("serviceName", String.class);

			return EnrichItem.of(
				enrichItemId, _position, active, jsonConfig, enrichPipelineId,
				name, serviceName);
		};
	}

	@Reference
	public void setDatabaseClient(DatabaseClient databaseClient) {
		_databaseClient = databaseClient;
	}

	@Reference(
		target = "(|" +
				 "(entity.mapper=io.openk9.datasource.model.EnrichItem)" +
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

	public static final String TABLE_NAME = "ENRICHITEM";

	@Override
	public ReactiveRepository getReactiveRepository() {
		return this;
	}
}

