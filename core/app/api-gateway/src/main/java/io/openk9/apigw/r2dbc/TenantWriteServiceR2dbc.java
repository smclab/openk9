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

import io.openk9.apigw.security.AuthorizationSchemeToken;
import io.openk9.apigw.security.RoutePath;
import io.openk9.common.util.CompactSnowflakeIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

/**
 * Service for inserting and modifying tenant-related data in the database.
 * This service can be used in event-driven architectures to handle incoming
 * data from message events.
 */
@Slf4j
@RequiredArgsConstructor
public class TenantWriteServiceR2dbc {

	record Tenant(long id, String tenantId, String hostName, String issuerUri, String clientId, String clientSecret) {}
	// TODO: record Issuer(long id, String tenantId, String issuerUri, String clientId, String clientSecret) {}
	record ApiKey(long id, String tenantId, String apiKeyHash, String checksum) {}
	record RouteSecurity(long id, String tenantId, String route, String authorizationScheme) {}

	private final static CompactSnowflakeIdGenerator idGenerator = new CompactSnowflakeIdGenerator();

	private final DatabaseClient db;
	private final R2dbcEntityTemplate template;

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

		log.debug("Inserting tenant: tenantId={}, hostName={}", tenantId, hostName);

		return template.insert(new Tenant(
				idGenerator.nextId(),
				tenantId,
				hostName,
				issuerUri,
				clientId,
				clientSecret
			))
			.then()
			.doOnSuccess(v -> log.info("Successfully inserted tenant: {}", tenantId))
			.doOnError(e -> log.error("Failed to insert tenant: {}", tenantId, e));
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

		log.debug("Inserting API key: tenantId={}, ", tenantId);

		return template.insert(new ApiKey(idGenerator.nextId(), tenantId, apiKeyHash, checksum))
			.then()
			.doOnSuccess(v -> log.info("Successfully inserted API key for tenant: {}", tenantId))
			.doOnError(e -> log.error("Failed to insert API key for tenant: {}", tenantId, e));
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
		String tenantId, String route, String authorizationScheme) {

		String uRoute = route.toUpperCase();
		String uAuthorizationScheme = authorizationScheme.toUpperCase();

		log.debug("Inserting route security: tenantId={}, route={}, scheme={}",
			tenantId, uRoute, uAuthorizationScheme);

		try {
			RoutePath.valueOf(uRoute);
			AuthorizationSchemeToken.valueOf(uAuthorizationScheme);
		}
		catch (IllegalArgumentException e) {
			log.warn("Invalid values: tenantId={}, route={}, scheme={}",
				tenantId, route, authorizationScheme);
			return Mono.error(new InvalidRouteSecurity(tenantId, route, authorizationScheme));
		}

		return template.insert(new RouteSecurity(
				idGenerator.nextId(),
				tenantId,
				uRoute,
				uAuthorizationScheme)
			)
			.then()
			.doOnSuccess(v -> log.info("Successfully inserted route security for tenant: {}", tenantId))
			.doOnError(e -> log.error("Failed to insert route security for tenant: {}", tenantId, e));
	}

	/**
	 * Update tenant information.
	 *
	 * @param tenantId the tenant identifier to update
	 * @param hostName the new hostname
	 * @param issuerUri the new issuer URI
	 * @return Mono that completes when update is done
	 */
	public Mono<Void> updateTenant(String tenantId, String hostName, String issuerUri) {
		log.debug("Updating tenant: tenantId={}", tenantId);

		return db.sql("""
                UPDATE tenant
                SET host_name = :hostName, issuer_uri = :issuerUri
                WHERE tenant_id = :tenantId
                """)
			.bind("tenantId", tenantId)
			.bind("hostName", hostName)
			.bind("issuerUri", issuerUri)
			.then()
			.doOnSuccess(v -> log.info("Successfully updated tenant: {}", tenantId))
			.doOnError(e -> log.error("Failed to update tenant: {}", tenantId, e));
	}

	/**
	 * Delete a tenant and all associated data (cascading delete should be handled by DB constraints).
	 *
	 * @param tenantId the tenant identifier to delete
	 * @return Mono that completes when deletion is done
	 */
	public Mono<Void> deleteTenant(String tenantId) {
		log.debug("Deleting tenant: tenantId={}", tenantId);

		return db.sql("""
                DELETE FROM tenant
                WHERE tenant_id = :tenantId
                """)
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

		return db.sql("""
                DELETE FROM api_key
                WHERE id = :id
                """)
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

		return db.sql("""
                DELETE FROM route_security
                WHERE id = :id
                """)
			.bind("id", id)
			.then()
			.doOnSuccess(v -> log.info("Successfully deleted route security: {}", id))
			.doOnError(e -> log.error("Failed to delete route security: {}", id, e));
	}
}
