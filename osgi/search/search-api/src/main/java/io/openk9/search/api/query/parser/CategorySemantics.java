package io.openk9.search.api.query.parser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@ToString
public class CategorySemantics {
	private final String category;
	private final Map<String, Object> semantics;
}
