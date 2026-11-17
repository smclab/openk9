package ${package};

import io.openk9.connector.api.beans.IngestionDTO;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageProducer;

/**
 * Represents an asynchronous worker that performs a task and sends
 * processed data using a {@link MessageProducer}.
 * Implementations of this interface must define the {@code work} method, which
 * performs a non-blocking operation. The result of the operation is communicated
 * using the provided {@link Promise}, and output data can be published using the
 * {@link MessageProducer}.
 * When OpenK9 no longer accepts the data of the schedule, for example because it
 * was cancelled, the next write throws {@link java.util.concurrent.CancellationException}:
 * let it propagate, so that the work stops.
 * In an asynchronous worker the exception is thrown inside your callback: pass it to
 * {@code promise.fail(e)}, otherwise the execution never ends.
 *
 * @see io.vertx.core.Promise
 * @see io.vertx.core.eventbus.MessageProducer
 * @see io.openk9.connector.api.beans.IngestionDTO
 */
public interface Worker {

    void work(Promise<?> promise, MessageProducer<IngestionDTO> producer);
}
