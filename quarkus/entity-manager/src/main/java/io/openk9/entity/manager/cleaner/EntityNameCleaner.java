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

package io.openk9.entity.manager.cleaner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public interface EntityNameCleaner {

	String getEntityType();

	QueryBuilder cleanEntityName(String tenantId, String entityName);

	default String cleanEntityName(String entityName) {
		return entityName.strip();
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	class DefaultEntityNameCleaner implements EntityNameCleaner {

		private String entityType;

		@Override
		public QueryBuilder cleanEntityName(String tenantId, String entityName) {
			return createQueryBuilder(cleanEntityName(entityName));
		}

		protected QueryBuilder createQueryBuilder(String entityName) {

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			boolQueryBuilder.must(
				QueryBuilders.matchQuery("name.keyword", entityName)
			);

			boolQueryBuilder.must(
				QueryBuilders.matchQuery("type.keyword", getEntityType())
			);

			return boolQueryBuilder;

		}

	}

}