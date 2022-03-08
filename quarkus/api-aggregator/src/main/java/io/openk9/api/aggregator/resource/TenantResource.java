package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.TenantHttp;
import io.openk9.api.aggregator.client.TenantClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/datasource")
public class TenantResource implements TenantHttp {
	@RestClient
	@Inject
	@Delegate
	TenantClient _tenantClient;
}
