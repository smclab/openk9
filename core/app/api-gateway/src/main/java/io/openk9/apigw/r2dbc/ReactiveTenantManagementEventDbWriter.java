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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.openk9.event.tenant.Authorization;
import io.openk9.event.tenant.ReactiveTenantManagementEventConsumer;
import io.openk9.event.tenant.Route;
import io.openk9.event.tenant.TenantManagementEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Example event listener for handling tenant-related events in an event-driven architecture.
 * This demonstrates how to use the TenantDataInsertService when receiving messages
 * from a message broker (e.g., Kafka, RabbitMQ, etc.).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveTenantManagementEventDbWriter
	implements ReactiveTenantManagementEventConsumer {

	private final TenantWriteServiceR2dbc writeService;

	/**
	 * Handle tenant creation events.
	 * Example usage with Spring Cloud Stream or similar:
	 * {@code @StreamListener("tenant-events")}
	 *
	 */
	@Override
	public Publisher<Void> handleTenantCreatedEvent(TenantManagementEvent.TenantCreated event) {
		log.info("Received tenant created event: {}", event.tenantId());

		Map<Route, Authorization> routeAuthorizationMap = event.routeAuthorizationMap();

		List<Mono<Void>> inserts = new ArrayList<>();
		for (Map.Entry<Route, Authorization> entry : routeAuthorizationMap.entrySet()) {
			inserts.add(writeService.insertRouteSecurity(
				event.tenantId(), entry.getKey(), entry.getValue()));
		}

		return writeService
			.insertTenant(event.tenantId(),
				event.hostName(),
				event.issuerUri(),
				event.clientId(),
				event.clientSecret()
			)
			.then(Mono.when(inserts))
			.doOnSuccess(v -> log.info("Processed tenant created event: {}", event.tenantId()))
			.doOnError(throwable -> log.error("Failed tenant creation", throwable));
	}

	/**
	 * Handle API key creation events.
	 */
	@Override
	public Publisher<Void> handleApiKeyCreatedEvent(TenantManagementEvent.ApiKeyCreated event) {
		log.info("Received API key created event for tenant: {}", event.tenantId());

		return writeService
			.insertApiKey(event.tenantId(), event.apiKeyHash(), event.checksum())
			.doOnSuccess(v -> log.info("Processed API key created event for tenant: {}", event.tenantId()));
	}

	/**
	 * Handle tenant update events.
	 */
	@Override
	public Publisher<Void> handleTenantUpdatedEvent(TenantManagementEvent.TenantUpdated event) {
		log.info("Received tenant updated event: {}", event.tenantId());

		return writeService
			.updateTenant(
				event.tenantId(),
				event.hostName(),
				event.issuerUri(),
				event.clientId(),
				event.clientSecret()
			)
			.doOnSuccess(v -> log.info("Processed tenant updated event: {}", event.tenantId()));
	}

	/**
	 * Handle tenant deletion events.
	 */
	@Override
	public Publisher<Void> handleTenantDeletedEvent(TenantManagementEvent.TenantDeleted event) {
		log.info("Received tenant deleted event: {}", event.tenantId());

		return writeService.deleteTenant(event.tenantId())
			.doOnSuccess(v -> log.info("Processed tenant deleted event: {}", event.tenantId()));
	}

}
