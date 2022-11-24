package io.openk9.datasource.searcher.parser;

import java.util.function.Consumer;

public interface QueryParser extends Consumer<ParserContext> {

	String getType();

	default boolean isQueryParserGroup() {
		return true;
	}

}
