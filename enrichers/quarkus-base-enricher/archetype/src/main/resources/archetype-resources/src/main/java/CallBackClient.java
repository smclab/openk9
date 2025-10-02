package ${package};

import io.openk9.enricher.api.beans.OpenK9Input;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Use this class when an asynchronous approach is set from OpenK9, during the configuration of the enricher.
 * When the enrichment of data is complete, {@code callback()} method send it with the token received to notify OpenK9 which payload the enriched data refers to.
 *
 * @see OpenK9Input
 * @see WebClient
 */
@ApplicationScoped
public class CallBackClient {

    private static final Logger LOGGER = Logger.getLogger(CallBackClient.class);

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "io.openk9.enricher.callback.api.host")
    private String host;

    @ConfigProperty(name = "io.openk9.enricher.callback.api.port")
    private int port;

    @ConfigProperty(name = "io.openk9.enricher.callback.api.path")
    private String path;

    public void callback(EnrichData enrichData, String tokenId) {
        WebClient client = WebClient.create(vertx);
        client
                .post(port, host, path + tokenId)
                .sendJson(enrichData)
                .onSuccess(res -> LOGGER.info("Enrich data sent to OpenK9 successfully!"))
                .onFailure(res -> LOGGER.error("Error sending enrich data: " + res.getMessage()));
    }
}
