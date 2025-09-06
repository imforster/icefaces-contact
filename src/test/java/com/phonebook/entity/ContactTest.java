package com.phonebook.entity;

import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for the Contact entity.
 * Tests JPA annotations, Bean Validation, and entity behavior.
 */
public class ContactTest {

    private Validator validator;

    @Before
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testDefaultConstructor() {
        Contact contact = new Contact();
        assertNull(contact.getId());
        assertNull(contact.getName());
        assertNull(contact.getPhoneNumber());
        assertNull(contact.getEmail());
    }

    @Test
    public void testConstructorWithRequiredFields() {
        String name = "John Doe";
        String phoneNumber = "123-456-7890";
        
        Contact contact = new Contact(name, phoneNumber);
        
        assertNull(contact.getId());
        assertEquals(name, contact.getName());
        assertEquals(phoneNumber, contact.getPhoneNumber());
        assertNull(contact.getEmail());
    }

    @Test
    public void testConstructorWithAllFields() {
        String name = "Jane Smith";
        String phoneNumber = "+1-555-123-4567";
        String email = "jane.smith@example.com";
        
        Contact contact = new Contact(name, phoneNumber, email);
        
        assertNull(contact.getId());
        assertEquals(name, contact.getName());
        assertEquals(phoneNumber, contact.getPhoneNumber());
        assertEquals(email, contact.getEmail());
    }

    @Test
    public void testGettersAndSetters() {
        Contact contact = new Contact();
        Long id = 1L;
        String name = "Test User";
        String phoneNumber = "555-0123";
        String email = "test@example.com";

        contact.setId(id);
        contact.setName(name);
        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);

