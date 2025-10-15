package ${package};

import io.openk9.enricher.api.ProcessResource;
import io.openk9.enricher.api.beans.OpenK9Input;
#if ($implementationType == "async")
import jakarta.inject.Inject;
#end
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

public class ProcessResourceImpl implements ProcessResource {

    private static final Logger LOGGER = Logger.getLogger(ProcessResourceImpl.class);

    #if ($implementationType == "async")@Inject
    CallBackClient callBackClient;

    @Override
    public void process(@NotNull OpenK9Input data) {
        if (data.getReplyTo() != null && !data.getReplyTo().isEmpty()) {
            LOGGER.info("Starting enrichment of data...");
            // Simulate call back endpoint
            EnrichData enrichData = new EnrichData();
            enrichData.setPayload(data.getPayload());
            callBackClient.callback(enrichData, data.getReplyTo());
        }
        else {
            throw new IllegalArgumentException("replyTo is null or empty");
        }
    }
    #elseif ($implementationType == "sync")@Override
    public void process(@NotNull OpenK9Input data) {
        LOGGER.info("sync test success!");
    }
    #end

}
