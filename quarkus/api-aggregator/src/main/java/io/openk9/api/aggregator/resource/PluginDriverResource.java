package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.PluginDriverHttp;
import io.openk9.api.aggregator.client.PluginDriverClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/plugin-driver-manager")
public class PluginDriverResource
	extends FaultTolerance implements PluginDriverHttp {
	@RestClient
	@Inject
	@Delegate
	PluginDriverClient _pluginDriverClient;
}
