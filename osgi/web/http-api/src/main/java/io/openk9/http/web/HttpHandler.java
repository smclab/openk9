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

package io.openk9.http.web;

import io.openk9.http.web.error.ErrorHandler;
import org.reactivestreams.Publisher;

import java.util.function.BiFunction;

public interface HttpHandler
	extends Endpoint, BiFunction<HttpRequest, HttpResponse, Publisher<Void>> {

	default int method() {
		return ALL;
	}

	default boolean prefix() {
		return false;
	}

	default ErrorHandler errorHandler() {
		return ErrorHandler.DEFAULT;
	}

	static HttpHandler get(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(GET, path, false, handle);
	}

	static HttpHandler post(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(POST, path, false, handle);
	}

	static HttpHandler put(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(PUT, path, false, handle);
	}

	static HttpHandler delete(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(DELETE, path, false, handle);
	}

	static HttpHandler patch(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(PATCH, path, false, handle);
	}

	static HttpHandler options(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(OPTIONS, path, false, handle);
	}

	static HttpHandler getPathPrefix(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(GET, path, true, handle);
	}

	static HttpHandler postPathPrefix(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(POST, path, true, handle);
	}

	static HttpHandler putPathPrefix(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(PUT, path, true, handle);
	}

	static HttpHandler deletePathPrefix(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(DELETE, path, true, handle);
	}

	static HttpHandler patchPathPrefix(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(PATCH, path, true, handle);
	}

	static HttpHandler optionsPathPrefix(
		String path,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {
		return methodHandle(OPTIONS, path, true, handle);
	}

	static HttpHandler methodHandle(
		int method, String path, boolean prefix,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle) {

		return new BaseHttpHandler(path, handle, method, prefix);
	}

	int GET =  		0b0_0_0_0_0_1;

	int POST = 		0b0_0_0_0_1_0;

	int PUT = 		0b0_0_0_1_0_0;

	int DELETE = 	0b0_0_1_0_0_0;

	int PATCH = 	0b0_1_0_0_0_0;

	int OPTIONS = 	0b1_0_0_0_0_0;

	int ALL = GET | POST | PUT | DELETE | PATCH | OPTIONS;

	class BaseHttpHandler implements HttpHandler {

		public BaseHttpHandler(
			String path,
			BiFunction<HttpRequest, HttpResponse, Publisher<Void>> handle,
			int method, boolean prefix) {
			_path = path;
			_handle = handle;
			_method = method;
			_prefix = prefix;
		}

		@Override
		public String getPath() {
			return _path;
		}

		@Override
		public Publisher<Void> apply(
			HttpRequest httpRequest, HttpResponse httpResponse) {
			return _handle.apply(httpRequest, httpResponse);
		}

		@Override
		public int method() {
			return _method;
		}

		@Override
		public boolean prefix() {
			return _prefix;
		}

		private final String _path;
		private final BiFunction<
			HttpRequest, HttpResponse, Publisher<Void>> _handle;
		private final int _method;
		private final boolean _prefix;


	}

}
