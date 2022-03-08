package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.SuggestionCategoryHttp;
import io.openk9.api.aggregator.client.SuggestionCategoryClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/datasource")
public class SuggestionCategoryResource implements SuggestionCategoryHttp {
	@RestClient
	@Inject
	@Delegate
	SuggestionCategoryClient _suggestionCategoryClient;
}
