package io.openk9.http.web;

import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

public interface ExceptionHandler<T extends Throwable> {

	Class<T> getType();

	int getCode();

	default String getReason() {
		return null;
	}

	default Mono<Void> map(
		T throwable, HttpServerResponse response) {

		String reason = getReason();

		HttpResponseStatus httpResponseStatus;

		if (reason != null) {
			reason = reason.replaceAll("[\\n\\r]", "\t");
			httpResponseStatus = HttpResponseStatus.valueOf(getCode(), reason);
		}
		else {
			httpResponseStatus = HttpResponseStatus.valueOf(getCode());
		}

		response.status(httpResponseStatus);

		String message = throwable.getMessage();

		if (message != null && !message.isBlank()) {
			return response.sendString(Mono.just(message)).then();
		}

		return response.send();

	}

	class InternalServerErrorExceptionHandler
		implements ExceptionHandler<Throwable> {

		@Override
		public Class<Throwable> getType() {
			return Throwable.class;
		}

		@Override
		public int getCode() {
			return 500;
		}

	}

	ExceptionHandler<Throwable> INTERNAL_SERVER_ERROR =
		new InternalServerErrorExceptionHandler();

}