        assertEquals(id, contact.getId());
        assertEquals(name, contact.getName());
        assertEquals(phoneNumber, contact.getPhoneNumber());
        assertEquals(email, contact.getEmail());
    }

    @Test
    public void testValidContact() {
        Contact contact = new Contact("John Doe", "123-456-7890", "john@example.com");
        
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        
        assertTrue("Valid contact should have no validation errors", violations.isEmpty());
    }

    @Test
    public void testValidContactWithoutEmail() {
        Contact contact = new Contact("John Doe", "123-456-7890");
        
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        
        assertTrue("Valid contact without email should have no validation errors", violations.isEmpty());
    }

    @Test
    public void testNameValidation_Null() {
        Contact contact = new Contact(null, "123-456-7890");
        
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        
        assertEquals(1, violations.size());
        ConstraintViolation<Contact> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Name is required", violation.getMessage());
    }

    @Test
    public void testNameValidation_Empty() {
        Contact contact = new Contact("", "123-456-7890");
        
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        
        assertEquals(1, violations.size());
        ConstraintViolation<Contact> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Name must be between 1 and 100 characters", violation.getMessage());
    }

    @Test
    public void testNameValidation_TooLong() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            sb.append("a");
        }
        String longName = sb.toString(); // 101 characters
        Contact contact = new Contact(longName, "123-456-7890");
        
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        
        assertEquals(1, violations.size());
        ConstraintViolation<Contact> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Name must be between 1 and 100 characters", violation.getMessage());
    }

    @Test
    public void testPhoneNumberValidation_Null() {
        Contact contact = new Contact("John Doe", null);
        
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        
        assertEquals(1, violations.size());
        ConstraintViolation<Contact> violation = violations.iterator().next();
        assertEquals("phoneNumber", violation.getPropertyPath().toString());
        assertEquals("Phone number is required", violation.getMessage());
    }

    @Test
    public void testPhoneNumberValidation_ValidFormats() {
        String[] validPhoneNumbers = {
            "123-456-7890",
            "+1-555-123-4567",
            "(555) 123-4567",
            "555 123 4567",
            "5551234567",
            "+15551234567",
            "123 456 7890"
        };

        for (String phoneNumber : validPhoneNumbers) {
            Contact contact = new Contact("John Doe", phoneNumber);
            Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
            assertTrue("Phone number '" + phoneNumber + "' should be valid", violations.isEmpty());
        }
    }

    @Test
    public void testPhoneNumberValidation_InvalidFormats() {
        String[] invalidPhoneNumbers = {
            "123",           // too short
            "abc-def-ghij",  // contains letters
            "123-456-7890-1234-5678", // too long
            "",              // empty
            "123@456#7890"   // invalid characters
        };

        for (String phoneNumber : invalidPhoneNumbers) {
            Contact contact = new Contact("John Doe", phoneNumber);
            Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
            assertFalse("Phone number '" + phoneNumber + "' should be invalid", violations.isEmpty());
        }
    }

    @Test
    public void testEmailValidation_ValidEmails() {
        String[] validEmails = {
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "123@example.com",
            null // email is optional
        };

        for (String email : validEmails) {
            Contact contact = new Contact("John Doe", "123-456-7890", email);
            Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
            assertTrue("Email '" + email + "' should be valid", violations.isEmpty());
        }
    }

    @Test
    public void testEmailValidation_InvalidEmails() {
        String[] invalidEmails = {
            "invalid-email",
            "@example.com",
            "user@",
            "user name@example.com", // space in local part
            "user@example" // missing TLD
        };

        for (String email : invalidEmails) {
            Contact contact = new Contact("John Doe", "123-456-7890", email);
            Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
            assertFalse("Email '" + email + "' should be invalid", violations.isEmpty());
        }
    }

    @Test
    public void testEmailValidation_TooLong() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 95; i++) {
            sb.append("a");
        }
        String longEmail = sb.toString() + "@example.com"; // over 100 chars
        
        Contact contact = new Contact("John Doe", "123-456-7890", longEmail);
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        
        assertFalse("Long email should be invalid", violations.isEmpty());
    }

    @Test
    public void testEquals_SameObject() {
        Contact contact = new Contact("John Doe", "123-456-7890");
        
        assertTrue("Contact should equal itself", contact.equals(contact));
    }

    @Test
    public void testEquals_NullObject() {
        Contact contact = new Contact("John Doe", "123-456-7890");
        
        assertFalse("Contact should not equal null", contact.equals(null));
    }

    @Test
    public void testEquals_DifferentClass() {
        Contact contact = new Contact("John Doe", "123-456-7890");
        String notAContact = "Not a contact";
        
        assertFalse("Contact should not equal different class", contact.equals(notAContact));
    }

    @Test
    public void testEquals_SameNameAndPhone() {
        Contact contact1 = new Contact("John Doe", "123-456-7890", "john@example.com");
        Contact contact2 = new Contact("John Doe", "123-456-7890", "john.doe@different.com");
        
        assertTrue("Contacts with same name and phone should be equal", contact1.equals(contact2));
    }

    @Test
    public void testEquals_DifferentName() {
        Contact contact1 = new Contact("John Doe", "123-456-7890");
        Contact contact2 = new Contact("Jane Doe", "123-456-7890");
        
        assertFalse("Contacts with different names should not be equal", contact1.equals(contact2));
    }

    @Test
    public void testEquals_DifferentPhone() {
        Contact contact1 = new Contact("John Doe", "123-456-7890");
        Contact contact2 = new Contact("John Doe", "987-654-3210");
        
        assertFalse("Contacts with different phone numbers should not be equal", contact1.equals(contact2));
    }

    @Test
    public void testHashCode_Consistency() {
        Contact contact = new Contact("John Doe", "123-456-7890");
        int hashCode1 = contact.hashCode();
        int hashCode2 = contact.hashCode();
        
        assertEquals("HashCode should be consistent", hashCode1, hashCode2);
    }

    @Test
    public void testHashCode_EqualObjects() {
        Contact contact1 = new Contact("John Doe", "123-456-7890", "john@example.com");
        Contact contact2 = new Contact("John Doe", "123-456-7890", "john.doe@different.com");
        
        assertEquals("Equal objects should have equal hash codes", 
                     contact1.hashCode(), contact2.hashCode());
    }

    @Test
    public void testHashCode_DifferentObjects() {
        Contact contact1 = new Contact("John Doe", "123-456-7890");
        Contact contact2 = new Contact("Jane Doe", "987-654-3210");
        
        assertNotEquals("Different objects should have different hash codes", 
                        contact1.hashCode(), contact2.hashCode());
    }

    @Test
    public void testToString() {
        Contact contact = new Contact("John Doe", "123-456-7890", "john@example.com");
        contact.setId(1L);
        
        String toString = contact.toString();
        
        assertTrue("toString should contain id", toString.contains("id=1"));
        assertTrue("toString should contain name", toString.contains("name='John Doe'"));
        assertTrue("toString should contain phoneNumber", toString.contains("phoneNumber='123-456-7890'"));
        assertTrue("toString should contain email", toString.contains("email='john@example.com'"));
    }

    @Test
    public void testToString_WithNullValues() {
        Contact contact = new Contact();
        
        String toString = contact.toString();
        
        assertTrue("toString should handle null values", toString.contains("Contact{"));
        assertTrue("toString should contain null id", toString.contains("id=null"));
        assertTrue("toString should contain null name", toString.contains("name='null'"));
        assertTrue("toString should contain null phoneNumber", toString.contains("phoneNumber='null'"));
        assertTrue("toString should contain null email", toString.contains("email='null'"));
    }

    @Test
    public void testSerializable() {
        Contact contact = new Contact("John Doe", "123-456-7890", "john@example.com");
        
        // Test that Contact implements Serializable
        assertTrue("Contact should implement Serializable", 
                   contact instanceof java.io.Serializable);
    }
}