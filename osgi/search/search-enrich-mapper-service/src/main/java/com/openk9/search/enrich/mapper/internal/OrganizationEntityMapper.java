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

package com.openk9.search.enrich.mapper.internal;

import com.openk9.search.enrich.mapper.api.EntityMapper;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = EntityMapper.class)
public class OrganizationEntityMapper implements EntityMapper {

	@Override
	public String getType() {
		return "organization";
	}

	@Override
	public QueryBuilder query(String term) {
		return QueryBuilders
			.matchQuery("name", term)
			.operator(Operator.AND);
	}

	@Override
	public String[] getSearchKeywords() {
		return new String[] {
			"name"
		};
	}
}
