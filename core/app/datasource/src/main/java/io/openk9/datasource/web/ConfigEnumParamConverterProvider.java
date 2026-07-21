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

package io.openk9.datasource.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.config.model.ImportMode;

/**
 * Converts the import/export enum query params ({@link ConfigEntityType},
 * {@link ImportMode}) and turns an unknown value into a {@code 400} with a
 * speaking message, instead of the opaque {@code 404} a raw enum
 * {@code @QueryParam} conversion failure yields by default (JAX-RS §3.2 maps a
 * query-param conversion failure to {@code NotFoundException}).
 * <p>
 * Scoped to these two types on purpose: the enum stays the declared parameter
 * type in {@link ConfigResource} — so the OpenAPI schema keeps enumerating the
 * valid values — while the rest of the platform keeps its default behavior.
 */
@Provider
public class ConfigEnumParamConverterProvider implements ParamConverterProvider {

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> ParamConverter<T> getConverter(
		Class<T> rawType, Type genericType, Annotation[] annotations) {

		if (rawType == ConfigEntityType.class || rawType == ImportMode.class) {
			return new EnumParamConverter(rawType);
		}
		return null;
	}

	private static final class EnumParamConverter<E extends Enum<E>>
		implements ParamConverter<E> {

		private final Class<E> type;

		EnumParamConverter(Class<E> type) {
			this.type = type;
		}

		@Override
		public E fromString(String value) {
			if (value == null) {
				return null;
			}
			try {
				return Enum.valueOf(type, value);
			}
			catch (IllegalArgumentException e) {
				throw new BadRequestException(
					Response.status(Response.Status.BAD_REQUEST)
						.entity(
							"Unknown " + type.getSimpleName() + " '" + value
							+ "'; valid values: "
							+ Arrays.toString(type.getEnumConstants()))
						.type(MediaType.TEXT_PLAIN)
						.build());
			}
		}

		@Override
		public String toString(E value) {
			return value == null ? null : value.name();
		}

	}

}
