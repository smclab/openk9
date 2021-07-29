package io.openk9.reactor.netty.util;

import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.server.HttpServerRequest;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
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
