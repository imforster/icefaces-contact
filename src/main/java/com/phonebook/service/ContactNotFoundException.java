package com.phonebook.service;

/**
 * Exception thrown when a requested contact is not found.
 * This is a specific type of ContactServiceException for cases
 * where a contact lookup by ID fails.
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
     * @param cause   the cause of this exception
     */
    public ContactNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}