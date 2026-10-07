package it.gov.pagopa.common.web.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a client provides a malformed or invalid pagination cursor.
 */
public class InvalidCursorException extends ClientException {

    public InvalidCursorException(String message) {
        super(HttpStatus.BAD_REQUEST, "[INVALID_CURSOR] " +  message);
    }

    public InvalidCursorException(String message, Throwable ex) {
        super(HttpStatus.BAD_REQUEST, "[INVALID_CURSOR] " + message, ex);
    }
}