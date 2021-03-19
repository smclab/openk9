package io.openk9.http.web;

import io.openk9.http.web.error.ErrorHandler;
import org.reactivestreams.Publisher;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class HttpHandlerWrapper implements HttpHandler {

	public HttpHandlerWrapper(HttpHandler _httpHandler) {
		this._httpHandler = _httpHandler;
	}

	public int method() {
		return this._httpHandler.method();
	}

	public boolean prefix() {
		return this._httpHandler.prefix();
	}

	public ErrorHandler errorHandler() {
		return this._httpHandler.errorHandler();
	}

	public String getPath() {
		return this._httpHandler.getPath();
	}

	public Publisher<Void> apply(HttpRequest t, HttpResponse u) {
		return this._httpHandler.apply(t, u);
	}

	public <V> BiFunction<HttpRequest, HttpResponse, V> andThen(
		Function<? super Publisher<Void>, ? extends V> after) {
		return this._httpHandler.andThen(after);
	}

	private final HttpHandler _httpHandler;

}