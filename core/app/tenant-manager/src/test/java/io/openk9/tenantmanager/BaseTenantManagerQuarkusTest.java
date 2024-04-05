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

package io.openk9.tenantmanager;

import io.openk9.tenantmanager.init.TenantManagerInitializer;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;

import javax.inject.Inject;

public abstract class BaseTenantManagerQuarkusTest {

	@Inject
	EventBus eventBus;

	protected <T> Uni<T> whenTenantManagerInitialized(Uni<T> uni) {
		return Uni.createFrom()
			.emitter(emitter -> {
				var consumer = eventBus.consumer(TenantManagerInitializer.INITIALIZED);
				consumer.handler(ignore -> {
					emitter.complete(ignore);
					consumer.unregisterAndForget();
				});
			})
			.chain(unused -> uni);
	}

}
