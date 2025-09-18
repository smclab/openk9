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
 *
 * @see io.vertx.core.Promise
 * @see io.vertx.core.eventbus.MessageProducer
 * @see io.openk9.connector.api.beans.IngestionDTO
 */
public interface Worker {

    void work(Promise<?> promise, MessageProducer<IngestionDTO> producer);
}
