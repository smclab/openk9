package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.ReindexHttp;
import io.openk9.api.aggregator.client.ReindexClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/datasource")
public class ReindexResource implements ReindexHttp {
	@RestClient
	@Inject
	@Delegate
	ReindexClient _reindexClient;
}
