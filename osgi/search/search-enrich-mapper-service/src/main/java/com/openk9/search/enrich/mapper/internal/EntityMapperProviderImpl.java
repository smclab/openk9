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
import com.openk9.search.enrich.mapper.api.EntityMapperProvider;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component(
	immediate = true,
	service = EntityMapperProvider.class
)
public class EntityMapperProviderImpl implements EntityMapperProvider {

	@Override
	public EntityMapper getEntityMapper(String type) {
		return _entityMapperMap.getOrDefault(type, _DEFAULT_ENTITY_MAPPER);
	}

	@Override
	public QueryBuilder query(String type, String term) {
		return getEntityMapper(type).query(term);
	}

	@Override
	public Collection<EntityMapper> getEntityMappers() {
		return _entityMapperMap.values();
	}

	@Reference(
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		cardinality = ReferenceCardinality.AT_LEAST_ONE,
		service = EntityMapper.class,
		bind = "addEntityMapper",
		unbind = "removeEntityMapper"
	)
	public void addEntityMapper(EntityMapper entityMapper) {
		_entityMapperMap.put(entityMapper.getType(), entityMapper);
	}

	public void removeEntityMapper(EntityMapper entityMapper) {
		_entityMapperMap.remove(entityMapper.getType());
	}

	private final Map<String, EntityMapper> _entityMapperMap = new HashMap<>();

	private static final EntityMapper _DEFAULT_ENTITY_MAPPER =
		new EntityMapper() {
			@Override
			public String getType() {
				return "default";
			}

			@Override
			public QueryBuilder query(String term) {
				return QueryBuilders
					.matchQuery("name", term)
					.operator(Operator.AND);
			}
		};

}
