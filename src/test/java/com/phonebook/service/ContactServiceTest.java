package com.phonebook.service;

import com.phonebook.entity.Contact;
import com.phonebook.repository.ContactRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContactService.
 * Uses Mockito to mock dependencies and test business logic in isolation.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private Validator validator;

    @Mock
    private ConstraintViolation<Contact> constraintViolation;

    @InjectMocks
    private ContactService contactService;

    private Contact testContact;
    private Contact testContact2;

    @Before
    public void setUp() {
        testContact = new Contact("John Doe", "123-456-7890", "john@example.com");
        testContact.setId(1L);

        testContact2 = new Contact("Jane Smith", "098-765-4321", "jane@example.com");
        testContact2.setId(2L);
    }

    @Test
    public void testGetAllContacts_Success() {
        // Given
        List<Contact> expectedContacts = Arrays.asList(testContact, testContact2);
        when(contactRepository.findAll()).thenReturn(expectedContacts);

        // When
        List<Contact> actualContacts = contactService.getAllContacts();

        // Then
        assertEquals(expectedContacts, actualContacts);
        verify(contactRepository).findAll();
    }

    @Test
    public void testGetAllContacts_EmptyList() {
        // Given
        when(contactRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Contact> actualContacts = contactService.getAllContacts();

        // Then
        assertTrue(actualContacts.isEmpty());
        verify(contactRepository).findAll();
    }

    @Test(expected = ContactServiceException.class)
    public void testGetAllContacts_RepositoryException() {
        // Given
        when(contactRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        contactService.getAllContacts();

        // Then - exception expected
    }

    @Test
    public void testGetContactById_Success() {
        // Given
        Long contactId = 1L;
        when(contactRepository.findById(contactId)).thenReturn(testContact);

        // When
        Contact actualContact = contactService.getContactById(contactId);

        // Then
        assertEquals(testContact, actualContact);
        verify(contactRepository).findById(contactId);
    }

    @Test(expected = ContactNotFoundException.class)
    public void testGetContactById_NotFound() {
        // Given
        Long contactId = 999L;
        when(contactRepository.findById(contactId)).thenReturn(null);

        // When
        contactService.getContactById(contactId);

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetContactById_NullId() {
        // When
        contactService.getContactById(null);

        // Then - exception expected
    }

    @Test(expected = ContactServiceException.class)
    public void testGetContactById_RepositoryException() {
        // Given
        Long contactId = 1L;
        when(contactRepository.findById(contactId)).thenThrow(new RuntimeException("Database error"));

        // When
        contactService.getContactById(contactId);

        // Then - exception expected
    }

    @Test
    public void testSaveContact_Success() {
        // Given
        Contact newContact = new Contact("New Contact", "555-1234");
        Contact savedContact = new Contact("New Contact", "555-1234");
        savedContact.setId(3L);

        when(validator.validate(newContact)).thenReturn(Collections.emptySet());
        when(contactRepository.save(newContact)).thenReturn(savedContact);

        // When
        Contact actualContact = contactService.saveContact(newContact);

        // Then
        assertEquals(savedContact, actualContact);
        verify(validator).validate(newContact);
        verify(contactRepository).save(newContact);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveContact_NullContact() {
        // When
        contactService.saveContact(null);

        // Then - exception expected
    }

    @Test(expected = ContactValidationException.class)
    public void testSaveContact_ValidationFailure() {
        // Given
        Contact invalidContact = new Contact("", ""); // Invalid contact
        Set<ConstraintViolation<Contact>> violations = new HashSet<>();
        violations.add(constraintViolation);

        when(constraintViolation.getMessage()).thenReturn("Name is required");
        when(validator.validate(invalidContact)).thenReturn(violations);

        // When
        contactService.saveContact(invalidContact);

        // Then - exception expected
    }

    @Test(expected = ContactServiceException.class)
    public void testSaveContact_RepositoryException() {
        // Given
        when(validator.validate(testContact)).thenReturn(Collections.emptySet());
        when(contactRepository.save(testContact)).thenThrow(new RuntimeException("Database error"));

        // When
        contactService.saveContact(testContact);

        // Then - exception expected
    }

    @Test
    public void testUpdateContact_Success() {
        // Given
        Contact existingContact = new Contact("Existing", "123-456-7890");
        existingContact.setId(1L);

        Contact updatedContact = new Contact("Updated Name", "123-456-7890");
        updatedContact.setId(1L);

        when(validator.validate(updatedContact)).thenReturn(Collections.emptySet());
        when(contactRepository.findById(1L)).thenReturn(existingContact);
        when(contactRepository.save(updatedContact)).thenReturn(updatedContact);

        // When
        Contact actualContact = contactService.updateContact(updatedContact);

        // Then
        assertEquals(updatedContact, actualContact);
        verify(validator).validate(updatedContact);
        verify(contactRepository).findById(1L);
        verify(contactRepository).save(updatedContact);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateContact_NullContact() {
        // When
        contactService.updateContact(null);

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateContact_NullId() {
        // Given
        Contact contactWithoutId = new Contact("Test", "123-456-7890");

        // When
        contactService.updateContact(contactWithoutId);

        // Then - exception expected
    }

    @Test(expected = ContactNotFoundException.class)
    public void testUpdateContact_NotFound() {
        // Given
        Contact contactToUpdate = new Contact("Test", "123-456-7890");
        contactToUpdate.setId(999L);

        when(validator.validate(contactToUpdate)).thenReturn(Collections.emptySet());
        when(contactRepository.findById(999L)).thenReturn(null);

        // When
        contactService.updateContact(contactToUpdate);

        // Then - exception expected
    }

    @Test(expected = ContactValidationException.class)
    public void testUpdateContact_ValidationFailure() {
        // Given
        Contact invalidContact = new Contact("", "");
        invalidContact.setId(1L);
        Set<ConstraintViolation<Contact>> violations = new HashSet<>();
        violations.add(constraintViolation);

        when(constraintViolation.getMessage()).thenReturn("Name is required");
        when(validator.validate(invalidContact)).thenReturn(violations);

        // When
        contactService.updateContact(invalidContact);

        // Then - exception expected
    }

    @Test
    public void testDeleteContact_Success() {
        // Given
        Long contactId = 1L;
        when(contactRepository.findById(contactId)).thenReturn(testContact);

        // When
        contactService.deleteContact(contactId);

        // Then
        verify(contactRepository).findById(contactId);
        verify(contactRepository).delete(contactId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteContact_NullId() {
        // When
        contactService.deleteContact(null);

        // Then - exception expected
    }

    @Test(expected = ContactNotFoundException.class)
    public void testDeleteContact_NotFound() {
        // Given
        Long contactId = 999L;
        when(contactRepository.findById(contactId)).thenReturn(null);

        // When
        contactService.deleteContact(contactId);

        // Then - exception expected
    }

    @Test(expected = ContactServiceException.class)
    public void testDeleteContact_RepositoryException() {
        // Given
        Long contactId = 1L;
        when(contactRepository.findById(contactId)).thenReturn(testContact);
        doThrow(new RuntimeException("Database error")).when(contactRepository).delete(contactId);

        // When
        contactService.deleteContact(contactId);

        // Then - exception expected
    }

    @Test
    public void testSearchContacts_Success() {
        // Given
        String searchTerm = "John";
        List<Contact> expectedContacts = Arrays.asList(testContact);
        when(contactRepository.findByNameContaining(searchTerm)).thenReturn(expectedContacts);

        // When
        List<Contact> actualContacts = contactService.searchContacts(searchTerm);

        // Then
        assertEquals(expectedContacts, actualContacts);
        verify(contactRepository).findByNameContaining(searchTerm);
    }

    @Test
    public void testSearchContacts_EmptyResult() {
        // Given
        String searchTerm = "NonExistent";
        when(contactRepository.findByNameContaining(searchTerm)).thenReturn(Collections.emptyList());

        // When
        List<Contact> actualContacts = contactService.searchContacts(searchTerm);

        // Then
        assertTrue(actualContacts.isEmpty());
        verify(contactRepository).findByNameContaining(searchTerm);
    }

    @Test
    public void testSearchContacts_NullSearchTerm() {
        // Given
        when(contactRepository.findByNameContaining("")).thenReturn(Arrays.asList(testContact, testContact2));

        // When
        List<Contact> actualContacts = contactService.searchContacts(null);

        // Then
        assertEquals(2, actualContacts.size());
        verify(contactRepository).findByNameContaining("");
    }

    @Test
    public void testSearchContacts_WhitespaceSearchTerm() {
        // Given
        String searchTerm = "  John  ";
        List<Contact> expectedContacts = Arrays.asList(testContact);
        when(contactRepository.findByNameContaining("John")).thenReturn(expectedContacts);

        // When
        List<Contact> actualContacts = contactService.searchContacts(searchTerm);

        // Then
        assertEquals(expectedContacts, actualContacts);
        verify(contactRepository).findByNameContaining("John");
    }

    @Test(expected = ContactServiceException.class)
    public void testSearchContacts_RepositoryException() {
        // Given
        String searchTerm = "John";
        when(contactRepository.findByNameContaining(searchTerm)).thenThrow(new RuntimeException("Database error"));

        // When
        contactService.searchContacts(searchTerm);

        // Then - exception expected
    }
}