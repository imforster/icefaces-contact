package com.phonebook.integration;

import com.phonebook.bean.ContactBean;
import com.phonebook.entity.Contact;
import com.phonebook.service.ContactService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration-style tests for add contact workflow.
 * Tests the complete workflow from UI bean through service layer.
 * Uses mocks to simulate the persistence layer for reliable testing.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddContactWorkflowTest {

    @Mock
    private ContactService contactService;

    private ContactBean contactBean;

    @Before
    public void setUp() {
        contactBean = new ContactBean();
        contactBean.setContactService(contactService);
        
        // Mock the service to return empty list initially
        when(contactService.getAllContacts()).thenReturn(Collections.emptyList());
        
        // Initialize the bean
        contactBean.init();
    }

    @Test
    public void testCompleteAddContactWorkflow() {
        // Given - Initial state verification
        assertEquals(0, contactBean.getContacts().size());
        assertFalse(contactBean.isShowAddForm());
        assertNotNull(contactBean.getNewContact());

        // When - User clicks "Add Contact" button
        contactBean.showAddContactForm();

        // Then - Add form should be displayed
        assertTrue(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowDeleteConfirmation());

        // Given - User fills in contact details
        Contact newContact = contactBean.getNewContact();
        newContact.setName("John Doe");
        newContact.setPhoneNumber("123-456-7890");
        newContact.setEmail("john@example.com");

        // Mock service to return the saved contact
        Contact savedContact = new Contact("John Doe", "123-456-7890", "john@example.com");
        savedContact.setId(1L);
        when(contactService.saveContact(any(Contact.class))).thenReturn(savedContact);
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(savedContact));

        // When - User submits the form
        contactBean.addContact();

        // Then - Contact should be saved and form hidden
        verify(contactService).saveContact(any(Contact.class));
        verify(contactService, times(2)).getAllContacts(); // Once in init, once after add
        assertFalse(contactBean.isShowAddForm());
        assertEquals(1, contactBean.getContacts().size());
        assertEquals("John Doe", contactBean.getContacts().get(0).getName());
    }

    @Test
    public void testAddContactWithValidationErrors() {
        // Given - Show add form
        contactBean.showAddContactForm();
        assertTrue(contactBean.isShowAddForm());

        // When - User submits form with empty name
        Contact newContact = contactBean.getNewContact();
        newContact.setName(""); // Invalid - empty name
        newContact.setPhoneNumber("123-456-7890");
        newContact.setEmail("john@example.com");

        contactBean.addContact();

        // Then - Form should still be visible and no service call made
        assertTrue(contactBean.isShowAddForm());
        verify(contactService, never()).saveContact(any());

        // When - User submits form with empty phone
        newContact.setName("John Doe");
        newContact.setPhoneNumber(""); // Invalid - empty phone
        newContact.setEmail("john@example.com");

        contactBean.addContact();

        // Then - Form should still be visible and no service call made
        assertTrue(contactBean.isShowAddForm());
        verify(contactService, never()).saveContact(any());
    }

    @Test
    public void testAddContactWithOptionalEmail() {
        // Given - Show add form and fill required fields only
        contactBean.showAddContactForm();
        Contact newContact = contactBean.getNewContact();
        newContact.setName("Jane Smith");
        newContact.setPhoneNumber("987-654-3210");
        newContact.setEmail(null); // Optional field

        // Mock service response
        Contact savedContact = new Contact("Jane Smith", "987-654-3210", null);
        savedContact.setId(2L);
        when(contactService.saveContact(any(Contact.class))).thenReturn(savedContact);
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(savedContact));

        // When - User submits the form
        contactBean.addContact();

        // Then - Contact should be saved successfully
        verify(contactService).saveContact(any(Contact.class));
        assertFalse(contactBean.isShowAddForm());
        assertEquals(1, contactBean.getContacts().size());
        assertEquals("Jane Smith", contactBean.getContacts().get(0).getName());
        assertNull(contactBean.getContacts().get(0).getEmail());
    }

    @Test
    public void testCancelAddContactForm() {
        // Given - Show add form and fill some data
        contactBean.showAddContactForm();
        Contact newContact = contactBean.getNewContact();
        newContact.setName("Test User");
        newContact.setPhoneNumber("555-1234");

        // When - User cancels the form
        contactBean.hideAddContactForm();

        // Then - Form should be hidden and no contact saved
        assertFalse(contactBean.isShowAddForm());
        verify(contactService, never()).saveContact(any());
        assertEquals(0, contactBean.getContacts().size());
    }

    @Test
    public void testFormStateManagement() {
        // Test that showing add form hides other forms
        contactBean.setShowEditForm(true);
        contactBean.setShowDeleteConfirmation(true);

        // When - Show add form
        contactBean.showAddContactForm();

        // Then - Only add form should be visible
        assertTrue(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowDeleteConfirmation());
    }

    @Test
    public void testSearchFilterMaintainedAfterAdd() {
        // Given - Set up initial contact and search
        Contact existingContact = new Contact("John Smith", "111-1111", "john@example.com");
        existingContact.setId(1L);
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(existingContact));
        when(contactService.searchContacts("John")).thenReturn(Collections.singletonList(existingContact));
        
        contactBean.init();
        contactBean.setSearchTerm("John");
        contactBean.searchContacts();
        assertEquals(1, contactBean.getFilteredContacts().size());

        // When - Add another contact that matches search
        Contact newContact = new Contact("John Doe", "222-2222", "johndoe@example.com");
        newContact.setId(2L);
        contactBean.setNewContact(newContact);
        
        // Mock service to return both contacts
        when(contactService.saveContact(any(Contact.class))).thenReturn(newContact);
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(existingContact, newContact));
        when(contactService.searchContacts("John")).thenReturn(Arrays.asList(existingContact, newContact));
        
        contactBean.addContact();

        // Then - Search should be maintained and show both contacts
        assertEquals("John", contactBean.getSearchTerm());
        assertEquals(2, contactBean.getFilteredContacts().size());
        assertTrue(contactBean.getFilteredContacts().stream()
                .allMatch(c -> c.getName().contains("John")));
    }

    @Test
    public void testNewContactObjectReset() {
        // Given - Fill new contact form
        contactBean.showAddContactForm();
        Contact newContact = contactBean.getNewContact();
        newContact.setName("Test User");
        newContact.setPhoneNumber("555-1234");
        newContact.setEmail("test@example.com");

        // Mock successful save
        Contact savedContact = new Contact("Test User", "555-1234", "test@example.com");
        savedContact.setId(3L);
        when(contactService.saveContact(any(Contact.class))).thenReturn(savedContact);
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(savedContact));

        // When - Submit form
        contactBean.addContact();

        // Then - New contact object should be reset
        assertFalse(contactBean.isShowAddForm());
        assertNotNull(contactBean.getNewContact());
        // The new contact should be a fresh instance (name should be null/empty)
        assertTrue(contactBean.getNewContact().getName() == null || 
                  contactBean.getNewContact().getName().isEmpty());
    }
}