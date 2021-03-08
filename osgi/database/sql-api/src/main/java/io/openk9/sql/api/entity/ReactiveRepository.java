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

import io.openk9.sql.api.client.CriteriaDefinition;
import io.openk9.sql.api.client.Page;
import io.openk9.sql.api.client.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

public interface ReactiveRepository<ENTITY, PK>
	extends EntityDefinition<ENTITY, PK> {

	Mono<ENTITY> insert(Mono<ENTITY> entity);

	Mono<ENTITY> insert(ENTITY entity);

	Mono<ENTITY> patch(PK pk, Map<String, Object> props);

	Mono<ENTITY> update(Mono<ENTITY> entity);

	Mono<ENTITY> update(ENTITY entity);

	Mono<ENTITY> delete(Mono<PK> primaryKey);

	Mono<ENTITY> delete(PK primaryKey);

	Mono<ENTITY> deleteEntity(Mono<ENTITY> entity);

	Mono<ENTITY> deleteEntity(ENTITY entity);

	Flux<ENTITY> findAll();

	Flux<ENTITY> findAll(Page page);

	Flux<ENTITY> findAll(Sort...sorts);

	Flux<ENTITY> findBy(CriteriaDefinition criteriaDefinition);

	Flux<ENTITY> findBy(CriteriaDefinition criteriaDefinition, Page page);

	Flux<ENTITY> findBy(CriteriaDefinition criteriaDefinition, Sort...sorts);

	Mono<ENTITY> findOneBy(CriteriaDefinition criteriaDefinition);

	Mono<ENTITY> findOneBy(CriteriaDefinition criteriaDefinition, Page page);

	Mono<ENTITY> findOneBy(
		CriteriaDefinition criteriaDefinition, Sort...sorts);

	Mono<ENTITY> findByPrimaryKey(PK primaryKey);

	Flux<ENTITY> findByPrimaryKeys(Collection<PK> primaryKeys);

	PK parsePrimaryKey(String primaryKey);

}
