/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.common.util;

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
