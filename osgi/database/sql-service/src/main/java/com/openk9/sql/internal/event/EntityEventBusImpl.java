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

package com.openk9.sql.internal.event;

import com.openk9.sql.api.event.EntityEvent;
import com.openk9.sql.api.event.EntityEventBus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component(
	immediate = true,
	service = EntityEventBus.class
)
public class EntityEventBusImpl implements EntityEventBus {

	@Activate
	public void activate() {
		_many = Sinks.many().multicast().onBackpressureBuffer(1);
	}

	@Deactivate
	public void deactivate() {
		_many.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
		_many = null;
	}

	@Override
	public <T> void sendEvent(EntityEvent<T> entityEvent) {
		_many.emitNext(entityEvent, Sinks.EmitFailureHandler.FAIL_FAST);
	}

	@Override
	public Flux<EntityEvent<?>> stream() {
		return _many.asFlux();
	}

	private Sinks.Many<EntityEvent<?>> _many;

}
