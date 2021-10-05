package io.openk9.search.query.internal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SuggestionsDTO {
	private List<Map<String, Object>> entities;
	private List<Map<String, Object>> datasources;
	private Map<String, Map<String, Object>> types;
}
