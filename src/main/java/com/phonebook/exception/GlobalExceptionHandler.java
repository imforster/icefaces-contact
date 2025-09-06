package com.phonebook.exception;

import com.phonebook.service.ContactNotFoundException;
import com.phonebook.service.ContactServiceException;
import com.phonebook.service.ContactValidationException;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global exception handler for application-wide exception management.
 * Provides centralized error handling and user-friendly error messages.
 */
@ApplicationScoped
public class GlobalExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * Handles database-related exceptions.
     * 
     * @param ex the database exception
     */
    public void handleDatabaseException(PersistenceException ex) {
        LOGGER.log(Level.SEVERE, "Database operation failed", ex);
        
        String userMessage = "A database error occurred. Please try again later.";
        String detailMessage = "If the problem persists, please contact support.";
        
        // Check for specific database constraint violations
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("UNIQUE")) {
                userMessage = "Duplicate Contact";
                detailMessage = "A contact with this name and phone number already exists.";
            } else if (ex.getMessage().contains("not null") || ex.getMessage().contains("NULL")) {
                userMessage = "Missing Required Information";
                detailMessage = "Please fill in all required fields.";
            }
        }
        
        addErrorMessage(userMessage, detailMessage);
    }

    /**
     * Handles validation exceptions from Bean Validation.
     * 
     * @param ex the validation exception
     */
    public void handleValidationException(ConstraintViolationException ex) {
        LOGGER.log(Level.WARNING, "Validation constraint violation", ex);
        
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        if (violations.isEmpty()) {
            addErrorMessage("Validation Error", "Please check your input and try again.");
            return;
        }
        
        // Display the first validation error
        ConstraintViolation<?> firstViolation = violations.iterator().next();
        String propertyName = getPropertyDisplayName(firstViolation.getPropertyPath().toString());
        String message = firstViolation.getMessage();
        
        addErrorMessage("Validation Error", propertyName + ": " + message);
        
        // Log all violations for debugging
        for (ConstraintViolation<?> violation : violations) {
            LOGGER.warning("Validation violation: " + violation.getPropertyPath() + " - " + violation.getMessage());
        }
    }

    /**
     * Handles custom contact validation exceptions.
     * 
     * @param ex the contact validation exception
     */
    public void handleContactValidationException(ContactValidationException ex) {
        LOGGER.log(Level.WARNING, "Contact validation failed", ex);
        addErrorMessage("Validation Error", ex.getMessage());
    }

    /**
     * Handles contact not found exceptions.
     * 
     * @param ex the contact not found exception
     */
    public void handleContactNotFoundException(ContactNotFoundException ex) {
        LOGGER.log(Level.WARNING, "Contact not found", ex);
        addErrorMessage("Contact Not Found", ex.getMessage());
    }

    /**
     * Handles general contact service exceptions.
     * 
     * @param ex the contact service exception
     */
    public void handleContactServiceException(ContactServiceException ex) {
        LOGGER.log(Level.SEVERE, "Contact service operation failed", ex);
        
        // Check if it's a specific subtype
        if (ex instanceof ContactNotFoundException) {
            handleContactNotFoundException((ContactNotFoundException) ex);
        } else if (ex instanceof ContactValidationException) {
            handleContactValidationException((ContactValidationException) ex);
        } else {
            addErrorMessage("Service Error", "An error occurred while processing your request. Please try again.");
        }
    }

    /**
     * Handles generic exceptions that aren't specifically handled elsewhere.
     * 
     * @param ex the generic exception
     */
    public void handleGenericException(Exception ex) {
        LOGGER.log(Level.SEVERE, "Unexpected error occurred", ex);
        
        String userMessage = "Unexpected Error";
        String detailMessage = "An unexpected error occurred. Please try again or contact support if the problem persists.";
        
        // Check for common exception types
        if (ex instanceof IllegalArgumentException) {
            userMessage = "Invalid Input";
            detailMessage = "Please check your input and try again.";
        } else if (ex instanceof SecurityException) {
            userMessage = "Access Denied";
            detailMessage = "You don't have permission to perform this operation.";
        }
        
        addErrorMessage(userMessage, detailMessage);
    }

    /**
     * Handles timeout exceptions.
     * 
     * @param ex the timeout exception
     */
    public void handleTimeoutException(Exception ex) {
        LOGGER.log(Level.WARNING, "Operation timed out", ex);
        addWarningMessage("Operation Timeout", 
            "The operation took longer than expected. Please try again.");
    }

    /**
     * Adds an error message to the JSF context.
     * 
     * @param summary the message summary
     * @param detail the detailed message
     */
    private void addErrorMessage(String summary, String detail) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
        }
    }

    /**
     * Adds a warning message to the JSF context.
     * 
     * @param summary the message summary
     * @param detail the detailed message
     */
    private void addWarningMessage(String summary, String detail) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, summary, detail));
        }
    }

    /**
     * Adds an info message to the JSF context.
     * 
     * @param summary the message summary
     * @param detail the detailed message
     */
    private void addInfoMessage(String summary, String detail) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
        }
    }

    /**
     * Converts property path to user-friendly display name.
     * 
     * @param propertyPath the property path from validation
     * @return user-friendly property name
     */
    private String getPropertyDisplayName(String propertyPath) {
        switch (propertyPath) {
            case "name":
                return "Name";
            case "phoneNumber":
                return "Phone Number";
            case "email":
                return "Email";
            default:
                // Capitalize first letter and replace camelCase with spaces
                return propertyPath.substring(0, 1).toUpperCase() + 
                       propertyPath.substring(1).replaceAll("([A-Z])", " $1");
        }
    }

    /**
     * Utility method to check if there are any error messages in the current context.
     * 
     * @return true if there are error messages, false otherwise
     */
    public boolean hasErrorMessages() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            return facesContext.getMessageList().stream()
                .anyMatch(msg -> msg.getSeverity().equals(FacesMessage.SEVERITY_ERROR));
        }
        return false;
    }

    /**
     * Utility method to clear all messages from the current context.
     */
    public void clearMessages() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.getMessageList().clear();
        }
    }
}