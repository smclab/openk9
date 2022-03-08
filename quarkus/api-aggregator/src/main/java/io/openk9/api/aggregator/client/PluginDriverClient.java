package io.openk9.api.aggregator.client;

import io.openk9.api.aggregator.api.PluginDriverHttp;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "plugin-driver-client")
@RegisterClientHeaders
public interface PluginDriverClient extends PluginDriverHttp {
}
