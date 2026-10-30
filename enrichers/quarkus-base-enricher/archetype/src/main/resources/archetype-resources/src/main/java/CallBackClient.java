#if ($implementationType == "async")
package ${package};

import io.openk9.enricher.api.beans.Payload;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Map;

/**
 * Use this class when an asynchronous approach is set from OpenK9, during the configuration of the enricher.
 * When the enrichment of data is complete, {@code callback()} method sends the enriched payload with the token received,
 * to notify OpenK9 which document it refers to. OpenK9 takes the payload as the new document.
 *
 * @see Payload
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

    public void callback(Payload payload, String tokenId) {
        WebClient client = WebClient.create(vertx);
        client
                .post(port, host, path + tokenId)
                .sendJson(Map.of("payload", payload))
                .onSuccess(res -> {
                    if (res.statusCode() / 100 == 2) {
                        LOGGER.info("Enrich data sent to OpenK9 successfully!");
                    } else {
                        LOGGER.errorf("Enrich data rejected with HTTP %d: %s", res.statusCode(), res.bodyAsString());
                    }
                })
                .onFailure(res -> LOGGER.error("Error sending enrich data: " + res.getMessage()));
    }
}
#elseif ($implementationType == "sync")
/*

!!! This class can only be used with an ASYNC implementation !!!

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

    public void callback(Payload payload, String tokenId) {
        WebClient client = WebClient.create(vertx);
        client
                .post(port, host, path + tokenId)
                .sendJson(Map.of("payload", payload))
                .onSuccess(res -> {
                    if (res.statusCode() / 100 == 2) {
                        LOGGER.info("Enrich data sent to OpenK9 successfully!");
                    } else {
                        LOGGER.errorf("Enrich data rejected with HTTP %d: %s", res.statusCode(), res.bodyAsString());
                    }
                })
                .onFailure(res -> LOGGER.error("Error sending enrich data: " + res.getMessage()));
    }
}
*/
#end
