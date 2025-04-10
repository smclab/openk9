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

import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.datasource.mapper.QueryParserConfigMapper;
import io.openk9.datasource.model.QueryAnalysis_;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

@ApplicationScoped
public class QueryParserConfigService extends BaseK9EntityService<QueryParserConfig, QueryParserConfigDTO> {
	 QueryParserConfigService(QueryParserConfigMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public Class<QueryParserConfig> getEntityClass() {
		return QueryParserConfig.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {QueryAnalysis_.NAME, QueryAnalysis_.DESCRIPTION};
	}

}
