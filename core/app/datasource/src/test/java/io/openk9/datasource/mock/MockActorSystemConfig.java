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

package io.openk9.datasource.mock;

import io.openk9.datasource.actor.ActorSystemBehaviorInitializer;
import io.openk9.datasource.actor.ActorSystemConfig;
import io.openk9.datasource.actor.ActorSystemInitializer;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import org.mockito.Mockito;

@Mock
@Dependent
public class MockActorSystemConfig extends ActorSystemConfig {


	@Override
	@Produces
	@ApplicationScoped
	public ActorSystemInitializer local() {
		return actorSystem -> {};
	}

	@Override
	@Produces
	@ApplicationScoped
	public ActorSystemInitializer cluster() {
		return actorSystem -> {};
	}

	@Override
	@Produces
	@ApplicationScoped
	public ActorSystemInitializer clusterSharding() {
		return actorSystem -> {};
	}

	@Override
	@Produces
	@ApplicationScoped
	public ActorSystemBehaviorInitializer cacheHandlerBehaviorInit() {
		return Mockito.mock(ActorSystemBehaviorInitializer.class);
	}

	@Override
	@Produces
	@ApplicationScoped
	public ActorSystemBehaviorInitializer createChannelManager() {
		return Mockito.mock(ActorSystemBehaviorInitializer.class);
	}

}
