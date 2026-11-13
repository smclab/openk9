package ${package};

import io.openk9.connector.api.beans.IngestionDTO;
import io.vertx.core.eventbus.MessageProducer;

/**
 * Represents a worker that performs a task and sends
 * processed data using a {@link MessageProducer}.
 * Implementations of this interface must define the {@code work} method, which
 * runs on a worker thread and may block. Returning normally completes the task,
 * throwing an exception fails it, and output data can be published using the
 * {@link MessageProducer}.
 * When OpenK9 no longer accepts the data of the schedule, for example because it
 * was cancelled, the next write throws {@link java.util.concurrent.CancellationException}:
 * let it propagate, so that the work stops.
 *
 * @see io.vertx.core.eventbus.MessageProducer
 * @see io.openk9.connector.api.beans.IngestionDTO
 */
public interface Worker {

    void work(MessageProducer<IngestionDTO> producer) throws Exception;
}
