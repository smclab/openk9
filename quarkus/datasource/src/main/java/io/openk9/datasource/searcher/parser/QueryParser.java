package io.openk9.datasource.searcher.parser;

import io.vertx.core.json.JsonObject;

import java.util.function.Consumer;

public interface QueryParser extends Consumer<ParserContext> {

	String getType();

	default boolean isQueryParserGroup() {
		return true;
	}

	default void configure(JsonObject configuration) {}

	default JsonObject getConfiguration() {
		return new JsonObject();
	}

}
