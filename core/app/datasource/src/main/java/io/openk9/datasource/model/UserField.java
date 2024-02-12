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

import io.openk9.datasource.model.util.JWT;

import java.util.List;
import java.util.Map;

public enum UserField {
	NAME {
		@Override
		public List<String> getTerms(JWT jwt) {

			String givenName = jwt.getGivenName();

			return List.of(givenName);
		}
	},
	SURNAME {
		@Override
		public List<String> getTerms(JWT jwt) {

			String familyName = jwt.getFamilyName();

			return List.of(familyName);
		}
	},
	NAME_SURNAME {
		@Override
		public List<String> getTerms(JWT jwt) {

			String name_surname =
				jwt.getGivenName() + " " + jwt.getFamilyName();

			return List.of(name_surname);
		}
	},
	USERNAME {
		@Override
		public List<String> getTerms(JWT jwt) {

			String username = jwt.getPreferredUsername();

			return List.of(username);
		}
	},
	EMAIL {
		@Override
		public List<String> getTerms(JWT jwt) {

			String email = jwt.getEmail();

			return List.of(email);
		}
	},
	ROLES {
		@Override
		public List<String> getTerms(JWT jwt) {

			Map<String, List<String>> realmAccess = jwt.getRealmAccess();

			if (realmAccess != null) {
				return realmAccess.get("roles");
			}
			return List.of();
		}
	};

	public abstract List<String> getTerms(JWT jwt);

}
