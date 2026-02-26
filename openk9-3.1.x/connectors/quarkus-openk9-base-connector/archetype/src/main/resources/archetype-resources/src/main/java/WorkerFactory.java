package ${package};

import io.openk9.connector.api.beans.InvokeRequest;

/**
 * Factory interface for creating {@link Worker} instances based on an {@link InvokeRequest}.
 * Implementations of this interface <strong>must be application scoped</strong>,
 * meaning a single instance is shared and reused throughout the application's lifecycle.
 *
 * @see Worker
 * @see InvokeRequest
 */
public interface WorkerFactory {
    /**
     * Creates a new {@link Worker} instance using the given {@link InvokeRequest}.
     *
     * @param invokeRequest the request containing the data to create the worker
     */
    Worker createWorker(InvokeRequest invokeRequest);
}