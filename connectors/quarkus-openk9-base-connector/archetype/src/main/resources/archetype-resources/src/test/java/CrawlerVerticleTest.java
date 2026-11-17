package ${package};

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openk9.connector.api.beans.DatasourcePayload;
import io.openk9.connector.api.beans.IngestionDTO;
import io.openk9.connector.api.beans.InvokeRequest;
import io.openk9.connector.api.beans.PayloadType;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Checks that a 406 from OpenK9, which has no queue for a schedule any more,
 * stops only the execution of that schedule and leaves the connector running.
 */
public class CrawlerVerticleTest {

    private static final String CANCELLED_SCHEDULE_ID = UUID.randomUUID().toString();
    private static final String ACTIVE_SCHEDULE_ID = UUID.randomUUID().toString();
    private static final long TIMEOUT_SECONDS = 10;

    private final List<JsonObject> received = new CopyOnWriteArrayList<>();
    private final CompletableFuture<Void> activeLastReceived = new CompletableFuture<>();
    private Vertx vertx;

    @BeforeEach
    public void setUp() throws Exception {
        vertx = Vertx.vertx();
        vertx.eventBus().registerCodec(new IngestionDTOCodec());
        // CrawlingService undeploys the CrawlerVerticle that asks for it
        vertx.eventBus().<String>consumer("undeploy", message -> vertx.undeploy(message.body()));

        // fake OpenK9 ingestion, without a queue for the cancelled schedule
        HttpServer ingestion = vertx.createHttpServer()
                .requestHandler(request -> request.body().onSuccess(body -> {
                    JsonObject ingestionDTO = body.toJsonObject();
                    received.add(ingestionDTO);
                    String scheduleId = ingestionDTO.getString("scheduleId");
                    if (ACTIVE_SCHEDULE_ID.equals(scheduleId)
                            && PayloadType.LAST.value().equals(ingestionDTO.getString("type"))) {
                        activeLastReceived.complete(null);
                    }
                    request.response()
                            .setStatusCode(CANCELLED_SCHEDULE_ID.equals(scheduleId) ? 406 : 200)
                            .end();
                }))
                .listen(0)
                .toCompletionStage()
                .toCompletableFuture()
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        HttpPublisherVerticle publisher = new HttpPublisherVerticle(
                new ObjectMapper(), 1, ingestion.actualPort(), "localhost", "/ingestion");
        await(vertx.deployVerticle(publisher).toCompletionStage().toCompletableFuture());
    }

    @AfterEach
    public void tearDown() throws Exception {
        await(vertx.close().toCompletionStage().toCompletableFuture());
    }

    @Test
    public void a406StopsOnlyTheExecutionThatReceivesIt() throws Exception {
        // 1. The worker of the cancelled schedule sends documents until it is stopped.
        // Its InvokeRequest uses upper case, as OpenK9 may send it.
        InvokeRequest cancelled = invokeRequest(CANCELLED_SCHEDULE_ID.toUpperCase());
        CompletableFuture<Exception> cancelledFailure = new CompletableFuture<>();
        Worker loopingWorker = (promise, producer) -> {
            try {
                for (int i = 0; i < 100; i++) {
                    producer.write(document(cancelled));
                    Thread.sleep(50);
                }
            } catch (CancellationException e) {
                cancelledFailure.complete(e);
                throw e;
            } catch (Exception e) {
                cancelledFailure.complete(e);
                promise.fail(e);
                return;
            }
            cancelledFailure.complete(null);
            promise.complete();
        };
        String cancelledDeploymentId = await(
                vertx.deployVerticle(new CrawlerVerticle(loopingWorker, cancelled))
                        .toCompletionStage()
                        .toCompletableFuture());

        // its next write fails, so the worker stops
        assertInstanceOf(CancellationException.class, await(cancelledFailure));

        assertOnlyTheCancelledExecutionStopped(cancelledDeploymentId);
    }

    @Test
    public void a406StopsAnAsynchronousWorkerThatFailsThePromise() throws Exception {
        // 1. The worker of the cancelled schedule writes inside a callback, as the README
        // example does, and passes the exception to promise.fail
        InvokeRequest cancelled = invokeRequest(CANCELLED_SCHEDULE_ID);
        CompletableFuture<Exception> cancelledFailure = new CompletableFuture<>();
        Worker asynchronousWorker = (promise, producer) -> {
            AtomicInteger written = new AtomicInteger();
            vertx.setPeriodic(50, timerId -> {
                try {
                    producer.write(document(cancelled));
                    if (written.incrementAndGet() == 100) {
                        vertx.cancelTimer(timerId);
                        cancelledFailure.complete(null);
                        promise.complete();
                    }
                } catch (Exception e) {
                    vertx.cancelTimer(timerId);
                    cancelledFailure.complete(e);
                    promise.fail(e);
                }
            });
        };
        String cancelledDeploymentId = await(
                vertx.deployVerticle(new CrawlerVerticle(asynchronousWorker, cancelled))
                        .toCompletionStage()
                        .toCompletableFuture());

        // its next write fails inside the callback
        assertInstanceOf(CancellationException.class, await(cancelledFailure));

        assertOnlyTheCancelledExecutionStopped(cancelledDeploymentId);
    }

    private void assertOnlyTheCancelledExecutionStopped(String cancelledDeploymentId)
            throws Exception {
        // 2. The connector still runs a new execution to the end
        InvokeRequest active = invokeRequest(ACTIVE_SCHEDULE_ID);
        Worker oneDocumentWorker = (promise, producer) -> {
            producer.write(document(active));
            promise.complete();
        };
        await(vertx.deployVerticle(new CrawlerVerticle(oneDocumentWorker, active))
                .toCompletionStage()
                .toCompletableFuture());
        await(activeLastReceived);

        // 3. The cancelled execution was undeployed, and sent neither LAST nor HALT
        assertFalse(vertx.deploymentIDs().contains(cancelledDeploymentId),
                "cancelled CrawlerVerticle still deployed");
        assertFalse(received.stream().anyMatch(ingestionDTO ->
                CANCELLED_SCHEDULE_ID.equals(ingestionDTO.getString("scheduleId"))
                        && !PayloadType.DOCUMENT.value().equals(ingestionDTO.getString("type"))));
    }

    private static <T> T await(CompletableFuture<T> future) throws Exception {
        return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static InvokeRequest invokeRequest(String scheduleId) {
        InvokeRequest invokeRequest = new InvokeRequest();
        invokeRequest.setDatasourceId(1L);
        invokeRequest.setScheduleId(scheduleId);
        invokeRequest.setTenantId("tenant");
        invokeRequest.setTimestamp(0L);
        return invokeRequest;
    }

    private static IngestionDTO document(InvokeRequest invokeRequest) {
        IngestionDTO ingestionDTO = new IngestionDTO();
        ingestionDTO.setContentId(UUID.randomUUID());
        ingestionDTO.setDatasourceId(invokeRequest.getDatasourceId());
        ingestionDTO.setScheduleId(UUID.fromString(invokeRequest.getScheduleId()));
        ingestionDTO.setTenantId(invokeRequest.getTenantId());
        ingestionDTO.setParsingDate(invokeRequest.getTimestamp().toString());
        ingestionDTO.setRawContent("document");
        ingestionDTO.setDatasourcePayload(new DatasourcePayload());
        ingestionDTO.setType(PayloadType.DOCUMENT);
        return ingestionDTO;
    }
}
