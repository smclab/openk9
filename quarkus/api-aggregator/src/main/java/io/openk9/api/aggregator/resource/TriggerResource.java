package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.TriggerHttp;
import io.openk9.api.aggregator.client.TriggerClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/datasource")
public class TriggerResource implements TriggerHttp {
	@RestClient
	@Inject
	@Delegate
	TriggerClient _triggerClient;
}
