package io.openk9.neo4j.relationship.graph.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionWrapper {

	public static <A, B> List<B> wrapList(List<A> list, Function<A, B> mapper) {
		return list
			.stream()
			.map(mapper)
			.collect(Collectors.toList());
	}

	public static <K, V, NV> Map<K, NV> wrapMap(
		Map<K, V> map, Function<V, NV> mapper) {

		return map
			.entrySet()
			.stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				kvEntry -> mapper.apply(kvEntry.getValue())));
	}

}
