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

import io.openk9.event.tenant.ReactiveTenantEventConsumer;
import io.openk9.event.tenant.TenantEvent;
import io.openk9.event.tenant.TenantEventConsumer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TenantEventDbWriter implements TenantEventConsumer {

	private final ReactiveTenantEventConsumer reactiveTenantEventConsumer;

	@Override
	public void handleTenantCreatedEvent(TenantEvent.TenantCreated event) {
		((Mono<Void>) reactiveTenantEventConsumer.handleTenantCreatedEvent(event)).block();
	}

	@Override
	public void handleApiKeyCreatedEvent(TenantEvent.ApiKeyCreated event) {
		((Mono<Void>) reactiveTenantEventConsumer.handleApiKeyCreatedEvent(event)).block();
	}

	@Override
	public void handleTenantUpdatedEvent(TenantEvent.TenantUpdated event) {
		((Mono<Void>) reactiveTenantEventConsumer.handleTenantUpdatedEvent(event)).block();
	}

	@Override
	public void handleTenantDeletedEvent(TenantEvent.TenantDeleted event) {
		((Mono<Void>) reactiveTenantEventConsumer.handleTenantDeletedEvent(event)).block();
	}

}
