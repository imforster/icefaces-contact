package com.phonebook.service;

/**
 * General exception for ContactService operations.
 * Thrown when business logic operations fail due to system errors.
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
     * @param cause   the cause of the exception
     */
    public ContactServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ContactServiceException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public ContactServiceException(Throwable cause) {
        super(cause);
    }
}