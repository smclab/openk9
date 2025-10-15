package ${package};

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * This class provides the byte array-encoded format of a binary payload.
 * To retrieve the correct payload, you must provide both a {@code resourceId} and a {@code schemaName}.
 *
 * @see WebClient
 */
@ApplicationScoped
public class ByteArrayClient {

    private static final Logger LOGGER = Logger.getLogger(ByteArrayClient.class);

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "io.openk9.enricher.file.manager.api.port")
    private int port;

    @ConfigProperty(name = "io.openk9.enricher.file.manager.api.host")
    private String host;

    @ConfigProperty(name = "io.openk9.enricher.file.manager.byte.array.api.path")
    private String path;

    public Future<Buffer> getByteArray(String resourceId, String schemaName) {
        WebClient client = WebClient.create(vertx);
        return client
                .get(port, host, path + resourceId + "/" + schemaName)
                .as(BodyCodec.buffer())
                .send()
                .map(HttpResponse::body)
                .onSuccess(res -> LOGGER.info("Result from byte array endpoint: " + res.getBytes()))
                .onFailure(res -> LOGGER.error("Error requesting byte array binaries: " + res.getMessage()));
    }
}
