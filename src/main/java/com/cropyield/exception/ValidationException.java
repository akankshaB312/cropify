package com.cropyield.exception;

/**
 * Thrown when input validation fails.
 * The message is intended to be safe for end‑users – it never contains
 * confidential information such as passwords.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
