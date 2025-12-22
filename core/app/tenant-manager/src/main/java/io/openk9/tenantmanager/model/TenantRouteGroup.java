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

package io.openk9.tenantmanager.model;

import java.util.List;

import lombok.Getter;

/**
 * The Tenant Manager view on the routes that would be configured for
 * security in the Api Gateway.
 * In the Tenant Manager we have a generic view on some groups of routes.
 */
public enum TenantRouteGroup {

	// TODO: There is a lot of code duplication here...
	//  It's related to the tenant-manager events
	//  and what happens in the api-gateway.
	//  This kind of configuration is showing a flaw in the design
	// 	already discussed in some analysis.
	// 	Tenant manager shouldn't have a view on the routes and security
	// 	of every specific Tenant.
	// 	A more meaningful design permits to tenant manager to configure
	// 	just the administration route, all the other routes would be configured
	//  by the tenant administration that ideally know the domain where this
	//  tenant would be served.

	/**
	 * This group defines the list of APIs used for administration scopes.
	 */
	ADMINISTRATION(List.of("/api/datasource/*")),

	/**
	 * This group defines the list of APIs that support the front-end for
	 * creating a useful interface.
	 * <p>
	 * This includes:
	 *  <li> the OAuth2 configurations; </li>
	 *  <li> the current Bucket (the container where the end-user is going to do his search query); </li>
	 *  <li> the card templates, a component templated that could be used to show a specific
	 *  document type with a special UI. </li>
	 *
	 */
	PUBLIC(List.of(
		"/api/datasource/oauth2/*",
		"/api/datasource/buckets/current/*",
		"/api/datasource/templates/*")
	),

	/**
	 * This group include all the APIs used for search and content retrieving scopes.
	 * <p>
	 * This includes:
	 * <ul>
	 *   <li> Search APIs </li>
	 *   <li> Retrieval-Augmented Generation (RAG) APIs </li>
	 * </ul>
	 */
	SEARCH_EXPERIENCE(List.of("/api/searcher/*", "/api/rag/**")),

	/**
	 * This group contains the api used during the ingestion phase.
	 * <p>
	 * This is generally used by the Connectors, so it's used for
	 * defining a machine to machine method authentication.
	 */
	INGESTION(List.of("/api/ingestion/**"));

	@Getter
	private final List<String> paths;

	TenantRouteGroup(List<String> paths) {
		this.paths = paths;
	}

}
