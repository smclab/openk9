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

package io.openk9.datasource.service.util;

import io.openk9.common.model.EntityService;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Set;

public interface K9EntityService<ENTITY extends K9Entity, DTO extends K9EntityDTO>
	extends EntityService<ENTITY, DTO> {
	Uni<Page<ENTITY>> findAllPaginated(
		Pageable pageable, String searchText);

	Uni<Page<ENTITY>> findAllPaginated(
		Pageable pageable, Filter filter);

	Uni<Page<ENTITY>> findAllPaginated(Pageable pageable);

	<T extends K9Entity> Uni<Page<T>> findAllPaginatedJoin(
		Long[] entityIds, String joinField, Class<T> joinType, int limit,
		String sortBy,
		long afterId, long beforeId, String searchText);

	<T extends K9Entity> Uni<Page<T>> findAllPaginatedJoin(
		Long[] entityIds, String joinField, Class<T> joinType, int limit,
		String sortBy,
		long afterId, long beforeId, Filter filter);

	Uni<Page<ENTITY>> findAllPaginated(
		int limit, String sortBy, long afterId, long beforeId, Filter filter);

	Uni<ENTITY> findById(long id);

	Uni<List<ENTITY>> findByIds(Set<Long> ids);

	Uni<ENTITY> create(ENTITY entity);

	Uni<ENTITY> deleteById(long entityId);
}
