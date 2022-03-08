package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.EnrichPipelineHttp;
import io.openk9.api.aggregator.client.EnrichPipelineClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/datasource")
public class EnrichPipelineResource implements EnrichPipelineHttp {
	@RestClient
	@Inject
	@Delegate
	EnrichPipelineClient _enrichPipelineClient;
}
