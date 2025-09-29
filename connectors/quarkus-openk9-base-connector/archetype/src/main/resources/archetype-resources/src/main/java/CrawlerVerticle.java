package ${package};

import io.openk9.connector.api.beans.IngestionDTO;
import io.openk9.connector.api.beans.InvokeRequest;
import io.openk9.connector.api.beans.PayloadType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageProducer;
import org.jboss.logging.Logger;

import java.util.UUID;

import static ${package}.HttpPublisherVerticle.ADDRESS;

public class CrawlerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = Logger.getLogger(CrawlerVerticle.class);
    private Worker worker;
    private InvokeRequest invokeRequest;


    public CrawlerVerticle(Worker worker, InvokeRequest invokeRequest) {
        this.worker = worker;
        this.invokeRequest = invokeRequest;
    }

    @Override
    public void start() {
        DeliveryOptions options = new DeliveryOptions().setCodecName("ingestionDTOCodec");
        MessageProducer<IngestionDTO> producer = vertx.eventBus().publisher(ADDRESS, options);
        vertx.executeBlocking(promise -> worker.work(promise, producer), result -> {
            if (result.succeeded()) {
                undeployVerticle();
            } else if (result.failed()) {
                producer.write(createHaltIngestionDTO(invokeRequest));
                undeployVerticle();
            }
        });
    }

    @Override
    public void stop() {
        LOGGER.info("Verticle closed: " + deploymentID());
    }

    private IngestionDTO createHaltIngestionDTO(InvokeRequest invokeRequest) {
        IngestionDTO ingestionDTO = new IngestionDTO();
        ingestionDTO.setContentId(UUID.randomUUID());
        ingestionDTO.setDatasourceId(invokeRequest.getDatasourceId());
        ingestionDTO.setScheduleId(UUID.fromString(invokeRequest.getScheduleId()));
        ingestionDTO.setTenantId(invokeRequest.getTenantId());
        ingestionDTO.setParsingDate(invokeRequest.getTimestamp().toString());
        ingestionDTO.setType(PayloadType.HALT);
        return ingestionDTO;
    }

    private void undeployVerticle() {
        String deploymentID = context.deploymentID();
        vertx.eventBus().send("undeploy", deploymentID);
    }
}
