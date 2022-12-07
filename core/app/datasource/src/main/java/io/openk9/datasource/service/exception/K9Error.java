package io.openk9.datasource.service.exception;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("k9-error")
public class K9Error extends RuntimeException {

	public K9Error(String message) {
		super(message);
	}

	public K9Error(String message, Throwable cause) {
		super(message, cause);
	}

	public K9Error(Throwable cause) {
		super(cause);
	}

}
