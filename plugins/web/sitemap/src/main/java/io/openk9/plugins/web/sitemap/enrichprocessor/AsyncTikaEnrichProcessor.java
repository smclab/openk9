package io.openk9.plugins.web.sitemap.enrichprocessor;

import io.openk9.model.BinaryPayload;
import io.openk9.model.IngestionPayload;
import io.openk9.search.enrich.api.AsyncEnrichProcessor;
import io.openk9.search.enrich.api.EnrichProcessor;
import org.osgi.service.component.annotations.Component;

import java.util.List;

@Component(immediate = true, service = EnrichProcessor.class)
public class AsyncTikaEnrichProcessor implements AsyncEnrichProcessor {

    @Override
    public String destinationName() {
        return "io.openk9.tika";
    }

    @Override
    public String name() {
        return AsyncTikaEnrichProcessor.class.getName();
    }

    @Override
    public boolean validate(IngestionPayload ingestionPayload) {

        List<BinaryPayload> binaries = ingestionPayload.getResources().getBinaries();
        return binaries != null && !binaries.isEmpty();
    }
}
