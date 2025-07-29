package io.openk9.searcher.queryanalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class QueryAnalysisResponse {
	@Schema(description = "Search text where performed query analysis")
	private String searchText;
	@Schema(description = "List of QueryAnalysisToken describing performed analysis")
	private List<QueryAnalysisTokens> analysis;
}