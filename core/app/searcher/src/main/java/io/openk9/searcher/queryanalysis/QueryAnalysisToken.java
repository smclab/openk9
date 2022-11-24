package io.openk9.searcher.queryanalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class QueryAnalysisToken {
	private String text;
	private Integer start;
	private Integer end;
	private Map<String, Object> token;
	private Integer[] pos;
}