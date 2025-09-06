package com.phonebook.service;

/**
 * Exception thrown when a service operation fails.
 * This is a runtime exception that wraps underlying exceptions
 * from the persistence layer or other service operations.
 */
public class ContactServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new ContactServiceException with the specified detail message.
     *
     * @param message the detail message
     */
    public ContactServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new ContactServiceException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of this exception
     */
    public ContactServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ContactServiceException with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public ContactServiceException(Throwable cause) {
        super(cause);
    }
}