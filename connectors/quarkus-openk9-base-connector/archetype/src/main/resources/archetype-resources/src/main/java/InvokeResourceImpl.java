package ${package};

import io.openk9.connector.api.InvokeResource;
import io.openk9.connector.api.beans.InvokeRequest;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

public class InvokeResourceImpl implements InvokeResource {

    private static final Logger LOGGER = Logger.getLogger(InvokeResourceImpl.class);

    @Inject
    CrawlingService crawlingService;

    @Override
    public void invoke(@NotNull InvokeRequest data) {
        crawlingService.crawling(data);
        LOGGER.info("Invoke Request Received...");
    }
}
