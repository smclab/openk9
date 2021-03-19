package io.openk9.http.exception;

import org.reactivestreams.Publisher;

public class HttpException extends RuntimeException {

	public HttpException(int statusCode, String reason) {
		super(reason);
		_reason = reason;
		_statusCode = statusCode;
	}

	public HttpException(
		int statusCode, String reason, Publisher<String> body) {
		this(statusCode, reason);
		_body = body;
	}

	public int getStatusCode() {
		return _statusCode;
	}

	public String getReason() {
		return _reason;
	}

	public Publisher<String> getBody() {
		return _body;
	}

	private final String _reason;

	private final int _statusCode;

	private Publisher<String> _body;

}
