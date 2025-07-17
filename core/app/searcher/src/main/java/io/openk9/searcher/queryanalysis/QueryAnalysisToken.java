package io.openk9.searcher.queryanalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class QueryAnalysisToken {
	@Schema(description = "Text associated to analysis")
	private String text;
	@Schema(description = "Character offset where analysis starts")
	private Integer start;
	@Schema(description = "Character offset where analysis ends")
	private Integer end;
	@Schema(description = "List of token associated to analysis")
	private Map<String, Object> token;
	@Schema(description = "Token position for analysis")
	private Integer[] pos;
}