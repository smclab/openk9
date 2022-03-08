package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.DatasourceHttp;
import io.openk9.api.aggregator.client.DatasourceClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/datasource")
public class DatasourceResource implements DatasourceHttp {
	@RestClient
	@Inject
	@Delegate
	DatasourceClient _datasourceClient;
}
