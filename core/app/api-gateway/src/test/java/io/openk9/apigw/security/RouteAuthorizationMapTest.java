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

package io.openk9.apigw.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@DisplayName("RouteAuthorizationMap")
class RouteAuthorizationMapTest {

	@Nested
	@DisplayName("allows()")
	class Allows {

		@Test
		@DisplayName("NO_AUTH route accepts AnonymousAuthenticationToken")
		void anonymousTokenAllowedOnNoAuthRoute() {
			Authentication anonymous = new AnonymousAuthenticationToken(
				"test-key",
				"anonymousUser",
				List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
			);

			RouteAuthorizationMap map = RouteAuthorizationMap.of();

			assertThat(map.allows(ApiRoute.SEARCHER, anonymous)).isTrue();
		}

		@Test
		@DisplayName("OAUTH2 route rejects AnonymousAuthenticationToken")
		void anonymousTokenRejectedOnOauth2Route() {
			Authentication anonymous = new AnonymousAuthenticationToken(
				"test-key",
				"anonymousUser",
				List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
			);

			RouteAuthorizationMap map = RouteAuthorizationMap.of();

			assertThat(map.allows(ApiRoute.DATASOURCE, anonymous)).isFalse();
		}
	}

	@Nested
	@DisplayName("AuthorizationSchemeToken")
	class SchemeToken {

		@Test
		@DisplayName("NO_AUTH matches Spring's AnonymousAuthenticationToken")
		void noAuthMatchesAnonymousToken() {
			assertThat(
				AuthorizationSchemeToken.NO_AUTH
					.match(AnonymousAuthenticationToken.class)
			).isTrue();
		}
	}

}
