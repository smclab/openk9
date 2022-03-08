package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.EnrichItemHttp;
import io.openk9.api.aggregator.client.EnrichItemClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/datasource")
public class EnrichItemResource implements EnrichItemHttp {
	@RestClient
	@Inject
	@Delegate
	EnrichItemClient _enrichItemClient;
}
