package com.phonebook.service;

/**
 * Exception thrown when contact validation fails.
 * This is a specific type of ContactServiceException for cases
 * where Bean Validation constraints are violated.
 */
public class ContactValidationException extends ContactServiceException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ContactValidationException with the specified detail message.
     *
     * @param message the detail message containing validation errors
     */
    public ContactValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ContactValidationException with the specified detail message and cause.
     *
     * @param message the detail message containing validation errors
     * @param cause   the cause of this exception
     */
    public ContactValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}