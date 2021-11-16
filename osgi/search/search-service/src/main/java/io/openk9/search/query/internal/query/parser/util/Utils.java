package io.openk9.search.query.internal.query.parser.util;

import io.vavr.Tuple;

public class Utils {

	public static Tuple toTuple(String[] rhs) {
		switch (rhs.length) {
			case 0: return Tuple.empty();
			case 1: return Tuple.of(rhs[0]);
			case 2: return Tuple.of(rhs[0], rhs[1]);
			case 3: return Tuple.of(rhs[0], rhs[1], rhs[2]);
			case 4: return Tuple.of(rhs[0], rhs[1], rhs[2], rhs[3]);
			case 5: return Tuple.of(rhs[0], rhs[1], rhs[2], rhs[3], rhs[4]);
			case 6: return Tuple.of(rhs[0], rhs[1], rhs[2], rhs[3], rhs[4], rhs[5]);
			case 7: return Tuple.of(rhs[0], rhs[1], rhs[2], rhs[3], rhs[4], rhs[5], rhs[6]);
			case 8: return Tuple.of(rhs[0], rhs[1], rhs[2], rhs[3], rhs[4], rhs[5], rhs[6], rhs[7]);
			default: throw new RuntimeException("tuple max size is: " + Tuple.MAX_ARITY);
		}
	}

	public static String[] toArray(Tuple tuple) {
		return tuple
			.toSeq()
			.map(o -> (String)o)
			.toJavaArray(String[]::new);
	}


}
