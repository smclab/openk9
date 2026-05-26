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

package io.openk9.tenantmanager.resource;

public class TenantManagerRequestExamples {

	public static final String INIT_TENANT_REQUEST =
		"""
			{
			\t"tenantName": "demo"
			}""";

	public static final String INIT_TENANT_RESPONSE =
		"""
			{
			\t"bucketId": 42
			}""";

	public static final String CREATE_CONNECTOR_REQUEST =
		"""
			{
			\t"tenantName": "demo",
			\t"preset": "CRAWLER"
			}""";

	public static final String CREATE_CONNECTOR_RESPONSE =
		"""
			{
			\t"result": "demo-CRAWLER-2026_1_0-SNAPSHOT"
			}""";

	public static final String CREATE_TENANT_MINIMAL_REQUEST =
		"""
			{
			\t"virtualHost": "demo.openk9.io",
			\t"securityConfiguration": "OAUTH2_ADMIN_ONLY"
			}""";

	public static final String CREATE_TENANT_EXTERNAL_IDP_REQUEST =
		"""
			{
			\t"virtualHost": "demo.openk9.io",
			\t"securityConfiguration": "OAUTH2_ADMIN_ONLY",
			\t"tenantName": "demo",
			\t"oAuth2Settings": {
			\t\t"issuerUri": "https://idp.example.com/realms/demo",
			\t\t"clientId": "openk9-admin",
			\t\t"clientSecret": "s3cr3t"
			\t}
			}""";

	public static final String CREATE_TENANT_SEARCH_ONLY_REQUEST =
		"""
			{
			\t"virtualHost": "search.example.io",
			\t"securityConfiguration": "NO_GATEWAY_AUTH",
			\t"ingressScopes": [
			\t\t"SEARCH"
			\t]
			}""";

	public static final String CREATE_TENANT_API_KEY_REQUEST =
		"""
			{
			\t"virtualHost": "demo.openk9.io",
			\t"securityConfiguration": "OAUTH2_SEARCH_WITH_API_KEY",
			\t"tenantName": "demo",
			\t"ingressScopes": [
			\t\t"SEARCH",
			\t\t"ADMINISTRATION",
			\t\t"RAG",
			\t\t"INGESTION"
			\t]
			}""";

	public static final String CREATE_TENANT_RESPONSE =
		"""
			{
			\t"id": "42",
			\t"tenantName": "demo",
			\t"virtualHost": "demo.openk9.io",
			\t"clientId": "demo-client",
			\t"clientSecret": "s3cr3t",
			\t"issuerUri": "https://keycloak.openk9.io/realms/demo",
			\t"securityConfiguration": "OAUTH2_ADMIN_ONLY",
			\t"realmProvisioned": true
			}""";

	public static final String CREATE_TABLES_RESPONSE =
		"""
			{
			\t"message": "Tables created for tenant 42"
			}""";

	public static final String REQUEST_DELETE_TENANT_REQUEST =
		"""
			{
			\t"virtualHost": "demo.openk9.io"
			}""";

	public static final String REQUEST_DELETE_TENANT_RESPONSE =
		"""
			{
			\t"message": "9f1e0b8c-4f2a-4f7d-9e5f-3c2a1b0d4e6a"
			}""";

	public static final String DELETE_TENANT_REQUEST =
		"""
			{
			\t"virtualHost": "demo.openk9.io",
			\t"token": "9f1e0b8c-4f2a-4f7d-9e5f-3c2a1b0d4e6a"
			}""";

	public static final String DELETE_TENANT_RESPONSE =
		"""
			{
			\t"message": "Tenant demo deleted"
			}""";

}
