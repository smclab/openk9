package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.api.SuggestionCategoryFieldHttp;
import io.openk9.api.aggregator.client.SuggestionCategoryFieldClient;
import lombok.experimental.Delegate;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/api/datasource")
public class SuggestionCategoryFieldResource
	extends FaultTolerance
	implements SuggestionCategoryFieldHttp {
	@RestClient
	@Inject
	@Delegate
	SuggestionCategoryFieldClient _suggestionCategoryClient;
}
