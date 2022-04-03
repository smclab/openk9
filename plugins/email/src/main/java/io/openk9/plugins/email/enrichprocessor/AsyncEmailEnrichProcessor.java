package io.openk9.plugins.email.enrichprocessor;

import io.openk9.search.enrich.api.AsyncEnrichProcessor;
import io.openk9.search.enrich.api.EnrichProcessor;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = EnrichProcessor.class)
public class AsyncEmailEnrichProcessor implements AsyncEnrichProcessor {

    @Override
    public String destinationName() {
        return "io.openk9.ner";
    }

    @Override
    public String name() {
        return AsyncEmailEnrichProcessor.class.getName();
    }

}