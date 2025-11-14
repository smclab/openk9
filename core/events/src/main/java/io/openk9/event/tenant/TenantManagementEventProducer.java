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
import org.reactivestreams.Subscriber;

/**
 * A producer of {@link TenantManagementEvent}.
 */
public interface TenantManagementEventProducer {

	/**
	 * Send the {@link TenantManagementEvent}.
	 *
	 * @param event the {@link TenantManagementEvent} to send.
	 */
	void send(TenantManagementEvent event) throws Throwable;

	/**
	 * Send the {@link TenantManagementEvent} asynchronous.
	 *
	 * @param event that will be sent
	 * @return a {@link Publisher} that will call
	 * {@link Subscriber#onComplete()} when the event is sent.
	 */
	Publisher<Void> sendAsync(TenantManagementEvent event);

}
