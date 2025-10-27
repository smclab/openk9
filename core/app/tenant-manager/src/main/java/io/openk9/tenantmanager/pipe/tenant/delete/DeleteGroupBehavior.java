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

package io.openk9.tenantmanager.pipe.tenant.delete;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteGroupMessage;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteMessage;
import io.openk9.tenantmanager.pipe.tenant.delete.message.TimeoutMessage;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.openk9.tenantmanager.actor.TypedActor.Stay;

public class DeleteGroupBehavior implements TypedActor.Behavior<DeleteGroupMessage> {

	private final EventBus eventBus;

	public DeleteGroupBehavior(
		TypedActor.Address<DeleteGroupMessage> self,
		TypedActor.System system,
		EventBus eventBus) {

		this.self = self;
		this.system = system;
		this.eventBus = eventBus;
	}

	private final Map<String, TypedActor.Address<DeleteMessage>> deleteActorMap =
		new LinkedHashMap<>();

	private final TypedActor.Address<DeleteGroupMessage> self;
	private final TypedActor.System system;

	@Override
	public TypedActor.Effect<DeleteGroupMessage> apply(DeleteGroupMessage timeoutMessage) {

		if (timeoutMessage instanceof DeleteGroupMessage.addDeleteRequest) {

			DeleteGroupMessage.addDeleteRequest newDeleteRequest =
				(DeleteGroupMessage.addDeleteRequest) timeoutMessage;

			String virtualHost = newDeleteRequest.virtualHost();

			deleteActorMap.compute(virtualHost, (k, v) -> {

				if (v != null) {
					v.tell(new DeleteMessage.Stop());
				}

				TypedActor.Address<TimeoutMessage> timeoutDeleteActor =
					system.actorOf(TimeoutStopDeleteBehavior::new);

				TypedActor.Address<DeleteMessage> deleteActor =
					system.actorOf(self -> new DeleteBehavior(eventBus, self));

				deleteActor.tell(new DeleteMessage.Start(self, virtualHost));

				timeoutDeleteActor.tell(
					new TimeoutMessage.Start(
						deleteActor, Duration.ofSeconds(90)));

				return deleteActor;

			});

		}
		else if (timeoutMessage instanceof DeleteGroupMessage.TellDelete) {
			DeleteGroupMessage.TellDelete tellDelete =
				(DeleteGroupMessage.TellDelete) timeoutMessage;
			if (deleteActorMap.containsKey(tellDelete.virtualHost())) {
				deleteActorMap
					.get(tellDelete.virtualHost())
					.tell(new DeleteMessage.Delete(tellDelete.token(), tellDelete.appManager()));
			}
			else {
				LOGGER.warn("virtualHost not found: " + tellDelete.virtualHost());
			}
		}
		else if (timeoutMessage instanceof DeleteGroupMessage.RemoveDeleteRequest) {

			DeleteGroupMessage.RemoveDeleteRequest removeDeleteRequest =
				(DeleteGroupMessage.RemoveDeleteRequest) timeoutMessage;

			deleteActorMap.remove(removeDeleteRequest.virtualHost());

		}

		return Stay();

	}

	private static final Logger LOGGER = Logger.getLogger(DeleteGroupBehavior.class);
	
}
