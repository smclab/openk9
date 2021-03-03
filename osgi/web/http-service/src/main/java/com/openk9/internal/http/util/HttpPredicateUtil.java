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

package com.openk9.internal.http.util;

import com.openk9.http.web.HttpHandler;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.vavr.CheckedFunction1;
import reactor.netty.http.server.HttpServerRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("all")
public class HttpPredicateUtil {

	public static Predicate<HttpServerRequest> delete(String uri) {
		return http(uri, null, HttpMethod.DELETE);
	}

	public static Predicate<HttpServerRequest> patch(String uri) {
		return http(uri, null, HttpMethod.PATCH);
	}

	public static Predicate<HttpServerRequest> get(String uri) {
		return http(uri, null, HttpMethod.GET);
	}

	public static Predicate<HttpServerRequest> head(String uri) {
		return http(uri, null, HttpMethod.HEAD);
	}

	public static Predicate<HttpServerRequest> http(
		String uri, HttpVersion protocol, HttpMethod method) {

		return (Predicate<HttpServerRequest>)
			_methodInvokerMap
				.getOrDefault(_HTTP_METHOD, objects -> null)
				.apply(new Object[]{uri, protocol, method});
	}

	public static Predicate<HttpServerRequest> options(String uri) {
		return http(uri, null, HttpMethod.OPTIONS);
	}

	public static Predicate<HttpServerRequest> post(String uri) {
		return http(uri, null, HttpMethod.POST);
	}

	public static Predicate<HttpServerRequest> prefix(String prefix) {
		return prefix(prefix, HttpMethod.GET);
	}

	public static Predicate<HttpServerRequest> prefix(
		String prefix, HttpMethod method) {

		return (Predicate<HttpServerRequest>)
			_methodInvokerMap
				.getOrDefault(_PREFIX_METHOD, objects -> null)
				.apply(new Object[]{prefix, method});
	}

	public static Predicate<HttpServerRequest> put(String uri) {
		return http(uri, null, HttpMethod.PUT);
	}

	public static Function<String, Predicate<HttpServerRequest>>
		getPredicate(int method) {

		return _predicateMap
			.entrySet()
			.stream()
			.filter(e -> (e.getKey() & method) != 0)
			.map(Map.Entry::getValue)
			.reduce((f1, f2) ->
				s -> Predicates.or(f1.apply(s), f2.apply(s)))
			.orElseGet(() -> s -> p -> false);
	}

	private static final int _PUBLIC_STATIC =
		Modifier.PUBLIC + Modifier.STATIC;

	private static final MethodKey _PREFIX_METHOD =
		MethodKey.of("prefix", String.class, HttpMethod.class);

	private static final MethodKey _HTTP_METHOD =
		MethodKey.of("http", String.class, HttpVersion.class, HttpMethod.class);

	private static class MethodKey {
		final String methodName;
		final Class[] types;

		MethodKey(String methodName, Class[] types) {
			this.methodName = methodName;
			this.types = types;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			MethodKey that = (MethodKey) o;
			return methodName.equals(that.methodName) &&
				   Arrays.equals(types, that.types);
		}

		@Override
		public int hashCode() {
			int result = Objects.hash(methodName);
			result = 31 * result + Arrays.hashCode(types);
			return result;
		}

		private static MethodKey of(String methodName, Class...types) {
			return new MethodKey(methodName, types);
		}

	}

	private static Map<MethodKey, Function<Object[], Object>>
		_methodInvokerMap = new HashMap<>();

	private static Map<Integer, Function<String, Predicate<HttpServerRequest>>>
		_predicateMap = new HashMap<>();

	static {

		try {
			Class<?> aClass1 =
				ClassLoaderUtil.getClassLoader()
					.loadClass("reactor.netty.http.server.HttpPredicate");

			for (Method m : aClass1.getDeclaredMethods()) {
				if ((m.getModifiers() & _PUBLIC_STATIC) == _PUBLIC_STATIC) {
					m.setAccessible(true);
					String methodName = m.getName();
					Class<?>[] parameterTypes = m.getParameterTypes();
					_methodInvokerMap.put(
						MethodKey.of(methodName, parameterTypes),
						CheckedFunction1.<Object[], Object>narrow(
							(objs) -> m.invoke(null, objs)).unchecked()
					);
				}
			}

			_predicateMap
				.put(HttpHandler.GET, HttpPredicateUtil::get);
			_predicateMap
				.put(HttpHandler.POST, HttpPredicateUtil::post);
			_predicateMap
				.put(HttpHandler.PUT, HttpPredicateUtil::put);
			_predicateMap
				.put(HttpHandler.DELETE, HttpPredicateUtil::delete);
			_predicateMap
				.put(HttpHandler.PATCH, HttpPredicateUtil::patch);
			_predicateMap
				.put(HttpHandler.OPTIONS, HttpPredicateUtil::options);

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}


	}


}
