package ${package};

import io.openk9.connector.api.beans.InvokeRequest;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.Vertx;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class CrawlingService {

    @Inject
    Vertx vertx;
    @Inject
    WorkerFactory workerFactory;


    @ConfigProperty(name = "ingestion.port")
    private int port;

    @ConfigProperty(name = "ingestion.host")
    private String host;

    @ConfigProperty(name = "ingestion.path")
    private String path;


    public void init(@Observes StartupEvent event) {
        vertx.eventBus().registerCodec(new IngestionDTOCodec());
        vertx.deployVerticle(new HttpPublisherVerticle(port, host, path));
    }

    public void crawling(InvokeRequest invokeRequest) {
        vertx.deployVerticle(new CrawlerVerticle(workerFactory.createWorker(invokeRequest), invokeRequest));
    }

    @ConsumeEvent("undeploy")
    public void undeploy(String deploymentID) {
        vertx.undeploy(deploymentID);
    }
}