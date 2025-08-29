package com.phonebook.validation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PhoneNumberValidator.
 * Tests various phone number formats and validation scenarios.
 */
public class PhoneNumberValidatorTest {

    private PhoneNumberValidator validator;

    @Mock
    private PhoneNumber phoneNumberAnnotation;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintViolationBuilder violationBuilder;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new PhoneNumberValidator();
        
        // Setup default annotation behavior
        when(phoneNumberAnnotation.allowInternational()).thenReturn(true);
        when(phoneNumberAnnotation.allowExtensions()).thenReturn(false);
        when(phoneNumberAnnotation.minLength()).thenReturn(7);
        when(phoneNumberAnnotation.maxLength()).thenReturn(15);
        
        // Setup context behavior for custom messages
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
        
        validator.initialize(phoneNumberAnnotation);
    }

    @Test
    public void testValidNorthAmericanFormats() {
        // Test various valid North American formats
        assertTrue("(555) 123-4567 should be valid", validator.isValid("(555) 123-4567", context));
        assertTrue("555-123-4567 should be valid", validator.isValid("555-123-4567", context));
        assertTrue("555.123.4567 should be valid", validator.isValid("555.123.4567", context));
        assertTrue("555 123 4567 should be valid", validator.isValid("555 123 4567", context));
        assertTrue("5551234567 should be valid", validator.isValid("5551234567", context));
        assertTrue("1-555-123-4567 should be valid", validator.isValid("1-555-123-4567", context));
        assertTrue("+1-555-123-4567 should be valid", validator.isValid("+1-555-123-4567", context));
    }

    @Test
    public void testValidInternationalFormats() {
        // Test various valid international formats
        assertTrue("+44 20 7946 0958 should be valid", validator.isValid("+44 20 7946 0958", context));
        assertTrue("+33 1 42 86 83 26 should be valid", validator.isValid("+33 1 42 86 83 26", context));
        assertTrue("+49 30 12345678 should be valid", validator.isValid("+49 30 12345678", context));
        assertTrue("+81 3 1234 5678 should be valid", validator.isValid("+81 3 1234 5678", context));
        assertTrue("+86 10 1234 5678 should be valid", validator.isValid("+86 10 1234 5678", context));
    }

    @Test
    public void testInvalidFormats() {
        // Test invalid formats
        assertFalse("123 should be invalid (too short)", validator.isValid("123", context));
        assertFalse("abc-def-ghij should be invalid (letters)", validator.isValid("abc-def-ghij", context));
        assertFalse("555-123-456789012345 should be invalid (too long)", 
                   validator.isValid("555-123-456789012345", context));
        assertFalse("555-123 should be invalid (incomplete)", validator.isValid("555-123", context));
    }

    @Test
    public void testBusinessRuleValidation() {
        // Test business rules - all same digits
        assertFalse("1111111111 should be invalid (all same digits)", 
                   validator.isValid("1111111111", context));
        
        // Test business rules - sequential digits
        assertFalse("1234567890 should be invalid (sequential)", 
                   validator.isValid("1234567890", context));
        
        // Test business rules - invalid area codes
        assertFalse("0551234567 should be invalid (area code starts with 0)", 
                   validator.isValid("0551234567", context));
        assertFalse("1551234567 should be invalid (area code starts with 1)", 
                   validator.isValid("1551234567", context));
        
        // Test business rules - invalid exchange codes
        assertFalse("5550234567 should be invalid (exchange starts with 0)", 
                   validator.isValid("5550234567", context));
    }

    @Test
    public void testNullAndEmptyValues() {
        // Null and empty values should be valid (handled by @NotNull)
        assertTrue("null should be valid", validator.isValid(null, context));
        assertTrue("empty string should be valid", validator.isValid("", context));
        assertTrue("whitespace should be valid", validator.isValid("   ", context));
    }

    @Test
    public void testExtensionsWhenDisabled() {
        // Extensions should be invalid when not allowed
        assertFalse("555-123-4567 x1234 should be invalid when extensions disabled", 
                   validator.isValid("555-123-4567 x1234", context));
    }

    @Test
    public void testExtensionsWhenEnabled() {
        // Setup annotation to allow extensions
        when(phoneNumberAnnotation.allowExtensions()).thenReturn(true);
        validator.initialize(phoneNumberAnnotation);
        
        // Extensions should be valid when allowed
        assertTrue("555-123-4567 x1234 should be valid when extensions enabled", 
                  validator.isValid("555-123-4567 x1234", context));
        assertTrue("555-123-4567 ext 5678 should be valid", 
                  validator.isValid("555-123-4567 ext 5678", context));
        assertTrue("555-123-4567 extension 9012 should be valid", 
                  validator.isValid("555-123-4567 extension 9012", context));
    }

    @Test
    public void testInternationalWhenDisabled() {
        // Setup annotation to disallow international
        when(phoneNumberAnnotation.allowInternational()).thenReturn(false);
        validator.initialize(phoneNumberAnnotation);
        
        // International formats should be invalid when not allowed
        assertFalse("+44 20 7946 0958 should be invalid when international disabled", 
                   validator.isValid("+44 20 7946 0958", context));
    }

    @Test
    public void testCustomLengthConstraints() {
        // Setup custom length constraints
        when(phoneNumberAnnotation.minLength()).thenReturn(10);
        when(phoneNumberAnnotation.maxLength()).thenReturn(12);
        validator.initialize(phoneNumberAnnotation);
        
        // Test length constraints
        assertFalse("5551234 should be invalid (too few digits)", 
                   validator.isValid("555-123-4", context));
        assertTrue("5551234567 should be valid (10 digits)", 
                  validator.isValid("555-123-4567", context));
        assertTrue("15551234567 should be valid (11 digits)", 
                  validator.isValid("1-555-123-4567", context));
        assertFalse("155512345678901 should be invalid (too many digits)", 
                   validator.isValid("1-555-123-4567-8901", context));
    }

    @Test
    public void testCustomMessageGeneration() {
        // Test that custom messages are generated for violations
        validator.isValid("123", context);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    public void testEdgeCases() {
        // Test edge cases
        assertFalse("Phone number with only special characters should be invalid", 
                   validator.isValid("()-.+ ", context));
        assertFalse("Phone number with mixed valid/invalid characters should be invalid", 
                   validator.isValid("555-123-ABCD", context));
        assertTrue("Minimal valid phone number should be valid", 
                  validator.isValid("5551234", context));
    }
}