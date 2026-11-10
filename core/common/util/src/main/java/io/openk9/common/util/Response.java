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

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Response<E> {
	private E entity;
	private List<FieldValidator> fieldValidators;
	private List<String> warnings;

	/**
	 * Builds a response that carries an entity or the validation errors
	 * that prevented its creation, and no warnings.
	 *
	 * @param entity          the entity, {@code null} when the request failed
	 * @param fieldValidators the validation errors, {@code null} when the
	 *                        request succeeded
	 * @param <T>             the entity type
	 * @return the response
	 */
	public static <T> Response<T> of(T entity, List<FieldValidator> fieldValidators) {
		return new Response<>(entity, fieldValidators, null);
	}

	/**
	 * Builds a response that carries an entity together with the warnings
	 * about it: non-blocking messages on a request that succeeded, such as a
	 * configuration that is incomplete but can be completed later.
	 *
	 * @param entity          the entity, {@code null} when the request failed
	 * @param fieldValidators the validation errors, {@code null} when the
	 *                        request succeeded
	 * @param warnings        the warnings, empty or {@code null} when none
	 * @param <T>             the entity type
	 * @return the response
	 */
	public static <T> Response<T> of(
		T entity, List<FieldValidator> fieldValidators, List<String> warnings) {

		return new Response<>(entity, fieldValidators, warnings);
	}

	public static <T> Response<T> error(List<FieldValidator> fieldValidators) {
		return Response.of(null, fieldValidators);
	}

	public static <T> Response<T> success(T entity) {
		return Response.of(entity, null);
	}

}
