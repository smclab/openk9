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

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class PersonEntityNameCleaner extends DefaultEntityNameCleaner {

	@Override
	public String getEntityType() {
		return "person";
	}

	protected QueryBuilder createQueryBuilder(String entityName) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		String[] tokens = entityName.split("\\s+");

		for (String token : tokens) {

			if (token.contains(".")) {
				String replace = token.replace(".", "");
				boolQueryBuilder.must(
					QueryBuilders.prefixQuery("name", replace)
				);
			}
			else {

				boolQueryBuilder.must(
					QueryBuilders.matchQuery("name", token)
				);

			}

		}

		boolQueryBuilder.filter(
			QueryBuilders.matchQuery("type.keyword", getEntityType())
		);

		return boolQueryBuilder;

	}

}
