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

package io.openk9.tenantmanager.service;

import io.openk9.event.tenant.TenantEvent;
import io.openk9.event.tenant.TenantEventProducer;

import io.smallrye.mutiny.Uni;
import mutiny.zero.flow.adapters.AdaptersToFlow;
import org.jboss.logging.Logger;

public class TenantEventProducerUtils {

	public static Uni<Void> sendEvent(
		TenantEventProducer producer, int rowCount, TenantEvent event) {

		if (rowCount == 0) {
			if (log.isDebugEnabled()) {
				log.debugf(
					"No rows was updated. The event %s wont be sent.",
					event);
			}

			return Uni.createFrom().voidItem();
		}

		return Uni.createFrom().publisher(
			AdaptersToFlow.publisher(producer.sendAsync(event))
		);

	}

	private static final Logger log = Logger.getLogger(TenantEventProducerUtils.class);

}
