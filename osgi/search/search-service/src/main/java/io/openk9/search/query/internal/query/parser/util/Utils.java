package io.openk9.search.query.internal.query.parser.util;

public class Utils {

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
