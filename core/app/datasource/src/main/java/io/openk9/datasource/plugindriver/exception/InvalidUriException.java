package io.openk9.datasource.plugindriver.exception;

public class InvalidUriException extends RuntimeException {

	public InvalidUriException() {
			super();
		}

	public InvalidUriException(String message) {
		super(message);
	}

	public InvalidUriException(String message, Throwable cause) {
			super(message, cause);
		}

	public InvalidUriException(Throwable cause) {
			super(cause);
		}
}
