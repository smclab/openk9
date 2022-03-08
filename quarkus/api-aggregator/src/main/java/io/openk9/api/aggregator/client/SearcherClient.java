package io.openk9.api.aggregator.client;

import io.openk9.api.aggregator.api.SearcherHttp;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "searcher-client")
@RegisterClientHeaders
public interface SearcherClient extends SearcherHttp {
}
