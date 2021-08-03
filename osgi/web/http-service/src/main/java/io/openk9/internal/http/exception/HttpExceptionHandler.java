package io.openk9.internal.http.exception;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.openk9.http.exception.HttpException;
import io.openk9.http.web.ExceptionHandler;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

@Component(
	immediate = true,
	service = ExceptionHandler.class
)
public class HttpExceptionHandler implements ExceptionHandler<HttpException> {

	@Override
	public Class<HttpException> getType() {
		return HttpException.class;
	}

	@Override
	public int getCode() {
		return 0;
	}

	@Override
	public Mono<Void> map(
		HttpException throwable, HttpServerResponse response) {

		String reason = throwable.getReason();

		HttpResponseStatus httpResponseStatus;

		if (reason != null) {
			reason = reason.replaceAll("[\\n\\r]", "\t");
			httpResponseStatus = HttpResponseStatus.valueOf(
				throwable.getStatusCode(), reason);
		}
		else {
			httpResponseStatus = HttpResponseStatus.valueOf(
				throwable.getStatusCode());
		}

		response.status(httpResponseStatus);

		String message = throwable.getMessage();

		if (message != null && !message.isBlank()) {
			return response.sendString(Mono.just(message)).then();
		}

		return response.send();
	}
}
