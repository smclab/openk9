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
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteMessage;
import io.openk9.tenantmanager.pipe.tenant.delete.message.TimeoutMessage;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;

import static io.openk9.tenantmanager.actor.TypedActor.Die;
import static io.openk9.tenantmanager.actor.TypedActor.Stay;

public class TimeoutStopDeleteBehavior implements TypedActor.Behavior<TimeoutMessage> {

	public TimeoutStopDeleteBehavior(TypedActor.Address<TimeoutMessage> self) {
		this.self = self;
	}

	@Override
	public TypedActor.Effect<TimeoutMessage> apply(TimeoutMessage timeoutMessage) {
		
		if (timeoutMessage instanceof TimeoutMessage.Start) {
			TimeoutMessage.Start start = (TimeoutMessage.Start) timeoutMessage;
			this.deleteActor = start.deleteActor();
			Duration duration = start.duration();
			delay = Instant.now().plus(duration);
			LOGGER.info("delete token expire in: " + duration);
			this.self.tell(new TimeoutMessage.Wait());
		}
		else if (timeoutMessage instanceof TimeoutMessage.Wait) {
			if (Instant.now().isAfter(delay)) {
				this.deleteActor.tell(new DeleteMessage.Stop());
				return Die();
			}
			else {
				this.self.tell(new TimeoutMessage.Wait());
			}
		}
		else if (timeoutMessage instanceof TimeoutMessage.Stop) {
			deleteActor.tell(new DeleteMessage.Stop());
			return Die();
		}

		return Stay();
		
	}

	private final TypedActor.Address<TimeoutMessage> self;
	private TypedActor.Address<DeleteMessage> deleteActor;
	private Instant delay;
	private static final Logger LOGGER = Logger.getLogger(
		TimeoutStopDeleteBehavior.class);
	
}
