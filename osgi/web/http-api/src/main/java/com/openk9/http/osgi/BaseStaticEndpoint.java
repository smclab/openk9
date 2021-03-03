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

package com.openk9.http.osgi;

import com.openk9.http.web.HttpHandler;
import com.openk9.http.web.HttpRequest;
import com.openk9.http.web.HttpResponse;
import org.osgi.framework.Bundle;
import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseStaticEndpoint implements HttpHandler {

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String path;

		if (!pathParam().isEmpty()) {
			path = _getPath(
				getStaticFolder(), httpRequest.pathParam(pathParam()));
		}
		else {
			path = _getPath(
				getStaticFolder(), httpRequest.path().substring(getPath().length()));
		}

		return sendStaticContent(path, _bundle, httpResponse);

	}

	protected String pathParam() {
		return "";
	}

	public abstract String getStaticFolder();

	public static Publisher<Void> sendStaticContent(
		String path, Bundle bundle, HttpResponse httpResponse) {

		return Mono.defer(() -> {

				URL resource = bundle.getResource(path);

				if (resource == null) {
					return Mono.from(httpResponse.sendNotFound());
				}

				try(InputStream is = resource.openStream();
					ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

					is.transferTo(buffer);

					Optional<String> extensionByStringHandling =
						getExtensionByStringHandling(path);

					extensionByStringHandling
						.filter(s -> s.equals("js"))
						.ifPresent(s -> httpResponse.header(
							"Content-Type",
							"application/javascript; charset=UTF-8"));

					return Mono.from(
						httpResponse.sendByteArray(
							Mono.just(buffer.toByteArray())));
				}
				catch (Exception e) {
					throw Exceptions.bubble(e);
				}

			});

	}

	private static Optional<String> getExtensionByStringHandling(
		String filename) {

		return Optional.ofNullable(filename)
			.filter(f -> f.contains("."))
			.map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}

	private String _getPath(String first, String... more) {

		String last = more[more.length - 1];

		return Stream
			.concat(Stream.of(first), Arrays.stream(more))
			.filter(e -> !e.isEmpty())
			.collect(
				Collectors.joining(
					"/", "/",
					last.endsWith("/") ? "/" : ""));

	}

	protected Bundle _bundle;

}
