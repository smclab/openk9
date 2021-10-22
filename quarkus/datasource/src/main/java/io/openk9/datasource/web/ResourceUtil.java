package io.openk9.datasource.web;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class ResourceUtil {

	public static String getFilterQuery(Map<String, Object> maps) {

		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = maps.keySet().iterator();

		Function<String, String> condition =
			key -> key.endsWith("_not")
				? " != "
				: " = ";

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

		return sb.toString();

	}

}
