package io.openk9.api.aggregator.client;

import io.openk9.api.aggregator.api.SuggestionCategoryFieldHttp;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "datasource-client")
@RegisterClientHeaders
public interface SuggestionCategoryFieldClient extends
	SuggestionCategoryFieldHttp {
}
