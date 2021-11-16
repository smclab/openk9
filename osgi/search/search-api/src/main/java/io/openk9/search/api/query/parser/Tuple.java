package io.openk9.search.api.query.parser;

import java.util.Arrays;

public final class Tuple<T> {

	public Tuple(T[] args) {
		_args = args;
	}

	public T[] toArray() {
		return _args;
	}

	public static <T> Tuple<T> of() {
		return (Tuple<T>)EMPTY_TUPLE;
	}

	@SafeVarargs
	public static <T> Tuple<T> of(T...args) {
		return new Tuple(args);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Tuple tuple = (Tuple) o;
		return Arrays.equals(_args, tuple._args);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(_args);
	}

	private final T[] _args;

	public static final Tuple EMPTY_TUPLE = new Tuple(new Object[0]);

}
