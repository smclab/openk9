package io.openk9.relationship.graph.api.util;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Pair<KEY, VALUE> {
	private final KEY key;
	private final VALUE value;
}
