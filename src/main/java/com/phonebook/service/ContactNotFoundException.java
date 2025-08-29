package com.phonebook.service;

/**
 * Exception thrown when a requested contact is not found.
 * This is a specific type of ContactServiceException for not found scenarios.
 */
public class ContactNotFoundException extends ContactServiceException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ContactNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public ContactNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ContactNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ContactNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}