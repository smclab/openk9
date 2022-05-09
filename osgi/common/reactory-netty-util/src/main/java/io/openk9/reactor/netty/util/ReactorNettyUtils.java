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

package io.openk9.reactor.netty.util;

import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.server.HttpServerRequest;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Function;

public class ReactorNettyUtils {

	public static Mono<String> aggregateBodyAsString(
		HttpServerRequest request, Charset charset) {
		return _aggregateAs(
			request, byteBufMono -> byteBufMono.asString(charset));
	}

	public static Mono<String> aggregateBodyAsString(
		HttpServerRequest request) {
		return _aggregateAs(request, ByteBufMono::asString);
	}

	public static Mono<byte[]> aggregateBodyAsByteArray(
		HttpServerRequest request) {
		return _aggregateAs(request, ByteBufMono::asByteArray);
	}

	public static Mono<InputStream> aggregateBodyAsInputStream(
		HttpServerRequest request) {
		return _aggregateAs(request, ByteBufMono::asInputStream);
	}

	public static Mono<ByteBuffer> aggregateBodyAsByteBuffer(
		HttpServerRequest request) {
		return _aggregateAs(request, ByteBufMono::asByteBuffer);
	}

	private static <T> Mono<T> _aggregateAs(
		HttpServerRequest request, Function<ByteBufMono, Mono<T>> mapper) {

		return mapper.apply(
			request
				.receive()
				.aggregate()
		);

	}

}
