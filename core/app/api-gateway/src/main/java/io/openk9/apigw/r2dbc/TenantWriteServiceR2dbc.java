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

package io.openk9.apigw.r2dbc;

import java.util.Objects;

import io.openk9.apigw.security.AuthorizationSchemeToken;
import io.openk9.apigw.security.RoutePath;
import io.openk9.common.util.CompactSnowflakeIdGenerator;
import io.openk9.event.tenant.Authorization;
import io.openk9.event.tenant.Route;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

/**
 * Service for inserting and modifying tenant-related data in the database.
 * This service can be used in event-driven architectures to handle incoming
 * data from message events.
 */
@Slf4j
public class TenantWriteServiceR2dbc {

	private static final String DELETE_TENANT_SQL = """
		DELETE FROM tenant
		WHERE tenant_id = :tenantId
		""";
	private static final String DELETE_API_KEY_SQL = """
		DELETE FROM api_key
		WHERE id = :id
		""";
	private static final String DELETE_ROUTE_SECURITY_SQL = """
		DELETE FROM route_security
		WHERE id = :id
		""";
	private static final String INSERT_TENANT_SQL = """
		INSERT INTO tenant (id, tenant_id, host_name, issuer_uri, client_id, client_secret)
		VALUES (:id, :tenantId, :hostName, :issuerUri, :clientId, :clientSecret)
		""";
	private static final String INSERT_ROUTE_SECURITY = """
		INSERT INTO route_security (id, tenant_id, route, authorization_scheme)
		VALUES (:id, :tenantId, :route, :authorizationScheme)
		""";
	private static final String INSERT_API_KEY_SQL = """
		INSERT INTO api_key (id, tenant_id, api_key_hash, checksum)
		VALUES (:id, :tenantId, :apiKeyHash, :checksum)
		""";
	private static final String UPDATE_TENANT_SQL = """
		UPDATE tenant
		SET host_name = :hostName,
		 issuer_uri = :issuerUri,
		 client_id = :clientId,
		 client_secret = :clientSecret
		WHERE tenant_id = :tenantId
		""";
	private static final CompactSnowflakeIdGenerator idGenerator =
		new CompactSnowflakeIdGenerator();

	private final DatabaseClient dbClient;

	/**
	 * Create a new {@link TenantWriteServiceR2dbc} given {@link DatabaseClient}.
	 *
	 * @param dbClient must not be {@literal null}.
	 */
	public TenantWriteServiceR2dbc(DatabaseClient dbClient) {
		this.dbClient = dbClient;
	}

	/**
	 * Insert a new tenant into the database.
	 *
	 * @param tenantId the tenant identifier
	 * @param hostName the hostname for the tenant
	 * @param issuerUri the issuer URI (can be null)
	 * @param clientId the clientId registered on the issuer (can be null)
	 * @param clientSecret the secret related to the clientId (can be null)
	 * @return Mono that completes when insertion is done
	 */
	public Mono<Void> insertTenant(
		String tenantId, String hostName, String issuerUri, String clientId, String clientSecret) {

		Objects.requireNonNull(tenantId);
		Objects.requireNonNull(hostName);

		log.debug("Inserting tenant: tenantId={}, hostName={}", tenantId, hostName);

		long id = idGenerator.nextId();

		DatabaseClient.GenericExecuteSpec stmt = dbClient.sql(INSERT_TENANT_SQL)
			.bind("id", id)
			.bind("tenantId", tenantId)
			.bind("hostName", hostName)
			.bindNull("issuerUri", String.class)
			.bindNull("clientId", String.class)
			.bindNull("clientSecret", String.class);

		if (issuerUri != null) {
			stmt = stmt.bind("issuerUri", issuerUri);
		}

		if (clientId != null) {
			stmt = stmt.bind("clientId", clientId);
		}

		if (clientSecret != null) {
			stmt = stmt.bind("clientSecret", clientSecret);
		}

		return stmt.then()
			.doOnSuccess(v -> log.info(
				"Successfully inserted tenant { id: {}, tenantId: {}, hostName: {} }",
				id, tenantId, hostName)
			)
			.doOnError(DuplicateKeyException.class, e -> log.warn(
				"Insert discarded because a Tenant with tenantId: {} is already created", tenantId))
			.onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
			.doOnError(e -> log.error(
				"Failed to insert tenant { id: {}, tenantId: {}, hostName: {} }",
				id, tenantId, hostName, e));
	}

	/**
	 * Insert a new API key into the database.
	 *
	 * @param tenantId the tenant identifier
	 * @param apiKeyHash the hashed API key
	 * @param checksum the checksum value
	 * @return Mono that completes when insertion is done
	 */
	public Mono<Void> insertApiKey(
		String tenantId, String apiKeyHash, String checksum) {

		Objects.requireNonNull(tenantId);
		Objects.requireNonNull(apiKeyHash);
		Objects.requireNonNull(checksum);

		log.debug("Inserting an API key for tenantId: {}, ", tenantId);

		long id = idGenerator.nextId();

		DatabaseClient.GenericExecuteSpec stmt = dbClient.sql(INSERT_API_KEY_SQL)
			.bind("id", id)
			.bind("tenantId", tenantId)
			.bind("apiKeyHash", apiKeyHash)
			.bind("checksum", checksum);

		return stmt.then()
			.doOnSuccess(v -> log.info(
				"Successfully inserted API key for tenantId: {}", tenantId))
			.doOnError(DuplicateKeyException.class, e -> log.warn(
				"Insert discarded because an ApiKey with tenantId: {} and apiKeyHash: {} is already created",
				tenantId, apiKeyHash))
			.onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
			.doOnError(e -> log.error(
				"Failed to insert API key for tenantId: {}", tenantId, e));
	}

