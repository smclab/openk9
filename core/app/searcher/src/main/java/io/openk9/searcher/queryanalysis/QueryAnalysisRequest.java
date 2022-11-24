package io.openk9.searcher.queryanalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class QueryAnalysisRequest {
	private String searchText;
	private List<QueryAnalysisToken> tokens;
}