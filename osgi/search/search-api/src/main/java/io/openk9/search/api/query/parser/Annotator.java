package io.openk9.search.api.query.parser;

import java.util.List;
import java.util.Set;

public interface Annotator extends Comparable<Annotator> {

	default List<CategorySemantics> annotate(String...tokens) {
		return annotate(-1, tokens);
	}

	List<CategorySemantics> annotate(long tenantId, String...tokens);

	default List<CategorySemantics> annotate(
		long tenantId,
		Set<String> context, String...tokens) {
		return annotate(tenantId, tokens);
	}

	int weight();

}
