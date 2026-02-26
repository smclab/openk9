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

package io.openk9.event.tenant;

import org.reactivestreams.Publisher;

/**
 * Defines an event listener interface for handling tenant-related domain events
 * within an event-driven architecture.
 * <p>
 * Implementations of this interface consume messages published by other
 * services or components to react to lifecycle changes in tenant management,
 * API key configuration, and route-level security settings.
 * <p>
 * Each event type represents a specific change in the system state, such as
 * tenant creation, update, or deletion. The methods return a {@link Publisher}
 * to support non-blocking, reactive event handling.
 */
public interface ReactiveTenantManagementEventConsumer {

	/**
	 * Handles an event indicating that a new tenant has been created.
	 *
	 * @param event the {@link TenantManagementEvent.TenantCreated} containing tenant details
	 * @return a {@link Publisher} that completes when the event has been processed
	 */
	Publisher<Void> handleTenantCreatedEvent(TenantManagementEvent.TenantCreated event);

	/**
	 * Handles an event indicating that a new API key has been created for a tenant route.
	 *
	 * @param event the {@link TenantManagementEvent.ApiKeyCreated} containing API key information
	 * @return a {@link Publisher} that completes when the event has been processed
	 */
	Publisher<Void> handleApiKeyCreatedEvent(TenantManagementEvent.ApiKeyCreated event);

	/**
	 * Handles an event indicating that an existing tenant has been updated.
	 *
	 * @param event the {@link TenantManagementEvent.TenantUpdated} containing updated tenant details
	 * @return a {@link Publisher} that completes when the event has been processed
	 */
	Publisher<Void> handleTenantUpdatedEvent(TenantManagementEvent.TenantUpdated event);

	/**
	 * Handles an event indicating that a tenant has been deleted.
	 *
	 * @param event the {@link TenantManagementEvent.TenantDeleted} identifying the deleted tenant
	 * @return a {@link Publisher} that completes when the event has been processed
	 */
	Publisher<Void> handleTenantDeletedEvent(TenantManagementEvent.TenantDeleted event);

}
