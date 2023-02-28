package io.openk9.common.util.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Collections {

	private Collections() {}

	public static <T> T head(java.util.Collection<T> collection) {

		java.util.Iterator<T> iterator = collection.iterator();

		if (iterator.hasNext()) {
			return iterator.next();
		}

		return null;
	}

	public static <T> Set<T> tail(Set<T> set) {

		if (set.isEmpty()) {
			return set;
		}

		Set<T> tail = new java.util.HashSet<>(set.size() - 1);

		java.util.Iterator<T> iterator = set.iterator();

		if (iterator.hasNext()) {
			iterator.next();
		}

		while (iterator.hasNext()) {
			tail.add(iterator.next());
		}

		return tail;

	}

	public static <T> java.util.List<T> tail(java.util.List<T> list) {

		if (list.isEmpty()) {
			return list;
		}

		List<T> tail = new ArrayList<>(list.size() - 1);

		for (int i = 1; i < list.size(); i++) {
			tail.add(list.get(i));
		}

		return tail;

	}

	public static <T> java.util.List<T> tailUnsafe(java.util.List<T> list) {

		if (list.isEmpty()) {
			return list;
		}

		return list.subList(1, list.size());
	}

	public static <T> T last(java.util.Collection<T> collection) {

		java.util.Iterator<T> iterator = collection.iterator();

		T t = null;

		while (iterator.hasNext()) {
			t = iterator.next();
		}

		return t;
	}

	public static <T> T last(java.util.List<T> list) {

		if (list.isEmpty()) {
			return null;
		}

		return list.get(list.size() - 1);

	}

	public static <T> java.util.List<T> tailUnsafe(java.util.List<T> list, int n) {

		if (list.isEmpty()) {
			return list;
		}

		return list.subList(n, list.size());

	}

}
