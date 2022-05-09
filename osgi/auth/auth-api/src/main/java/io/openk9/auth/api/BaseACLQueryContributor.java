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

package io.openk9.auth.api;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Map;

public abstract class BaseACLQueryContributor implements ACLQueryContributor {

	public abstract String getPluginName();

	@Override
	public void accept(UserInfo userInfo, BoolQueryBuilder boolQueryBuilder) {

		Map<String, List<String>> realmAccess = userInfo.getRealmAccess();

		List<String> roles = realmAccess.getOrDefault("roles", List.of());

		if (!roles.isEmpty()) {

			String fieldName = "acl." + getPluginName() + ".rolesName.keyword";

			boolQueryBuilder.should(QueryBuilders.termsQuery(fieldName, roles));

		}

	}

}