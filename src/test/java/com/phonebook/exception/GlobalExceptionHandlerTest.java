package com.phonebook.exception;

import com.phonebook.service.ContactNotFoundException;
import com.phonebook.service.ContactServiceException;
import com.phonebook.service.ContactValidationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests exception handling scenarios without JSF context dependencies.
 */
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Before
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void testHandleDatabaseException() {
        // Test generic database exception - should not throw exception
        PersistenceException ex = new PersistenceException("Database connection failed");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleDatabaseException(ex);
        
        // Test passes if no exception is thrown
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleDatabaseExceptionWithUniqueConstraint() {
        // Test unique constraint violation
        PersistenceException ex = new PersistenceException("unique constraint violation");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleDatabaseException(ex);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleDatabaseExceptionWithNotNullConstraint() {
        // Test not null constraint violation
        PersistenceException ex = new PersistenceException("not null constraint violation");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleDatabaseException(ex);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleValidationException() {
        // Test with empty violations set
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolationException ex = new ConstraintViolationException("Validation failed", violations);
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleValidationException(ex);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleContactValidationException() {
        ContactValidationException ex = new ContactValidationException("Invalid contact data");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleContactValidationException(ex);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleContactNotFoundException() {
        ContactNotFoundException ex = new ContactNotFoundException("Contact not found with ID: 123");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleContactNotFoundException(ex);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleContactServiceException() {
        ContactServiceException ex = new ContactServiceException("Service operation failed");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleContactServiceException(ex);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleContactServiceExceptionWithSubtypes() {
        // Test with ContactNotFoundException subtype
        ContactNotFoundException notFoundEx = new ContactNotFoundException("Contact not found");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleContactServiceException(notFoundEx);
        
        assertTrue("Exception handled without error", true);
        
        // Test with ContactValidationException subtype
        ContactValidationException validationEx = new ContactValidationException("Validation failed");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleContactServiceException(validationEx);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleGenericException(ex);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleGenericExceptionWithSpecificTypes() {
        // Test IllegalArgumentException
        IllegalArgumentException illegalArgEx = new IllegalArgumentException("Invalid argument");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleGenericException(illegalArgEx);
        
        assertTrue("Exception handled without error", true);
        
        // Test SecurityException
        SecurityException securityEx = new SecurityException("Access denied");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleGenericException(securityEx);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleTimeoutException() {
        Exception ex = new Exception("Operation timed out");
        
        // Should handle gracefully without throwing exception
        exceptionHandler.handleTimeoutException(ex);
        
        assertTrue("Exception handled without error", true);
    }

    @Test
    public void testHandleExceptionsWhenFacesContextIsNull() {
        // Test behavior when FacesContext is null (non-web context)
        // Should not throw exception
        exceptionHandler.handleGenericException(new Exception("Test"));
        exceptionHandler.handleDatabaseException(new PersistenceException("Test"));
        exceptionHandler.handleContactServiceException(new ContactServiceException("Test"));
        
        // No verification needed - just ensuring no exceptions are thrown
        assertTrue("All exceptions handled without error", true);
    }

    @Test
    public void testHasErrorMessagesWhenFacesContextIsNull() {
        // Test error message detection when FacesContext is null
        boolean hasErrors = exceptionHandler.hasErrorMessages();
        
        assertFalse("Should return false when FacesContext is null", hasErrors);
    }

    @Test
    public void testClearMessagesWhenFacesContextIsNull() {
        // Test message clearing when FacesContext is null
        // Should not throw exception
        exceptionHandler.clearMessages();
        
        assertTrue("Clear messages handled without error", true);
    }
}