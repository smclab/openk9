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

import io.openk9.datasource.mapper.VectorIndexMapper;
import io.openk9.datasource.model.VectorIndex;
import io.openk9.datasource.model.VectorIndex_;
import io.openk9.datasource.model.dto.VectorIndexDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VectorIndexService extends BaseK9EntityService<VectorIndex, VectorIndexDTO> {

	VectorIndexService(VectorIndexMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<VectorIndex> getEntityClass() {
		return VectorIndex.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{VectorIndex_.NAME, VectorIndex_.DESCRIPTION};
	}

	public Uni<VectorIndex> findByDataIndexId(long dataIndexId) {

		return sessionFactory.withTransaction((s, t) -> s.createQuery(
				"from VectorIndex vi join vi.dataIndex di where di.id = :dataIndexId",
				VectorIndex.class
			)
			.setParameter("dataIndexId", dataIndexId)
			.getSingleResultOrNull());
	}
}
