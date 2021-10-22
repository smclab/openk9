package io.openk9.datasource.web;

import io.smallrye.mutiny.tuples.Tuple2;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResourceUtil {

	public static Tuple2<String, Map<String, Object>> getFilterQuery(
		Map<String, Object> maps) {

		StringBuilder sb = new StringBuilder();

		Map<String, Object> collect =
			maps
				.entrySet()
				.stream()
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		Function<String, String> condition =
			key -> key.endsWith("_not")
				? " != "
				: " = ";

		Iterator<String> iterator = collect.keySet().iterator();

		if (iterator.hasNext()) {
			String key = iterator.next();
			sb.append(key).append(condition.apply(key)).append(':').append(key);
		}

		while (iterator.hasNext()) {
			String key = iterator.next();
			sb
				.append(" and ")
				.append(key)
				.append(condition.apply(key))
				.append(':')
				.append(key);
		}

		return Tuple2.of(sb.toString(), collect);

	}

}
