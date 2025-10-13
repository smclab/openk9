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

package io.openk9.experimental.spring_apigw_sample.r2dbc;

import io.openk9.experimental.spring_apigw_sample.event.ReactiveTenantManagementEventConsumer;
import io.openk9.experimental.spring_apigw_sample.event.TenantManagementEvent;
import io.openk9.experimental.spring_apigw_sample.event.TenantManagementEventConsumer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantManagementEventDbWriter implements TenantManagementEventConsumer {

	private final ReactiveTenantManagementEventConsumer reactiveTenantManagementEventConsumer;

	@Override
	public void handleTenantCreatedEvent(TenantManagementEvent.TenantCreated event) {
		reactiveTenantManagementEventConsumer.handleTenantCreatedEvent(event).block();
	}

	@Override
	public void handleApiKeyCreatedEvent(TenantManagementEvent.ApiKeyCreated event) {
		reactiveTenantManagementEventConsumer.handleApiKeyCreatedEvent(event).block();
	}

	@Override
	public void handleTenantUpdatedEvent(TenantManagementEvent.TenantUpdated event) {
		reactiveTenantManagementEventConsumer.handleTenantUpdatedEvent(event).block();
	}

	@Override
	public void handleTenantDeletedEvent(TenantManagementEvent.TenantDeleted event) {
		reactiveTenantManagementEventConsumer.handleTenantDeletedEvent(event).block();
	}

}