	/**
	 * Insert a new route security configuration into the database.
	 *
	 * @param tenantId the tenant identifier
	 * @param route the route pattern
	 * @param authorizationScheme the authorization scheme (OAUTH2, API_KEY, NO_AUTH)
	 * @return Mono that completes when insertion is done
	 */
	public Mono<Void> insertRouteSecurity(
		String tenantId, Route route, Authorization authorizationScheme) {

		Objects.requireNonNull(tenantId);
		String routePath = mapRoute(route);
		String authScheme = mapAuthorization(authorizationScheme);

		log.debug(
			"Inserting route security {route: {}, scheme: {}} for tenantId: {}",
			routePath, authScheme, tenantId);

		long id = idGenerator.nextId();

		DatabaseClient.GenericExecuteSpec stmt = dbClient.sql(INSERT_ROUTE_SECURITY)
			.bind("id", id)
			.bind("tenantId", tenantId)
			.bind("route", routePath)
			.bind("authorizationScheme", authScheme);

		return stmt.then()
			.doOnSuccess(v -> log.info(
				"Successfully inserted route security for tenantId: {}", tenantId))
			.doOnError(DuplicateKeyException.class, e -> log.warn(
				"Insert discarded because RouteSecurity with tenantId: {} and route: {} is already created",
				tenantId, route))
			.onErrorResume(DuplicateKeyException.class, e -> Mono.empty())
			.doOnError(e -> log.error(
				"Failed to insert route security for tenantId: {}", tenantId, e));
	}

	/**
	 * Update tenant information.
	 *
	 * @param tenantId the tenant identifier to update
	 * @param hostName the new hostname
	 * @param issuerUri the new issuer URI
	 * @return Mono that completes when update is done
	 */
	public Mono<Void> updateTenant(
		String tenantId, String hostName, String issuerUri, String clientId, String clientSecret) {

		log.debug("Updating tenant: tenantId={}", tenantId);

		 DatabaseClient.GenericExecuteSpec stmt = dbClient.sql(UPDATE_TENANT_SQL)
			 .bind("tenantId", tenantId)
			 .bind("hostName", hostName)
			 .bindNull("issuerUri", String.class)
			 .bindNull("clientId", String.class)
			 .bindNull("clientSecret", String.class);

		if (issuerUri != null) {
			stmt = stmt.bind("issuerUri", issuerUri);
		}

		if (clientId != null) {
			stmt = stmt.bind("clientId", clientId);
		}

		if (clientSecret != null) {
			stmt = stmt.bind("clientSecret", clientSecret);
		}

		return stmt.then()
			.doOnSuccess(v -> log.info("Successfully updated tenant: {}", tenantId))
			.doOnError(e -> log.error("Failed to update tenant: {}", tenantId, e));
	}

	/**
	 * Delete a tenant and all associated data
	 * (cascading delete should be handled by DB constraints).
	 *
	 * @param tenantId the tenant identifier to delete
	 * @return Mono that completes when deletion is done
	 */
	public Mono<Void> deleteTenant(String tenantId) {
		log.debug("Deleting tenant: tenantId={}", tenantId);

		return dbClient.sql(DELETE_TENANT_SQL)
			.bind("tenantId", tenantId)
			.then()
			.doOnSuccess(v -> log.info("Successfully deleted tenant: {}", tenantId))
			.doOnError(e -> log.error("Failed to delete tenant: {}", tenantId, e));
	}

	/**
	 * Delete an API key by its ID.
	 *
	 * @param id the API key ID to delete
	 * @return Mono that completes when deletion is done
	 */
	public Mono<Void> deleteApiKey(Long id) {
		log.debug("Deleting API key: id={}", id);

		return dbClient.sql(DELETE_API_KEY_SQL)
			.bind("id", id)
			.then()
			.doOnSuccess(v -> log.info("Successfully deleted API key: {}", id))
			.doOnError(e -> log.error("Failed to delete API key: {}", id, e));
	}

	/**
	 * Delete a route security configuration by its ID.
	 *
	 * @param id the route security ID to delete
	 * @return Mono that completes when deletion is done
	 */
	public Mono<Void> deleteRouteSecurity(Long id) {
		log.debug("Deleting route security: id={}", id);

		return dbClient.sql(DELETE_ROUTE_SECURITY_SQL)
			.bind("id", id)
			.then()
			.doOnSuccess(v -> log.info("Successfully deleted route security: {}", id))
			.doOnError(e -> log.error("Failed to delete route security: {}", id, e));
	}

	private static String mapRoute(Route route) {
		return (switch (route) {
			case DATASOURCE -> RoutePath.DATASOURCE;
			case SEARCHER -> RoutePath.SEARCHER;
			case RAG -> RoutePath.RAG;
			case ANY -> RoutePath.ANY;
		}).name();
	}

	private static String mapAuthorization(Authorization authorization) {
		return (switch (authorization) {
			case OAUTH2 -> AuthorizationSchemeToken.OAUTH2;
			case API_KEY -> AuthorizationSchemeToken.API_KEY;
			case NO_AUTH -> AuthorizationSchemeToken.NO_AUTH;
		}).name();
	}

}
