package io.openk9.search.api.query.parser;

import java.util.List;
import java.util.Set;

public interface Annotator extends Comparable<Annotator> {

	default List<CategorySemantics> annotate(Tuple<Integer> chartKey, String...tokens) {
		return annotate(chartKey, -1, tokens);
	}

	List<CategorySemantics> annotate(
		Tuple<Integer> chartKey, long tenantId, String...tokens);

	default List<CategorySemantics> annotate(
		Tuple<Integer> chartKey,
		long tenantId,
		Set<String> context, String...tokens) {
		return annotate(chartKey, tenantId, tokens);
	}

	int weight();

}
