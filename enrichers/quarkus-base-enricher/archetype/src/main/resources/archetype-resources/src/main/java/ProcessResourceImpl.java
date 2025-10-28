package ${package};

import io.openk9.enricher.api.ProcessResource;
import io.openk9.enricher.api.beans.OpenK9Input;
import jakarta.inject.Inject;
import io.openk9.enricher.api.beans.ProcessResponseDTO;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

public class ProcessResourceImpl implements ProcessResource {

    private static final Logger LOGGER = Logger.getLogger(ProcessResourceImpl.class);

    @Inject
    Base64Client base64Client;

    @Inject
    ByteArrayClient byteArrayClient;

    #if ($implementationType == "async")@Inject
    CallBackClient callBackClient;

    @Override
    public ProcessResponseDTO process(@NotNull OpenK9Input data) {
        if (data.getReplyTo() != null && !data.getReplyTo().isEmpty()) {
            ProcessResponseDTO responseDTO = new ProcessResponseDTO();
            LOGGER.info("Starting enrichment of data...");
            /*
            // Simulate call back endpoint
            EnrichData enrichData = new EnrichData();
            enrichData.setPayload(data.getPayload());
            callBackClient.callback(enrichData, data.getReplyTo());
            */

            String resourceId = data.getPayload().getAdditionalProperties().get("resourceId").toString();
            String schemaName = data.getPayload().getAdditionalProperties().get("schemaName").toString();
            // Simulate base 64 call endpoint
            base64Client.getBase64(resourceId, schemaName);

            // Simulate byte array call endpoint
            byteArrayClient.getByteArray(resourceId, schemaName);

            responseDTO.setPayload("OK");
            return responseDTO;
        }
        else {
            throw new IllegalArgumentException("replyTo is null or empty");
        }
    }
    #elseif ($implementationType == "sync")@Override
    public ProcessResponseDTO process(@NotNull OpenK9Input data) {
        LOGGER.info("sync test success!");

        String resourceId = data.getPayload().getAdditionalProperties().get("resourceId").toString();
        String schemaName = data.getPayload().getAdditionalProperties().get("schemaName").toString();
        // Simulate base 64 call endpoint
        base64Client.getBase64(resourceId, schemaName);

        // Simulate byte array call endpoint
        byteArrayClient.getByteArray(resourceId, schemaName);

        return new ProcessResponseDTO();
    }
    #end

}