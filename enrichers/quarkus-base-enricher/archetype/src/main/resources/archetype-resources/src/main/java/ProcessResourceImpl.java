package ${package};

import io.openk9.enricher.api.ProcessResource;
import io.openk9.enricher.api.beans.EnrichItemConfig;
import io.openk9.enricher.api.beans.OpenK9Input;
import io.openk9.enricher.api.beans.Payload;
import io.openk9.enricher.api.beans.ProcessResponseDTO;
#if ($implementationType == "async")
import io.vertx.core.Vertx;
#end
import io.vertx.core.buffer.Buffer;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessResourceImpl implements ProcessResource {

    private static final Logger LOGGER = Logger.getLogger(ProcessResourceImpl.class);
    private static final String DEFAULT_TARGET_FIELD = "enrichExample";

    @Inject
    ByteArrayClient byteArrayClient;

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
     * Example enrichment: reads each binary of the document from the file manager, by its resourceId and the
     * tenantId of the document, and adds its name and size to the payload, in the target field. OpenK9 takes the
     * returned payload as the new document, so the result goes into the payload instead of being returned on its own.
     * The enrich item configuration holds the values set in the fields of {@link FormResourceImpl}:
     * this example reads targetField and readBinaries.
     */
    private Payload enrich(Payload payload, EnrichItemConfig config) {
        boolean readBinaries = readBinaries(config);
        Object tenantId = payload.getAdditionalProperties().get("tenantId");
        List<Map<String, Object>> binaries = new ArrayList<>();
        for (Map<String, Object> binary : binaries(payload)) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("name", binary.get("name"));
            if (!readBinaries) {
                binaries.add(info);
                continue;
            }
            Object resourceId = binary.get("resourceId");
            if (resourceId == null || tenantId == null) {
                LOGGER.warnf("Binary %s has no resourceId or the document no tenantId", binary.get("name"));
                continue;
            }
            try {
                Buffer content = byteArrayClient.getByteArray(resourceId.toString(), tenantId.toString())
                        .toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
                info.put("size", content.length());
                binaries.add(info);
            } catch (ExecutionException | TimeoutException e) {
                LOGGER.warnf("Binary %s not read: %s", binary.get("name"),
                        e.getCause() == null ? e.getClass().getSimpleName() : e.getCause().getMessage());
            } catch (InterruptedException e) {
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
