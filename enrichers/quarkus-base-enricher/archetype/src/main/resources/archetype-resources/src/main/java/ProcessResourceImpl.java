package ${package};

import io.openk9.enricher.api.ProcessResource;
import io.openk9.enricher.api.beans.OpenK9Input;
import io.openk9.enricher.api.beans.Payload;
import io.openk9.enricher.api.beans.ProcessResponseDTO;
#if ($implementationType == "async")
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
#end
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProcessResourceImpl implements ProcessResource {

    private static final Logger LOGGER = Logger.getLogger(ProcessResourceImpl.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
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
        // Accept the request at once, then send the enriched payload to OpenK9 when it is ready
        vertx.executeBlocking(() -> enrich(data.getPayload(), targetField(data)))
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
        responseDTO.setPayload(enrich(data.getPayload(), targetField(data)));
        return responseDTO;
    }
#end

    /**
     * The enrich item configuration holds the values set in the fields of {@link FormResourceImpl}.
     */
    private static String targetField(OpenK9Input data) {
        Object targetField = data.getEnrichItemConfig() == null
                ? null
                : data.getEnrichItemConfig().getAdditionalProperties().get("targetField");
        return targetField == null || targetField.toString().isBlank()
                ? DEFAULT_TARGET_FIELD
                : targetField.toString();
    }

    /**
     * Example enrichment: reads each binary of the document from its pre-signed staging URL and
     * adds its name and size to the payload, in the target field. OpenK9 takes the returned payload
     * as the new document, so the result goes into the payload instead of being returned on its own.
     */
    private Payload enrich(Payload payload, String targetField) {
        List<Map<String, Object>> binaries = new ArrayList<>();
        for (Map<String, Object> binary : binaries(payload)) {
            Object stagingUrl = binary.get("stagingUrl");
            if (stagingUrl == null) {
                LOGGER.warnf("Binary %s has no stagingUrl", binary.get("name"));
                continue;
            }
            try {
                // The staging URL is a bearer credential: never log it
                HttpResponse<byte[]> response = HTTP_CLIENT.send(
                        HttpRequest.newBuilder(URI.create(stagingUrl.toString())).GET().build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() >= 300) {
                    LOGGER.warnf("Binary %s not read: HTTP %d", binary.get("name"), response.statusCode());
                    continue;
                }
                Map<String, Object> info = new LinkedHashMap<>();
                info.put("name", binary.get("name"));
                info.put("size", response.body().length);
                binaries.add(info);
            } catch (IOException e) {
                LOGGER.warnf("Binary %s not read: %s", binary.get("name"), e.getClass().getSimpleName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        payload.setAdditionalProperty(targetField, Map.of("binaries", binaries));
        return payload;
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
