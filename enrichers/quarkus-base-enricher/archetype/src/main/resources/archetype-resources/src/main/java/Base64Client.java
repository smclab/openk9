package ${package};

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * This class provides the base64-encoded format of a binary payload.
 * To retrieve the correct payload, you must provide both a {@code resourceId} and a {@code schemaName}.
 *
 * @see WebClient
 */
@ApplicationScoped
public class Base64Client {

    private static final Logger LOGGER = Logger.getLogger(Base64Client.class);

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "io.openk9.enricher.base64.api.port")
    private int port;

    @ConfigProperty(name = "io.openk9.enricher.base64.api.host")
    private String host;

    @ConfigProperty(name = "io.openk9.enricher.base64.api.path")
    private String path;

    public Future<String> getBase64(String resourceId, String schemaName) {
        WebClient client = WebClient.create(vertx);
        return client
                .get(port, host, path + resourceId + "/" + schemaName)
                .as(BodyCodec.string())
                .send()
                .map(HttpResponse::body)
                .onSuccess(res -> LOGGER.info("Result from base64 endpoint: " + res))
                .onFailure(res -> LOGGER.error("Error requesting base64 binaries: " + res.getMessage()));
    }
}
