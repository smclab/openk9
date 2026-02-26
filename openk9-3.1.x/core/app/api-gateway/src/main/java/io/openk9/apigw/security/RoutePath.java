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

import lombok.Getter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Enumeration of the routes managed by the API Gateway.
 * <p>
 * The defined routes are used both when building the
 * {@link org.springframework.cloud.gateway.route.RouteLocator}
 * and when configuring security mappings in
 * {@link Tenant}.
 * </p>
 *
 * <p><strong>Note:</strong>
 * <ul>
 *   <li>The order of the constants is significant. Routes are evaluated in
 *   declaration order, meaning that more specific routes (e.g., {@link #SEARCHER})
 *   should be declared before more generic routes (e.g., {@link #ANY}).</li>
 *   <li>This enum is intended to be handled exhaustively in a {@code switch}
 *   construct <em>without</em> a {@code default} branch. This ensures that when a
 *   new route is added, the compiler forces developers to update all switch-based
 *   route definitions accordingly.</li>
 * </ul>
 * </p>
 *
 * @see org.springframework.cloud.gateway.route.RouteLocator
 * @see org.springframework.util.AntPathMatcher
 * @see Tenant
 */
@Getter
public enum RoutePath {

	/**
	 * Route for the datasource service, that doesn't need to be protected.
	 * Matches requests under {@code /api/datasource/oauth2/settings.js}
	 */
	DATASOURCE_OAUTH2_SETTINGS("/api/datasource/oauth2/settings.js"),

	/**
	 * Route for the datasource service, that doesn't need to be protected.
	 * Matches requests under {@code /api/datasource/buckets/current/**}
	 */
	DATASOURCE_CURRENT_BUCKET("/api/datasource/buckets/current/**"),

	/**
	 * Route for the datasource service, that doesn't need to be protected.
	 * Matches requests under {@code /api/datasource/templates/current/**}
	 */
	DATASOURCE_TEMPLATES("/api/datasource/templates/**"),

	/**
	 * Route for the datasource service.
	 * Matches requests under {@code /api/datasource/**}.
	 */
	DATASOURCE("/api/datasource/**"),

	/**
	 * Route for the searcher service.
	 * Matches requests under {@code /api/searcher/**}.
	 */
	SEARCHER("/api/searcher/**"),

	/**
	 * Route for the rag service.
	 * Matches requests under {@code /api/rag/**}.
	 */
	RAG("/api/rag/**"),

	/**
	 * Catch-all route that matches any request.
	 * Matches requests under {@code /**}.
	 */
	ANY("/**");

	private final String antPattern;

	RoutePath(String antPattern) {
		this.antPattern = antPattern;
	}

	/**
	 * Return the first route that match the path, like {@link RoutePath#valueOf(String)}
	 *
	 * @param path the request path
	 * @return the enum constant that matched the specified path
	 */
	public static RoutePath matchOf(String path) {

		for (RoutePath routePath : values()) {
			if (ANT_PATH_MATCHER.match(routePath.getAntPattern(), path)) {
				return routePath;
			}
		}

		throw new IllegalArgumentException();
	}

	public static String[] antPatterns() {
		if (ANT_PATTERNS != null) {
			return ANT_PATTERNS;
		}
		var n = RoutePath.values().length;
		var patterns = new String[n];

		for (int i = 0; i < n; i++) {
			patterns[i] = RoutePath.values()[i].getAntPattern();
		}

		ANT_PATTERNS = patterns;

		return ANT_PATTERNS;
	}

	private final static PathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
	private static String[] ANT_PATTERNS = null;

}
