package io.openk9.search.api.query.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Tuple<T> {

	public Tuple(T[] args) {
		_args = args;
	}

	public T[] toArray() {
		return _args;
	}

	public T get(int index) {
		return _args[index];
	}

	public T getOrDefault(int index, T defaultValue) {
		if (isEmpty() || (index < 0 || index >= _args.length)) {
			return defaultValue;
		}

		T result = _args[index];

		if (result == null) {
			return defaultValue;
		}

		return result;

	}

	public boolean isEmpty() {
		return _args.length == 0;
	}

	public static <T> Tuple<T> of() {
		return (Tuple<T>)EMPTY_TUPLE;
	}

	@SafeVarargs
	public static <T> Tuple<T> of(T...args) {

		if (args.length == 2) {
			T frt = args[0];
			if (frt instanceof Integer) {
				Map<Integer, Tuple<?>> integerTupleMap = _CACHE.get(frt);
				if (integerTupleMap != null) {
					T snd = args[1];
					if (snd instanceof Integer) {
						Tuple<?> tuple = integerTupleMap.get(snd);
						if (tuple != null) {
							return (Tuple<T>)tuple;
						}
					}
				}
			}
		}

		return new Tuple<>(args);
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

	@Override
	public String toString() {
		return Arrays
			.stream(_args)
			.map(Objects::toString)
			.collect(Collectors.joining(",", "(", ")"));
	}

	private final T[] _args;

	public static final Tuple EMPTY_TUPLE = new Tuple(new Object[0]);

	private static final Map<Integer, Map<Integer, Tuple<?>>> _CACHE = new HashMap<>();

	static {
		for (int i = 0; i < 50; i++) {
			for (int j = 0; j < 50; j++) {
				_CACHE.put(i, Map.of(j, Tuple.of(i, j)));
			}
		}
	}

}
