package ${package};

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * This class provides the base64-encoded format of a binary payload.
 * To retrieve the correct payload, you must provide the {@code resourceId} of the binary and, as
 * {@code schemaName}, the {@code tenantId} of the document.
 *
 * @see WebClient
 */
@ApplicationScoped
public class Base64Client {

    private static final Logger LOGGER = Logger.getLogger(Base64Client.class);

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "io.openk9.enricher.file.manager.api.port")
    private int port;

    @ConfigProperty(name = "io.openk9.enricher.file.manager.api.host")
    private String host;

    @ConfigProperty(name = "io.openk9.enricher.file.manager.base64.api.path")
    private String path;

    public Future<String> getBase64(String resourceId, String schemaName) {
        WebClient client = WebClient.create(vertx);
        return client
                .get(port, host, path + resourceId + "/" + schemaName)
                .as(BodyCodec.string())
                .send()
                .map(res -> {
                    if (res.statusCode() / 100 != 2) {
                        throw new IllegalStateException("File manager answered HTTP " + res.statusCode());
                    }
                    // The file manager answers 200 with an empty body when it does not have the resource
                    if (res.body() == null || res.body().isEmpty()) {
                        throw new IllegalStateException("File manager returned no content");
                    }
                    return res.body();
                })
                .onSuccess(res -> LOGGER.info("Base64 binary received: " + res.length() + " characters"))
                .onFailure(res -> LOGGER.error("Error requesting base64 binaries: " + res.getMessage()));
    }
}
