package io.openk9.reactor.netty.util;

import io.netty.handler.codec.http.HttpMethod;
import reactor.netty.http.server.HttpServerRequest;

import java.util.function.Predicate;

public final class HttpPrefixPredicate implements Predicate<HttpServerRequest> {

	final HttpMethod method;
	final String     prefix;

	public HttpPrefixPredicate(String prefix, HttpMethod method) {
		this.prefix = prefix;
		this.method = method;
	}

	@Override
	public boolean test(HttpServerRequest key) {
		return method.equals(key.method()) && key.uri().startsWith(prefix);
	}

	public static Predicate<HttpServerRequest> of(
		String prefix, HttpMethod httpMethod) {
		return new HttpPrefixPredicate(prefix, httpMethod);
	}


}