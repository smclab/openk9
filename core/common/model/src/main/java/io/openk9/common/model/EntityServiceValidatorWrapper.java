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

package io.openk9.common.model;

import io.openk9.common.util.FieldValidator;
import io.openk9.common.util.Response;
import io.smallrye.mutiny.Uni;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EntityServiceValidatorWrapper<E, D> {

	public EntityServiceValidatorWrapper(
		EntityService<E, D> delegate, Validator validator) {
		this.delegate = delegate;
		this.validator = validator;
	}

	public Uni<Response<E>> patch(long id, D dto) {
		return _validate(dto, () -> this.delegate.patch(id, dto));
	}

	public Uni<Response<E>> update(long id, D dto) {
		return _validate(dto, () -> this.delegate.update(id, dto));
	}

	public Uni<Response<E>> create(D dto) {
		return _validate(dto, () -> this.delegate.create(dto));
	}

	public <T> List<FieldValidator> validate(T dto) {
		return _toFieldValidators(validator.validate(dto));
	}

	private Uni<Response<E>> _validate(D dto, Supplier<Uni<E>> supplier) {

		return Uni.createFrom().deferred(() -> {

			Set<ConstraintViolation<D>> constraintViolationSet = validator.validate(dto);

			if (constraintViolationSet.isEmpty()) {
				return supplier
					.get()
					.onItemOrFailure()
					.transform((entity, t) -> {

						if (entity != null) {
							return Response.of(entity, null);
						}

						return Response.of(
							null, List.of(
								FieldValidator.of(
									"error",
									t.getMessage())
							)
						);

					});
			}

			List<FieldValidator>
				fieldValidatorList = _toFieldValidators(constraintViolationSet);

			return Uni.createFrom().item(Response.of(null, fieldValidatorList));

		});

	}

	private String _findFieldName(String detail) {
		Matcher matcher = PATTERN_FIELD_NAME.matcher(detail);

		if (matcher.find()) {
			return matcher.group(1);
		}

		return "";
	}

	private <T> List<FieldValidator> _toFieldValidators(
		Set<ConstraintViolation<T>> constraintViolationSet) {
		return constraintViolationSet.stream()
			.map(constraintViolation -> {
				String field = constraintViolation.getPropertyPath().toString();
				String message = constraintViolation.getMessage();
				return FieldValidator.of(field, message);
			})
			.collect(Collectors.toList());
	}

	private final EntityService<E, D> delegate;
	private final Validator validator;

	private static final Pattern PATTERN_FIELD_NAME = Pattern.compile(
		"Key \\(([a-zA-Z_-]+)\\)") ;

}
