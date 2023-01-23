package io.openk9.common.util.function;

import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class Predicates {

	private Predicates() {}

	public static <T> Predicate<T> positive() {
		return (Predicate<T>)TRUE;
	}

	public static <T> Predicate<T> negative() {
		return (Predicate<T>)FALSE;
	}

	public static <T> Predicate<T> not(Predicate<T> predicate) {
		return predicate.negate();
	}

	public static <T> Predicate<T> and(Predicate<T> p1, Predicate<T> p2) {
		return p1.and(p2);
	}

	public static <T> Predicate<T> or(Predicate<T> p1, Predicate<T> p2) {
		return p1.or(p2);
	}

	public static <T> Predicate<T> isEqual(T targetRef) {
		return Predicate.isEqual(targetRef);
	}

	public static final Predicate<?> FALSE = __ -> false;
	public static final Predicate<?> TRUE = __ -> true;

}
