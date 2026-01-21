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

import io.openk9.event.tenant.ReactiveTenantManagementEventConsumer;
import io.openk9.event.tenant.TenantManagementEvent;
import io.openk9.event.tenant.TenantManagementEventConsumer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TenantManagementEventDbWriter implements TenantManagementEventConsumer {

	private final ReactiveTenantManagementEventConsumer reactiveTenantManagementEventConsumer;

	@Override
	public void handleTenantCreatedEvent(TenantManagementEvent.TenantCreated event) {
		((Mono<Void>)reactiveTenantManagementEventConsumer.handleTenantCreatedEvent(event)).block();
	}

	@Override
	public void handleApiKeyCreatedEvent(TenantManagementEvent.ApiKeyCreated event) {
		((Mono<Void>)reactiveTenantManagementEventConsumer.handleApiKeyCreatedEvent(event)).block();
	}

	@Override
	public void handleTenantUpdatedEvent(TenantManagementEvent.TenantUpdated event) {
		((Mono<Void>)reactiveTenantManagementEventConsumer.handleTenantUpdatedEvent(event)).block();
	}

	@Override
	public void handleTenantDeletedEvent(TenantManagementEvent.TenantDeleted event) {
		((Mono<Void>)reactiveTenantManagementEventConsumer.handleTenantDeletedEvent(event)).block();
	}

}
