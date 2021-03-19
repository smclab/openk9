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

package io.openk9.sql.api.entity;

import io.openk9.sql.api.client.Criteria;
import io.openk9.sql.api.client.CriteriaDefinition;
import io.openk9.sql.api.client.DatabaseClient;
import io.openk9.sql.api.client.Page;
import io.openk9.sql.api.client.Sort;
import io.openk9.sql.api.event.EntityEvent;
import io.openk9.sql.api.event.EntityEventBus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public abstract class BaseReactiveRepository<ENTITY, PK>
	implements ReactiveRepository<ENTITY, PK> {

	@Override
	public Mono<ENTITY> insert(Mono<ENTITY> mono) {
		return mono
			.flatMap(entity ->
				_databaseClient
					.insert()
					.into(tableName())
					.value(_insertMapper.apply(entity))
					.map(entityMapping())
					.first()
			)
			.doOnNext(e -> _entityEventBus.sendEvent(
				EntityEvent.insert(e.getClass(), e)));
	}

	@Override
	public Mono<ENTITY> insert(ENTITY entity) {
		return insert(Mono.just(entity));
	}

	@Override
	public Mono<ENTITY> patch(PK pk, Map<String, Object> props) {

		return _databaseClient
			.update()
			.from(tableName())
			.value(props)
			.matching(
				Criteria
					.where(primaryKeyName())
					.is(pk)
			)
			.map(entityMapping())
			.first()
			.doOnNext(e -> _entityEventBus.sendEvent(
				EntityEvent.update(e.getClass(), e)));

	}

	@Override
	public Mono<ENTITY> update(Mono<ENTITY> mono) {
		return mono
			.flatMap(entity ->
				_databaseClient
					.update()
					.from(tableName())
					.value(_updateMapper.apply(entity))
					.matching(
						Criteria
							.where(primaryKeyName())
							.is(getPrimaryKey(entity))
					)
					.map(entityMapping())
					.first()
			)
			.doOnNext(e -> _entityEventBus.sendEvent(
				EntityEvent.update(e.getClass(), e)));
	}

	@Override
	public Mono<ENTITY> update(ENTITY entity) {
		return update(Mono.just(entity));
	}

	@Override
	public Mono<ENTITY> delete(Mono<PK> mono) {
		return mono
			.flatMap(primaryKey ->
				_databaseClient
					.delete()
					.from(tableName())
					.matching(Criteria.where(primaryKeyName()).is(primaryKey))
					.map(entityMapping())
					.first()
			)
			.doOnNext(e -> _entityEventBus.sendEvent(
				EntityEvent.delete(e.getClass(), e)));
	}

	@Override
	public Mono<ENTITY> delete(PK primaryKey) {
		return delete(Mono.just(primaryKey));
	}

	@Override
	public Mono<ENTITY> deleteEntity(Mono<ENTITY> mono) {
		return mono.flatMap(entity -> delete(getPrimaryKey(entity)));
	}

	@Override
	public Mono<ENTITY> deleteEntity(ENTITY entity) {
		return delete(getPrimaryKey(entity));
	}

	@Override
	public Flux<ENTITY> findAll() {
		return findAll(Page.DEFAULT);
	}

	@Override
	public Flux<ENTITY> findAll(Page page) {
		return _databaseClient
			.select()
			.from(tableName())
			.page(page)
			.map(entityMapping())
			.all();
	}

	@Override
	public Flux<ENTITY> findAll(Sort... sorts) {
		return _databaseClient
			.select()
			.from(tableName())
			.orderBy(sorts)
			.map(entityMapping())
			.all();
	}

	@Override
	public Flux<ENTITY> findBy(
		CriteriaDefinition criteriaDefinition) {
		return findBy(criteriaDefinition, Page.DEFAULT);
	}

	@Override
	public Flux<ENTITY> findBy(
		CriteriaDefinition criteriaDefinition, Page page) {

		return _databaseClient
			.select()
			.from(tableName())
			.matching(criteriaDefinition)
			.page(page)
			.map(entityMapping())
			.all();
	}

	@Override
	public Flux<ENTITY> findBy(
		CriteriaDefinition criteriaDefinition, Sort... sorts) {
		return _databaseClient
			.select()
			.from(tableName())
			.matching(criteriaDefinition)
			.orderBy(sorts)
			.map(entityMapping())
			.all();
	}

	@Override
	public Mono<ENTITY> findOneBy(
		CriteriaDefinition criteriaDefinition) {
		return findOneBy(criteriaDefinition, Page.DEFAULT);
	}

	@Override
	public Mono<ENTITY> findOneBy(
		CriteriaDefinition criteriaDefinition, Page page) {
		return _databaseClient
			.select()
			.from(tableName())
			.matching(criteriaDefinition)
			.page(page)
			.map(entityMapping())
			.one();
	}

	@Override
	public Mono<ENTITY> findOneBy(
		CriteriaDefinition criteriaDefinition, Sort... sorts) {
		return _databaseClient
			.select()
			.from(tableName())
			.matching(criteriaDefinition)
			.orderBy(sorts)
			.map(entityMapping())
			.one();
	}

	@Override
	public Mono<ENTITY> findByPrimaryKey(PK primaryKey) {
		return findOneBy(Criteria.where(primaryKeyName()).is(primaryKey));
	}

	@Override
	public final Flux<ENTITY> findByPrimaryKeys(Collection<PK> primaryKeys) {
		return findBy(Criteria.where(primaryKeyName()).in(primaryKeys));
	}

	protected void setDatabaseClient(DatabaseClient databaseClient) {
		_databaseClient = databaseClient;
	}

	protected void setUpdateMapper(
		Function<Object, Map<String, Object>> updateMapper) {
		_updateMapper = updateMapper;
	}

	protected void setInsertMapper(
		Function<Object, Map<String, Object>> insertMapper) {
		_insertMapper = insertMapper;
	}

	protected void setEntityEventBus(EntityEventBus entityEventBus) {
		_entityEventBus = entityEventBus;
	}

	protected DatabaseClient _databaseClient;

	protected Function<Object, Map<String, Object>> _updateMapper;

	protected Function<Object, Map<String, Object>> _insertMapper;

	protected EntityEventBus _entityEventBus;

}
