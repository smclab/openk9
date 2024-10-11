package io.openk9.datasource.searcher;

public class MissingLLMException extends RuntimeException {
	public MissingLLMException() {
		super();
	}

	public MissingLLMException(String message) {
		super(message);
	}

	public MissingLLMException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingLLMException(Throwable cause) {
		super(cause);
	}
}
