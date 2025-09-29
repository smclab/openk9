package ${package};

import io.openk9.connector.api.beans.IngestionDTO;
import io.openk9.connector.api.beans.InvokeRequest;
import io.openk9.connector.api.beans.PayloadType;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageProducer;

import java.util.ArrayList;
import java.util.UUID;

public class WorkerImpl implements Worker {

    private InvokeRequest invokeRequest;

    public WorkerImpl(InvokeRequest invokeRequest) {
        this.invokeRequest = invokeRequest;
    }

    @Override
    public void work(Promise<?> promise, MessageProducer<IngestionDTO> producer) {
        // Simulate an I/O-bound delay condition
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Setting up an IngestionDTO example
        IngestionDTO ingestionDTO = new IngestionDTO();
        ingestionDTO.setContentId(UUID.randomUUID());
        ingestionDTO.setDatasourceId(invokeRequest.getDatasourceId());
        ingestionDTO.setScheduleId(UUID.fromString(invokeRequest.getScheduleId()));
        ingestionDTO.setTenantId(invokeRequest.getTenantId());
        ingestionDTO.setParsingDate(invokeRequest.getTimestamp().toString());
        ingestionDTO.setRawContent("Test-Ingestion-Payload");
        ingestionDTO.setDatasourcePayload(null);

        ResourcesDTO resourcesDTO = new ResourcesDTO();
        resourcesDTO.setBinaries(new ArrayList<>());
        ingestionDTO.setResources(resourcesDTO);

        ingestionDTO.setType(PayloadType.DOCUMENT);
        ingestionDTO.setLast(false);
        producer.write(ingestionDTO);
        promise.complete();
    }
}
