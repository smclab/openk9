package ${package};

import io.openk9.connector.api.beans.InvokeRequest;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WorkerFactoryImpl implements WorkerFactory {

    @Override
    public Worker createWorker(InvokeRequest invokeRequest) {
        return new WorkerImpl(invokeRequest);
    }
}
