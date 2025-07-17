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
public class QueryAnalysisRequest {
	@Schema(description = "Search text to perform query analysis")
	private String searchText;
	@Schema(description = "List of QueryAnalysisToken accepted by user")
	private List<QueryAnalysisToken> tokens;
}