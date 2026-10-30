package ${package};

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openk9.connector.api.beans.IngestionDTO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
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
    private ObjectMapper objectMapper;

    public HttpPublisherVerticle(ObjectMapper objectMapper, int maxPoolSize, int port, String host, String path) {
        this.objectMapper = objectMapper;
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
            /*
               Serialize with Jackson, as OpenK9 does: Vert.x Json encodes byte[] as unpadded
               base64url, which OpenK9 rejects, so binaries would never be ingested.
             */
            Buffer body;
            try {
                body = Buffer.buffer(objectMapper.writeValueAsBytes(ingestionDTO));
            } catch (JsonProcessingException e) {
                LOGGER.error("Error serializing IngestionDTO object: " + e.getMessage());
                return;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Message consumed: " + body);
            }
            client
                    .post(port, host, path)
                    .putHeader("Content-Type", "application/json")
                    .sendBuffer(body)
                    .onSuccess(res -> {
                        if (res.statusCode() == 406) {
                            LOGGER.error("HttpPublisherVerticle closed caused by 406 error");
                            vertx.close();
                        } else if (res.statusCode() / 100 != 2) {
                            LOGGER.errorf("IngestionDTO [%s, %s, %s] rejected with HTTP %d: %s",
                                    ingestionDTO.getTenantId(),
                                    ingestionDTO.getDatasourceId(),
                                    ingestionDTO.getContentId(),
                                    res.statusCode(),
                                    res.bodyAsString());
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