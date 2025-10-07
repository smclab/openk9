package ${package};

import io.openk9.enricher.api.ProcessResource;
import io.openk9.enricher.api.beans.OpenK9Input;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

public class ProcessResourceImpl implements ProcessResource {

    private static final Logger LOGGER = Logger.getLogger(ProcessResourceImpl.class);

    #if ($implementationType == "async")
    @Inject
    CallBackClient callBackClient;

    @Override
    public void process(@NotNull OpenK9Input data) {
        if (!data.getReplyTo().isEmpty() && data.getReplyTo() != null) {
            LOGGER.info("Starting enrichment of data...");
            // Simulate call back endpoint
            EnrichData enrichData = new EnrichData();
            enrichData.setPayload(data.getPayload());
            callBackClient.callback(enrichData, data.getReplyTo());
        }
    }

    #elseif ($implementationType == "sync")
        System.out.println("Test sincrono");
    #end

}
