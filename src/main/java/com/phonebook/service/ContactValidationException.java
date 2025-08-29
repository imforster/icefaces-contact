package com.phonebook.service;

/**
 * Exception thrown when contact validation fails.
 * This is a specific type of ContactServiceException for validation errors.
 */
public class ContactValidationException extends ContactServiceException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ContactValidationException with the specified detail message.
     *
     * @param message the detail message
     */
    public ContactValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ContactValidationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ContactValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}