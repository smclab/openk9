package ${package};

import io.openk9.connector.api.beans.DatasourcePayload;
import io.openk9.connector.api.beans.IngestionDTO;
import io.openk9.connector.api.beans.InvokeRequest;
import io.openk9.connector.api.beans.PayloadType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageProducer;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.concurrent.CancellationException;

import static ${package}.HttpPublisherVerticle.ADDRESS;

public class CrawlerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = Logger.getLogger(CrawlerVerticle.class);
    /**
     * Prefix of the event bus address, followed by the schedule id, that stops the execution
     * of that schedule.
     */
    public static final String CANCEL_ADDRESS = CrawlerVerticle.class.getName() + ".cancel.";
    private Worker worker;
    private InvokeRequest invokeRequest;


    public CrawlerVerticle(Worker worker, InvokeRequest invokeRequest) {
        this.worker = worker;
        this.invokeRequest = invokeRequest;
    }

    @Override
    public void start() {
        DeliveryOptions options = new DeliveryOptions().setCodecName("ingestionDTOCodec");
        CancellableProducer producer =
                new CancellableProducer(vertx.eventBus().publisher(ADDRESS, options));
        // HttpPublisherVerticle writes the schedule id as a lower case UUID
        String scheduleId = UUID.fromString(invokeRequest.getScheduleId()).toString();
        vertx.eventBus().consumer(CANCEL_ADDRESS + scheduleId, message -> producer.cancel());
        vertx.executeBlocking(promise -> worker.work(promise, producer), result -> {
            if (producer.isCancelled()) {
                // OpenK9 has no queue for this schedule, so it would reject LAST and HALT too
                LOGGER.warnf("No queue in OpenK9 for schedule %s: execution stopped", scheduleId);
            } else if (result.succeeded()) {
                producer.write(createLastIngestionDTO(invokeRequest));
            } else {
                LOGGER.error("Worker failed, sending HALT", result.cause());
                producer.write(createHaltIngestionDTO(invokeRequest, result.cause()));
            }
            undeployVerticle();
        });
    }

    @Override
    public void stop() {
        LOGGER.info("Verticle closed: " + deploymentID());
    }

    private IngestionDTO createHaltIngestionDTO(InvokeRequest invokeRequest, Throwable cause) {
        IngestionDTO ingestionDTO = new IngestionDTO();
        ingestionDTO.setContentId(UUID.randomUUID());
        ingestionDTO.setDatasourceId(invokeRequest.getDatasourceId());
        ingestionDTO.setScheduleId(UUID.fromString(invokeRequest.getScheduleId()));
        ingestionDTO.setTenantId(invokeRequest.getTenantId());
        ingestionDTO.setParsingDate(invokeRequest.getTimestamp().toString());
        ingestionDTO.setDatasourcePayload(new DatasourcePayload());
        // OpenK9 stores the HALT rawContent as the scheduling error: send only the exception type,
        // since its message may hold source URLs or credentials. The full cause is in the log.
        ingestionDTO.setRawContent("Worker failed: " + cause.getClass().getSimpleName());
        ingestionDTO.setType(PayloadType.HALT);
        return ingestionDTO;
    }

    private IngestionDTO createLastIngestionDTO(InvokeRequest invokeRequest) {
        IngestionDTO ingestionDTO = new IngestionDTO();
        ingestionDTO.setContentId(UUID.randomUUID());
        ingestionDTO.setDatasourceId(invokeRequest.getDatasourceId());
        ingestionDTO.setScheduleId(UUID.fromString(invokeRequest.getScheduleId()));
        ingestionDTO.setTenantId(invokeRequest.getTenantId());
        ingestionDTO.setParsingDate(invokeRequest.getTimestamp().toString());
        ingestionDTO.setDatasourcePayload(new DatasourcePayload());
        ingestionDTO.setType(PayloadType.LAST);
        return ingestionDTO;
    }

    private void undeployVerticle() {
        String deploymentID = context.deploymentID();
        vertx.eventBus().send("undeploy", deploymentID);
    }

    /**
     * Producer given to the worker: once the schedule is cancelled, each write throws
     * {@link CancellationException}, so the worker stops.
     */
    private static final class CancellableProducer implements MessageProducer<IngestionDTO> {

        private final MessageProducer<IngestionDTO> delegate;
        private volatile boolean cancelled;

        private CancellableProducer(MessageProducer<IngestionDTO> delegate) {
            this.delegate = delegate;
        }

        private void cancel() {
            cancelled = true;
        }

        private boolean isCancelled() {
            return cancelled;
        }

        @Override
        public MessageProducer<IngestionDTO> deliveryOptions(DeliveryOptions options) {
            delegate.deliveryOptions(options);
            return this;
        }

        @Override
        public String address() {
            return delegate.address();
        }

        @Override
        public void write(IngestionDTO body, Handler<AsyncResult<Void>> handler) {
            checkNotCancelled();
            delegate.write(body, handler);
        }

        @Override
        public Future<Void> write(IngestionDTO body) {
            checkNotCancelled();
            return delegate.write(body);
        }

        @Override
        public Future<Void> close() {
            return delegate.close();
        }

        @Override
        public void close(Handler<AsyncResult<Void>> handler) {
            delegate.close(handler);
        }

        private void checkNotCancelled() {
            if (cancelled) {
                throw new CancellationException("OpenK9 has no queue for this schedule any more");
            }
        }
    }
}
