package io.openk9.search.api.query.parser;

import java.util.List;

public interface Annotator {

	default List<CategorySemantics> annotate(String...tokens) {
		return annotate(-1, tokens);
	}

	List<CategorySemantics> annotate(long tenantId, String...tokens);

}
