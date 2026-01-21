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

package io.openk9.datasource.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

public enum UserField {

	USERNAME {
		@Override
		public List<String> getTerms(JsonWebToken jwt) {

			String username = jwt.getSubject();

			return username != null ? List.of(username) : List.of();
		}
	},
	ROLES {
		@Override
		public List<String> getTerms(JsonWebToken jwt) {

			Set<String> groups = jwt.getGroups();

			return groups != null ? new ArrayList<>(groups) : List.of();
		}
	},
	EMAIL {
		@Override
		public List<String> getTerms(JsonWebToken jwt) {

			String email = jwt.getClaim(Claims.email);

			return email != null ? List.of(email) : List.of();
		}
	};

	public abstract List<String> getTerms(JsonWebToken jwt);

}
