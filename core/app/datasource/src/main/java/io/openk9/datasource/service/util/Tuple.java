package io.openk9.datasource.service.util;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Tuple<T> implements Iterable<T> {
	private final Object[] values;

	public Tuple(T value1, T value2) {
		this.values = new Object[] {value1, value2};
	}

	public Tuple(T value) {
		this.values = new Object[] {value};
	}

	public Tuple() {
		this.values = EMPTY;
	}

	public Tuple(T[] values) {
		this.values = values;
	}

	public <T> Tuple<T> empty() {
		return (Tuple<T>)EMPTY_TUPLE;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < values.length;
			}

			@Override
			public T next() {
				return (T)values[index++];
			}
		};
	}

	public Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	private static final Object[] EMPTY = new Object[0];
	public static final Tuple EMPTY_TUPLE = new Tuple();

}
