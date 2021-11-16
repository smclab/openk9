package io.openk9.search.api.query.parser;

import java.util.List;
import java.util.Map;

public interface Annotator {

	default List<CategorySemantics> annotate(Map<Tuple, List<CategorySemantics>> context, String...tokens) {
		return annotate(-1, context, tokens);
	}

	List<CategorySemantics> annotate(long tenantId, Map<Tuple, List<CategorySemantics>> context, String...tokens);


}
