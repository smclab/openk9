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

import java.util.Objects;
import java.util.function.BiConsumer;

public interface ACLQueryContributor extends BiConsumer<UserInfo, BoolQueryBuilder> {

	default String driverServiceName() {
		return this.getClass().getName();
	}

	default String fieldName() {
		return "rolesName.keyword";
	}

	default ACLQueryContributor andThen(ACLQueryContributor after) {
		Objects.requireNonNull(after);

		return (l, r) -> {
			accept(l, r);
			after.accept(l, r);
		};

	}

	ACLQueryContributor NOTHING = (userInfo, booleanClauses) -> {};

}
