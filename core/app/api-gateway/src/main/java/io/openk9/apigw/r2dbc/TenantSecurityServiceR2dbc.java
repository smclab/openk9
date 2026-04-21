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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.openk9.apigw.security.ApiRoute;
import io.openk9.apigw.security.RouteAuthorizationResolverFilter;
import io.openk9.apigw.security.AuthorizationSchemeToken;
import io.openk9.apigw.security.ChecksumValidationException;
import io.openk9.apigw.security.ExpiredApiKeyException;
import io.openk9.apigw.security.Keychain;
import io.openk9.apigw.security.RouteAuthorizationMap;
import io.openk9.apigw.security.Tenant;
import io.openk9.apigw.security.TenantIdResolverFilter;
import io.openk9.apigw.security.TenantSecurityService;
import io.openk9.apigw.security.apikey.ApiKeyAuthenticationToken;
import io.openk9.apigw.security.apikey.ApiKeyMalformedException;
import io.openk9.apigw.security.oauth2.OAuth2Settings;
import io.openk9.event.tenant.ApiGroup;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * An implementation of the {@link TenantSecurityService} that fetches
 * the {@link Tenant} from the database, using a reactive client.
 *
 * @implNote the implemented methods fetch all the configuration related
 * to a {@code  tenantId}, to construct an aggregated {@link Tenant} that would be saved
 * in a Spring {@link Cache}.
 * <p>
 * The aggregated cached object is used when the methods of this service are invoked.
 */
@RequiredArgsConstructor
public class TenantSecurityServiceR2dbc implements TenantSecurityService {

	private final DatabaseClient db;
	private final Cache cache;

	public Mono<Tenant> getTenantAggregate(String tenantId) {
		var cached = cache.get(tenantId, Tenant.class);
		if (cached != null) {
			return Mono.just(cached);
		}

		Mono<Tenant> tenantMono = getTenant(tenantId);
		Mono<Keychain> keychainMono = getKeychain(tenantId);
		Mono<RouteAuthorizationMap> routeAuthMono = getRouteAuthorizationMap(tenantId);

		return Mono.zip(tenantMono, keychainMono, routeAuthMono)
			.map(tuple -> new Tenant(
				tuple.getT1().tenantId(),
				tuple.getT1().hostName(),
				tuple.getT1().oauth2Settings(),
				tuple.getT2(),
				tuple.getT3()
			))
			.doOnNext(tenant -> cache.put(tenantId, tenant));

	}

	private Mono<Tenant> getTenant(String tenantId) {
		return db.sql("""
                    SELECT tenant_id, host_name, issuer_uri, client_id, client_secret
                    FROM tenant
                    WHERE tenant_id = :tenantId
                    """)
			.bind("tenantId", tenantId)
			.map((row, meta) -> new Tenant(
				row.get("tenant_id", String.class),
				row.get("host_name", String.class),
				OAuth2Settings.fromStrings(
					row.get("issuer_uri", String.class),
					row.get("client_id", String.class),
					row.get("client_secret", String.class)
				),
				Keychain.of(),
				RouteAuthorizationMap.of()
			))
			.one();
	}

	private Mono<Keychain> getKeychain(String tenantId) {
		return db.sql("""
                    SELECT api_key_hash, checksum, api_group, expiration_date
                    FROM api_key
                    WHERE tenant_id = :tenantId
                    """)
			.bind("tenantId", tenantId)
			.map((row, meta) -> {
				String apiKeyHash = row.get("api_key_hash", String.class);
				String checksum = row.get("checksum", String.class);
				String apiGroupStr = row.get("api_group", String.class);
				OffsetDateTime expirationDate = row.get(
					"expiration_date", OffsetDateTime.class);

				Objects.requireNonNull(apiKeyHash);
				Objects.requireNonNull(checksum);
				Objects.requireNonNull(apiGroupStr);

				ApiGroup apiGroup = ApiGroup
					.valueOf(apiGroupStr);

				Instant expInstant = expirationDate != null
					? expirationDate.toInstant()
					: null;

				return Keychain.Key.of(
					apiKeyHash, checksum, apiGroup, expInstant);
			})
			.all()
			.collectList()
			.map(Keychain::of);
	}

