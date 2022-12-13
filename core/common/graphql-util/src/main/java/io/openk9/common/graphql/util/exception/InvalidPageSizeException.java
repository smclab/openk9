package io.openk9.common.graphql.util.exception;

public class InvalidPageSizeException extends Exception {

    public InvalidPageSizeException(String message) {
        this(message, null);
    }

    public InvalidPageSizeException(String message, Throwable cause) {
        super(message, cause);
    }

}