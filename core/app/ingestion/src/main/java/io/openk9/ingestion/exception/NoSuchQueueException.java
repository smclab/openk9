package io.openk9.ingestion.exception;

public class NoSuchQueueException extends RuntimeException {

	public NoSuchQueueException(String message) {
		super(message);
	}

	public NoSuchQueueException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchQueueException(Throwable cause) {
		super(cause);
	}
}