	private Mono<RouteAuthorizationMap> getRouteAuthorizationMap(String tenantId) {
		return db.sql("""
                    SELECT route, authorization_scheme
                    FROM route_security
                    WHERE tenant_id = :tenantId
                    """)
			.bind("tenantId", tenantId)
			.map((row, meta) -> Map.entry(
				ApiRoute.valueOf(row.get("route", String.class)),
				AuthorizationSchemeToken.valueOf(row.get("authorization_scheme", String.class))
			))
			.all()
			.collectMap(Map.Entry::getKey, Map.Entry::getValue, () -> new EnumMap<>(ApiRoute.class))
			.map(RouteAuthorizationMap::of);
	}

	@Override
	public Mono<Boolean> isAuthorized(
		Mono<Authentication> authenticationMono, 
		ServerWebExchange exchange) {

		ApiRoute apiRoute = RouteAuthorizationResolverFilter
			.getApiRoute(exchange);
		AuthorizationSchemeToken scheme = RouteAuthorizationResolverFilter
			.getAuthorizationScheme(exchange);

		// The resolver filter did not stamp the exchange. Deny.
		if (apiRoute == null || scheme == null) {
			return Mono.just(false);
		}

		return authenticationMono
			.map(auth -> allows(scheme, apiRoute, auth));
	}

	private static boolean allows(
		AuthorizationSchemeToken authSchemeToken,
		ApiRoute apiRoute,
		Authentication authentication) {

		// NO_AUTH and null scheme always allow.
		if (authSchemeToken == null
			|| authSchemeToken == AuthorizationSchemeToken.NO_AUTH) {

			return true;
		}

		// Authentication class must match 
		// AuthorizationSchemeToken referenced class.
		if (!authSchemeToken.match(authentication.getClass())) {
			return false;
		}

		// API Keys must match the current ApiRoute.
		if (authentication instanceof ApiKeyAuthenticationToken) {
			String requiredAuthority = "ROUTE_" + apiRoute.name();

			return authentication.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(requiredAuthority::equals);
		}

		return true;
	}

	@Override
	public Mono<OAuth2Settings> getOAuth2Settings(ServerWebExchange exchange) {
		String tenantId = TenantIdResolverFilter.getTenantId(exchange);

		if (tenantId == null) {
			return Mono.empty();
		}

		return getTenantAggregate(tenantId)
			.mapNotNull(Tenant::oauth2Settings)
			.filter(Objects::nonNull);
	}

	@Override
	public Mono<List<String>> getApiKeyPermission(
		ApiKeyAuthenticationToken apiKeyToken) {

		String tenantId = apiKeyToken.getTenantId();
		String apiKey = apiKeyToken.getApiKey();

		return getTenantAggregate(tenantId)
			.<List<String>>handle((tenant, sink) -> {
				Keychain keychain = tenant.keychain();
				// no keychain configured for this tenant
				if (keychain == null) {
					sink.complete();
					return;
				}
				try {
					Optional<Keychain.Key> found = keychain.find(apiKey);

					// no apiKey found with the same hash 
					if (found.isEmpty()) {
						sink.complete();
						return;
					}

					Keychain.Key key = found.get();

					if (key.isExpired()) {
						sink.error(new ExpiredApiKeyException(
							"API key has expired"));
						return;
					}

					sink.next(mapApiGroupToAuthorities(
						key.getApiGroup()));
				}
				catch (ChecksumValidationException e) {
					sink.error(e);
				}
			})
			.onErrorMap(
				ChecksumValidationException.class,
				ApiKeyMalformedException::new
			);
	}

	/**
	 * Maps an API group name to the list of route authority strings
	 * that the key is allowed to access.
	 *
	 * @param apiGroup the API group name
	 * @return list of authority strings in the form {@code "ROUTE_<ApiRoute>"}
	 */
	static List<String> mapApiGroupToAuthorities(ApiGroup apiGroup) {

		return ApiRoute.routesFor(apiGroup)
			.stream()
			.map(route -> "ROUTE_" + route.name())
			.toList();
	}

	@Override
	public Mono<String> getTenantId(String hostName) {
		var cached = cache.get(hostName, String.class);
		if (cached != null) {
			return Mono.just(cached);
		}

		return db.sql("""
                    SELECT tenant_id
                    FROM tenant
                    WHERE host_name = :hostName
                    """)
			.bind("hostName", hostName)
			.map((row, meta) -> row.get("tenant_id", String.class))
			.one()
			.doOnNext(tenantId -> cache.put(hostName, tenantId));
	}

}
