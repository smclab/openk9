package io.openk9.api.aggregator.client;

import io.openk9.api.aggregator.api.ReindexHttp;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "datasource-client")
@RegisterClientHeaders
public interface ReindexClient extends ReindexHttp {
}
