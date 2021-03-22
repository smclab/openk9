package io.openk9.relationship.graph.api.util;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class Pair<KEY, VALUE> {
	private final KEY key;
	private final VALUE value;
}
