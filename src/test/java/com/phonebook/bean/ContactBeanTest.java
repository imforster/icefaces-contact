package com.phonebook.bean;

import com.phonebook.entity.Contact;
import com.phonebook.service.ContactService;
import com.phonebook.service.ContactServiceException;
import com.phonebook.service.ContactNotFoundException;
import com.phonebook.service.ContactValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContactBean managed bean.
 * Tests the UI interaction logic and proper delegation to the service layer.
 * Note: FacesMessage testing is omitted as it requires complex JSF context setup.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContactBeanTest {

    @Mock
    private ContactService contactService;

    @InjectMocks
    private ContactBean contactBean;

    private Contact testContact1;
    private Contact testContact2;
    private List<Contact> testContacts;

    @Before
    public void setUp() {
        // Create test contacts
        testContact1 = new Contact("John Doe", "123-456-7890", "john@example.com");
        testContact1.setId(1L);
        
        testContact2 = new Contact("Jane Smith", "987-654-3210", "jane@example.com");
        testContact2.setId(2L);
        
        testContacts = Arrays.asList(testContact1, testContact2);
    }

    @Test
    public void testInit() {
        // Given
        when(contactService.getAllContacts()).thenReturn(testContacts);

        // When
        contactBean.init();

        // Then
        verify(contactService).getAllContacts();
        assertEquals(2, contactBean.getContacts().size());
        assertEquals(2, contactBean.getFilteredContacts().size());
        assertNotNull(contactBean.getNewContact());
        assertNotNull(contactBean.getSelectedContact());
        assertEquals("", contactBean.getSearchTerm());
        assertFalse(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowDeleteConfirmation());
    }

    @Test
    public void testInitWithServiceException() {
        // Given
        when(contactService.getAllContacts()).thenThrow(new ContactServiceException("Database error"));

        // When
        contactBean.init();

        // Then
        verify(contactService).getAllContacts();
        assertTrue(contactBean.getContacts().isEmpty());
        assertTrue(contactBean.getFilteredContacts().isEmpty());
    }

    @Test
    public void testShowAddContactForm() {
        // When
        contactBean.showAddContactForm();

        // Then
        assertTrue(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowDeleteConfirmation());
        assertNotNull(contactBean.getNewContact());
    }

    @Test
    public void testHideAddContactForm() {
        // Given
        contactBean.setShowAddForm(true);

        // When
        contactBean.hideAddContactForm();

        // Then
        assertFalse(contactBean.isShowAddForm());
        assertNotNull(contactBean.getNewContact());
    }

    @Test
    public void testAddContactSuccess() {
        // Given
        Contact newContact = new Contact("Test User", "555-1234", "test@example.com");
        newContact.setId(3L);
        contactBean.setNewContact(newContact);
        
        when(contactService.saveContact(any(Contact.class))).thenReturn(newContact);
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(testContact1, testContact2, newContact));

        // When
        contactBean.addContact();

        // Then
        verify(contactService).saveContact(newContact);
        verify(contactService).getAllContacts(); // Called after add
        assertFalse(contactBean.isShowAddForm());
    }

    @Test
    public void testAddContactWithEmptyName() {
        // Given
        Contact newContact = new Contact("", "555-1234", "test@example.com");
        contactBean.setNewContact(newContact);

        // When
        contactBean.addContact();

        // Then
        verify(contactService, never()).saveContact(any());
    }

    @Test
    public void testAddContactWithEmptyPhoneNumber() {
        // Given
        Contact newContact = new Contact("Test User", "", "test@example.com");
        contactBean.setNewContact(newContact);

        // When
        contactBean.addContact();

        // Then
        verify(contactService, never()).saveContact(any());
    }

    @Test
    public void testAddContactWithValidationException() {
        // Given
        Contact newContact = new Contact("Test User", "555-1234", "test@example.com");
        contactBean.setNewContact(newContact);
        
        when(contactService.saveContact(any(Contact.class)))
            .thenThrow(new ContactValidationException("Validation failed"));

        // When
        contactBean.addContact();

        // Then
        verify(contactService).saveContact(newContact);
    }

    @Test
    public void testShowEditContactForm() {
        // When
        contactBean.showEditContactForm(testContact1);

        // Then
        assertTrue(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowDeleteConfirmation());
        assertEquals(testContact1.getId(), contactBean.getSelectedContact().getId());
        assertEquals(testContact1.getName(), contactBean.getSelectedContact().getName());
    }

    @Test
    public void testShowEditContactFormWithNullContact() {
        // When
        contactBean.showEditContactForm(null);

        // Then
        assertFalse(contactBean.isShowEditForm());
    }

    @Test
    public void testUpdateContactSuccess() {
        // Given
        Contact updatedContact = new Contact("Updated Name", "555-9999", "updated@example.com");
        updatedContact.setId(1L);
        contactBean.setSelectedContact(updatedContact);
        
        when(contactService.updateContact(any(Contact.class))).thenReturn(updatedContact);
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(updatedContact, testContact2));

        // When
        contactBean.updateContact();

        // Then
        verify(contactService).updateContact(updatedContact);
        verify(contactService).getAllContacts(); // Called after update
        assertFalse(contactBean.isShowEditForm());
    }

    @Test
    public void testUpdateContactNotFound() {
        // Given
        Contact updatedContact = new Contact("Updated Name", "555-9999", "updated@example.com");
        updatedContact.setId(1L);
        contactBean.setSelectedContact(updatedContact);
        
        when(contactService.updateContact(any(Contact.class)))
            .thenThrow(new ContactNotFoundException("Contact not found"));
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(testContact2));

        // When
        contactBean.updateContact();

        // Then
        verify(contactService).updateContact(updatedContact);
        verify(contactService).getAllContacts(); // Called after error
        assertFalse(contactBean.isShowEditForm());
    }

    @Test
    public void testShowDeleteConfirmation() {
        // When
        contactBean.showDeleteConfirmation(testContact1);

        // Then
        assertTrue(contactBean.isShowDeleteConfirmation());
        assertFalse(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        assertEquals(testContact1, contactBean.getSelectedContact());
    }

    @Test
    public void testDeleteContactSuccess() {
        // Given
        contactBean.setSelectedContact(testContact1);
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(testContact2));

        // When
        contactBean.deleteContact();

        // Then
        verify(contactService).deleteContact(testContact1.getId());
        verify(contactService).getAllContacts(); // Called after delete
        assertFalse(contactBean.isShowDeleteConfirmation());
    }

    @Test
    public void testDeleteContactNotFound() {
        // Given
        contactBean.setSelectedContact(testContact1);
        doThrow(new ContactNotFoundException("Contact not found"))
            .when(contactService).deleteContact(testContact1.getId());
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(testContact2));

        // When
        contactBean.deleteContact();

        // Then
        verify(contactService).deleteContact(testContact1.getId());
        verify(contactService).getAllContacts(); // Called after error
        assertFalse(contactBean.isShowDeleteConfirmation());
    }

    @Test
    public void testSearchContacts() {
        // Given
        contactBean.setSearchTerm("John");
        when(contactService.searchContacts("John")).thenReturn(Collections.singletonList(testContact1));

        // When
        contactBean.searchContacts();

        // Then
        verify(contactService).searchContacts("John");
        assertEquals(1, contactBean.getFilteredContacts().size());
        assertEquals(testContact1, contactBean.getFilteredContacts().get(0));
    }

    @Test
    public void testSearchContactsWithEmptyTerm() {
        // Given
        contactBean.setContacts(testContacts);
        contactBean.setSearchTerm("");

        // When
        contactBean.searchContacts();

        // Then
        verify(contactService, never()).searchContacts(anyString());
        assertEquals(2, contactBean.getFilteredContacts().size());
    }

    @Test
    public void testSearchContactsWithServiceException() {
        // Given
        contactBean.setContacts(testContacts);
        contactBean.setSearchTerm("John");
        when(contactService.searchContacts("John")).thenThrow(new ContactServiceException("Search error"));

        // When
        contactBean.searchContacts();

        // Then
        verify(contactService).searchContacts("John");
        assertEquals(2, contactBean.getFilteredContacts().size()); // Should fallback to all contacts
    }

    @Test
    public void testSearchContactsWithNullTerm() {
        // Given
        contactBean.setContacts(testContacts);
        contactBean.setSearchTerm(null);

        // When
        contactBean.searchContacts();

        // Then
        verify(contactService, never()).searchContacts(anyString());
        assertEquals(2, contactBean.getFilteredContacts().size());
    }

    @Test
    public void testSearchContactsWithWhitespaceTerm() {
        // Given
        contactBean.setContacts(testContacts);
        contactBean.setSearchTerm("   ");

        // When
        contactBean.searchContacts();

        // Then
        verify(contactService, never()).searchContacts(anyString());
        assertEquals(2, contactBean.getFilteredContacts().size());
    }

    @Test
    public void testClearSearch() {
        // Given
        contactBean.setContacts(testContacts);
        contactBean.setSearchTerm("John");

        // When
        contactBean.clearSearch();

        // Then
        assertEquals("", contactBean.getSearchTerm());
        assertEquals(2, contactBean.getFilteredContacts().size());
    }

    @Test
    public void testHasContacts() {
        // Given
        contactBean.setFilteredContacts(testContacts);

        // When & Then
        assertTrue(contactBean.hasContacts());

        // Given empty list
        contactBean.setFilteredContacts(Collections.emptyList());

        // When & Then
        assertFalse(contactBean.hasContacts());
    }

    @Test
    public void testIsSearchActive() {
        // Given
        contactBean.setSearchTerm("John");

        // When & Then
        assertTrue(contactBean.isSearchActive());

        // Given empty search term
        contactBean.setSearchTerm("");

        // When & Then
        assertFalse(contactBean.isSearchActive());
    }

    @Test
    public void testGetContactCount() {
        // Given
        contactBean.setFilteredContacts(testContacts);

        // When & Then
        assertEquals(2, contactBean.getContactCount());

        // Given null list
        contactBean.setFilteredContacts(null);

        // When & Then
        assertEquals(0, contactBean.getContactCount());
    }
}