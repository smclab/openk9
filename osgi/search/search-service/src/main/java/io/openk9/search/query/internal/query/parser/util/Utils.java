package io.openk9.search.query.internal.query.parser.util;

import io.openk9.search.api.query.parser.Tuple;

public class Utils {

	public static String[] split(String s) {
		return s.split("\\s+");
	}

	public static <T> Tuple<T> toTuple(T[] rhs) {
		if (rhs.length == 0) {
			return Tuple.of();
		}
		return Tuple.of(rhs);
	}

	public static String[] toArray(Tuple<String> tuple) {
		return tuple.toArray();
	}

}
