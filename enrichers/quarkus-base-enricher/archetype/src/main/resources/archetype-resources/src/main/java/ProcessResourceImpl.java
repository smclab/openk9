package ${package};

import io.openk9.enricher.api.ProcessResource;
import io.openk9.enricher.api.beans.EnrichItemConfig;
import io.openk9.enricher.api.beans.OpenK9Input;
import io.openk9.enricher.api.beans.Payload;
import io.openk9.enricher.api.beans.ProcessResponseDTO;
#if ($implementationType == "async")
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
#end
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessResourceImpl implements ProcessResource {

    private static final Logger LOGGER = Logger.getLogger(ProcessResourceImpl.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final String DEFAULT_TARGET_FIELD = "enrichExample";

#if ($implementationType == "async")
    @Inject
    Vertx vertx;

    @Inject
    CallBackClient callBackClient;

    @Override
    public ProcessResponseDTO process(@NotNull OpenK9Input data) {
        if (data.getReplyTo() == null || data.getReplyTo().isEmpty()) {
            throw new IllegalArgumentException("replyTo is null or empty");
        }
        // Accept the request at once, then send the enriched payload to OpenK9 when it is ready.
        // Not ordered: documents are independent, so a slow one must not hold back the others.
        vertx.executeBlocking(() -> enrich(data.getPayload(), data.getEnrichItemConfig()), false)
                .onSuccess(payload -> callBackClient.callback(payload, data.getReplyTo()))
                .onFailure(e -> LOGGER.error("Error enriching data", e));
        ProcessResponseDTO responseDTO = new ProcessResponseDTO();
        responseDTO.setPayload("OK");
        return responseDTO;
    }
#elseif ($implementationType == "sync")
    @Override
    public ProcessResponseDTO process(@NotNull OpenK9Input data) {
        ProcessResponseDTO responseDTO = new ProcessResponseDTO();
        responseDTO.setPayload(enrich(data.getPayload(), data.getEnrichItemConfig()));
        return responseDTO;
    }
#end

    /**
     * Example enrichment: reads each binary of the document from its pre-signed staging URL and
     * adds its name and size to the payload, in the target field. OpenK9 takes the returned payload
     * as the new document, so the result goes into the payload instead of being returned on its own.
     * The enrich item configuration holds the values set in the fields of {@link FormResourceImpl}:
     * this example reads targetField and readBinaries.
     */
    private Payload enrich(Payload payload, EnrichItemConfig config) {
        boolean readBinaries = readBinaries(config);
        List<Map<String, Object>> binaries = new ArrayList<>();
        for (Map<String, Object> binary : binaries(payload)) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("name", binary.get("name"));
            if (!readBinaries) {
                binaries.add(info);
                continue;
            }
            Object stagingUrl = binary.get("stagingUrl");
            if (stagingUrl == null) {
                LOGGER.warnf("Binary %s has no stagingUrl", binary.get("name"));
                continue;
            }
            CompletableFuture<HttpResponse<byte[]>> download = null;
            try {
                // The staging URL is a bearer credential: never log it, nor the messages that quote it
                download = HTTP_CLIENT.sendAsync(
                        HttpRequest.newBuilder(URI.create(stagingUrl.toString())).GET().build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                // Bounds the whole download: the request timeout of the JDK client stops at the headers
                HttpResponse<byte[]> response = download.get(30, TimeUnit.SECONDS);
                if (response.statusCode() >= 300) {
                    LOGGER.warnf("Binary %s not read: HTTP %d", binary.get("name"), response.statusCode());
                    continue;
                }
                info.put("size", response.body().length);
                binaries.add(info);
            } catch (IllegalArgumentException e) {
                LOGGER.warnf("Binary %s not read: %s", binary.get("name"), e.getClass().getSimpleName());
            } catch (ExecutionException e) {
                LOGGER.warnf("Binary %s not read: %s", binary.get("name"), e.getCause().getClass().getSimpleName());
            } catch (TimeoutException e) {
                download.cancel(true);
                LOGGER.warnf("Binary %s not read: download took more than 30s", binary.get("name"));
            } catch (InterruptedException e) {
                download.cancel(true);
                Thread.currentThread().interrupt();
                break;
            }
        }
        payload.setAdditionalProperty(targetField(config), Map.of("binaries", binaries));
        return payload;
    }

    private static String targetField(EnrichItemConfig config) {
        Object targetField = config == null ? null : config.getAdditionalProperties().get("targetField");
        return targetField == null || targetField.toString().isBlank()
                ? DEFAULT_TARGET_FIELD
                : targetField.toString();
    }

    private static boolean readBinaries(EnrichItemConfig config) {
        Object readBinaries = config == null ? null : config.getAdditionalProperties().get("readBinaries");
        return readBinaries == null || Boolean.parseBoolean(readBinaries.toString());
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> binaries(Payload payload) {
        Object resources = payload.getAdditionalProperties().get("resources");
        if (resources instanceof Map<?, ?> map && map.get("binaries") instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }
}
