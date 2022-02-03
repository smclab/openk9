package io.openk9.search.api.query.parser;

import java.util.Arrays;
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

	public static Tuple<Integer> of(Integer frt, Integer snd) {

		try {
			return _CACHE[frt][snd];
		}
		catch (IndexOutOfBoundsException ioobe) {
			// ignore
		}

		return new Tuple<>(new Integer[] {frt, snd});
	}

	@SafeVarargs
	public static <T> Tuple<T> of(T...args) {

		if (args.length == 2) {
			try {
				T frt = args[0];
				if (frt instanceof Integer) {
					Tuple<?>[] tupleArray = _CACHE[(Integer)frt];
					T snd = args[1];
					if (snd instanceof Integer) {
						Tuple<?> tuple = tupleArray[(Integer)snd];
						if (tuple != null) {
							return (Tuple<T>)tuple;
						}
					}
				}
			}
			catch (IndexOutOfBoundsException ioobe) {
				// ignore
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

	private static final Tuple<Integer>[][] _CACHE = new Tuple[50][50];

	static {
		for (int i = 0; i < 50; i++) {
			for (int j = 0; j < 50; j++) {
				_CACHE[i][j] = Tuple.of(i, j);
			}
		}
	}

}
