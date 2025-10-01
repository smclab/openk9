package ${package};

import io.openk9.enricher.api.ProcessResource;
import io.openk9.enricher.api.beans.OpenK9Input;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

public class ProcessResourceImpl implements ProcessResource {

    private static final Logger LOGGER = Logger.getLogger(ProcessResourceImpl.class);

    @Inject
    CallBackClient callBackClient;

    @Override
    public void process(@NotNull OpenK9Input data) {
        LOGGER.info("Starting enrichment of data...");
        // Simulate call back endpoint
        callBackClient.callback(data);
    }
}
