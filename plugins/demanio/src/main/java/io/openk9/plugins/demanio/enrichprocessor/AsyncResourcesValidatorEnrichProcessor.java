package io.openk9.plugins.demanio.enrichprocessor;

import io.openk9.search.enrich.api.AsyncEnrichProcessor;
import io.openk9.search.enrich.api.EnrichProcessor;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = EnrichProcessor.class)
public class AsyncResourcesValidatorEnrichProcessor implements AsyncEnrichProcessor {
    @Override
    public String destinationName() {
        return "resources-validator";
    }

    @Override
    public String name() {
        return AsyncResourcesValidatorEnrichProcessor.class.getName();
    }
}
