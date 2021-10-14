package io.openk9.search.query.internal.response;

import io.openk9.search.query.internal.response.suggestions.Suggestions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SuggestionsResponse {
	private Collection<Suggestions> result;
	private String afterKey;
}
