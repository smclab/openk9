package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.SearcherHttp;
import io.openk9.api.aggregator.client.SearcherClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/searcher")
public class SearcherResource extends FaultTolerance implements SearcherHttp {
	@RestClient
	@Inject
	@Delegate
	SearcherClient _searcherClient;
}
