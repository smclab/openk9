package io.openk9.http.web;

import org.reactivestreams.Publisher;

import java.util.function.BiFunction;

public interface HttpFilter extends Comparable<HttpFilter> {

	Publisher<Void> doFilter(
		HttpRequest httpRequest, HttpResponse httpResponse,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> chain);

	String URL_PATTERNS = "url.patterns";

	default int weight() {
		return 10;
	}

	default int compareTo(HttpFilter other) {
		return Integer.compare(this.weight(), other.weight());
	}

}
