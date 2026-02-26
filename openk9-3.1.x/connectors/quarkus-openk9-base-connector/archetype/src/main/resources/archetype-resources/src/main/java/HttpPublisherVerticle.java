package ${package};

import io.openk9.connector.api.beans.IngestionDTO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.jboss.logging.Logger;

/**
 * This class extends {@link AbstractVerticle}.
 * It overrides {@code start} method to set a {@link io.vertx.ext.web.client.WebClient} object
 * to send IngestionDTO objects to OpenK9 asynchronously.
 * Initializes the verticle using host, port, and path from application properties.
 * The class name is used as address to allow {@link io.vertx.core.eventbus.EventBus}
 * to consume messages from {@link io.vertx.core.eventbus.MessageProducer}.
 *
 * @see CrawlerVerticle
 * @see CrawlingService
 */
public class HttpPublisherVerticle extends AbstractVerticle {

    private static final Logger LOGGER = Logger.getLogger(HttpPublisherVerticle.class);
    public static final String ADDRESS = HttpPublisherVerticle.class.getName();

    private int port;
    private String host;
    private String path;
    private int maxPoolSize;

    public HttpPublisherVerticle(int maxPoolSize, int port, String host, String path) {
        this.maxPoolSize = maxPoolSize;
        this.port = port;
        this.host = host;
        this.path = path;
    }

    @Override
    public void start() {
        /*
           Configure and create a WebClient instance. When maxPoolSize is set to 1,
           only one active HTTP connection is allowed, which ensures that the order of the extracted payloads is preserved.
         */
        WebClientOptions webClientOptions = webClientOptionsConfiguration(); //
        WebClient client = WebClient.create(vertx, webClientOptions);
        vertx.eventBus().consumer(ADDRESS, message -> {
            IngestionDTO ingestionDTO = (IngestionDTO) message.body();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Message consumed: " + Json.encodePrettily(ingestionDTO));
            }
            client
                    .post(port, host, path)
                    .sendJson(ingestionDTO)
                    .onSuccess(res -> {
                        if (res.statusCode() == 406) {
                            LOGGER.error("HttpPublisherVerticle closed caused by 406 error");
                            vertx.close();
                        } else {
                            LOGGER.infof("IngestionDTO [%s, %s, %s] sent successfully\n",
                                    ingestionDTO.getTenantId(),
                                    ingestionDTO.getDatasourceId(),
                                    ingestionDTO.getContentId());
                        }
                    })
                    .onFailure(res -> {
                        LOGGER.error("Error sending IngestionDTO object: " + res.getMessage());
                    });
        });
    }

    private WebClientOptions webClientOptionsConfiguration() {
        WebClientOptions webClientOptions = new WebClientOptions();
        webClientOptions.setMaxPoolSize(maxPoolSize);
        return webClientOptions;
    }
}