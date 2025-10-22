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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.openk9.apigw.security.AuthorizationSchemeToken;
import io.openk9.apigw.security.ChecksumValidationException;
import io.openk9.apigw.security.Keychain;
import io.openk9.apigw.security.NoAuthenticationToken;
import io.openk9.apigw.security.RouteAuthorizationMap;
import io.openk9.apigw.security.RoutePath;
import io.openk9.apigw.security.Tenant;
import io.openk9.apigw.security.TenantIdResolverFilter;
import io.openk9.apigw.security.TenantSecurityService;
import io.openk9.apigw.security.apikey.ApiKeyAuthenticationToken;
import io.openk9.apigw.security.apikey.ApiKeyMalformedException;
import io.openk9.apigw.security.oauth2.OAuth2Settings;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.Authentication;
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
                    SELECT api_key_hash, checksum
                    FROM api_key
                    WHERE tenant_id = :tenantId
                    """)
			.bind("tenantId", tenantId)
			.map((row, meta) -> {
				var apiKeyHash = row.get("api_key_hash", String.class);
				var lastDigits = row.get("checksum", String.class);
				Objects.requireNonNull(apiKeyHash);
				Objects.requireNonNull(lastDigits);
				return Keychain.Key.of(apiKeyHash, lastDigits);
			})
			.all()
			.collectList()
			.map(Keychain::new);
	}

	private Mono<RouteAuthorizationMap> getRouteAuthorizationMap(String tenantId) {
		return db.sql("""
                    SELECT route, authorization_scheme
                    FROM route_security
                    WHERE tenant_id = :tenantId
                    """)
			.bind("tenantId", tenantId)
			.map((row, meta) -> Map.entry(
				RoutePath.valueOf(row.get("route", String.class)),
				AuthorizationSchemeToken.valueOf(row.get("authorization_scheme", String.class))
			))
			.all()
			.collectMap(Map.Entry::getKey, Map.Entry::getValue, () -> new EnumMap<>(RoutePath.class))
			.map(RouteAuthorizationMap::of);
	}

	@Override
	public Mono<Boolean> isAuthorized(Mono<Authentication> authenticationMono, ServerWebExchange exchange) {
		String tenantId = TenantIdResolverFilter.getTenantId(exchange);

		if (tenantId == null) {
			return Mono.just(false);
		}

		var request = exchange.getRequest();
		var path = request.getPath();

		RoutePath routePath = RoutePath.matchOf(path.value());

		return getTenantAggregate(tenantId)
			.map(Tenant::routeAuthorizationMap)
			.flatMap(routeAuthorizationMap -> authenticationMono
				.defaultIfEmpty(NoAuthenticationToken.INSTANCE)
				.map(auth -> routeAuthorizationMap.allows(routePath, auth)))
			.defaultIfEmpty(false); // disallow if tenant doesn't exist
	}

	@Override
	public Mono<OAuth2Settings> getOAuth2Settings(ServerWebExchange exchange) {
		String tenantId = TenantIdResolverFilter.getTenantId(exchange);

		if (tenantId == null) {
			return Mono.empty();
		}

		return getTenantAggregate(tenantId)
			.map(Tenant::oauth2Settings)
			.filter(Objects::nonNull);
	}

	@Override
	public Mono<List<String>> getApiKeyPermission(
		ApiKeyAuthenticationToken apiKeyToken) {

		String tenantId = apiKeyToken.getTenantId();
		String apiKey = apiKeyToken.getApiKey();

		return getTenantAggregate(tenantId)
			.<List<String>>handle((tenant, sink) -> {
				var keychain = tenant.keychain();
				try {
					if (keychain != null && keychain.contains(apiKey)) {
						sink.next(List.of("ADMIN"));
					}
					else {
						sink.complete();
					}
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
