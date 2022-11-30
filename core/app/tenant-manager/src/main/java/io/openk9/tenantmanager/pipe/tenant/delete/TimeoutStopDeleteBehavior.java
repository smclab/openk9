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
