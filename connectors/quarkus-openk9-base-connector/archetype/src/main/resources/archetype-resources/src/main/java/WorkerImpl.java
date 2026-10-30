package ${package};

import io.openk9.connector.api.beans.DatasourcePayload;
import io.openk9.connector.api.beans.IngestionDTO;
import io.openk9.connector.api.beans.InvokeRequest;
import io.openk9.connector.api.beans.PayloadType;
import io.openk9.connector.api.beans.ResourcesDTO;
import io.vertx.core.eventbus.MessageProducer;

import java.util.ArrayList;
import java.util.UUID;

public class WorkerImpl implements Worker {

    private InvokeRequest invokeRequest;

    public WorkerImpl(InvokeRequest invokeRequest) {
        this.invokeRequest = invokeRequest;
    }

    @Override
    public void work(MessageProducer<IngestionDTO> producer) throws InterruptedException {
        // The datasource configuration, with the fields of FormResourceImpl, is in
        // invokeRequest.getDatasourceConfig().getAdditionalProperties()

        // Simulate an I/O-bound delay condition
        Thread.sleep(10000);
        // Setting up an IngestionDTO example
        IngestionDTO ingestionDTO = new IngestionDTO();
        ingestionDTO.setContentId(UUID.randomUUID());
        ingestionDTO.setDatasourceId(invokeRequest.getDatasourceId());
        ingestionDTO.setScheduleId(UUID.fromString(invokeRequest.getScheduleId()));
        ingestionDTO.setTenantId(invokeRequest.getTenantId());
        ingestionDTO.setParsingDate(invokeRequest.getTimestamp().toString());
        ingestionDTO.setRawContent("Test-Ingestion-Payload");
        ingestionDTO.setDatasourcePayload(new DatasourcePayload());

        ResourcesDTO resourcesDTO = new ResourcesDTO();
        resourcesDTO.setBinaries(new ArrayList<>());
        ingestionDTO.setResources(resourcesDTO);

        ingestionDTO.setType(PayloadType.DOCUMENT);
        producer.write(ingestionDTO);
    }
}
