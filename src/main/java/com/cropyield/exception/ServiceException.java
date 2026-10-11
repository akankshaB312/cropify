package com.cropyield.exception;

/**
 * Unchecked exception used by the service layer to wrap lower‑level failures
 * such as {@link java.sql.SQLException}. It is a runtime exception because the
 * service API is intended for higher‑level code (e.g., a UI controller) that
 * may decide to handle or propagate the error.
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(String message) {
        super(message);
    }
}
