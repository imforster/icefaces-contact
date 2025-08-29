package com.phonebook.integration;

import com.phonebook.bean.ContactBean;
import com.phonebook.entity.Contact;
import com.phonebook.service.ContactService;
import com.phonebook.service.ContactNotFoundException;
import com.phonebook.service.ContactValidationException;
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
 * Integration-style tests for edit contact workflow.
 * Tests the complete workflow from UI bean through service layer.
 * Uses mocks to simulate the persistence layer for reliable testing.
 */
@RunWith(MockitoJUnitRunner.class)
public class EditContactWorkflowTest {

    @Mock
    private ContactService contactService;

    private ContactBean contactBean;
    private Contact existingContact;

    @Before
    public void setUp() {
        contactBean = new ContactBean();
        contactBean.setContactService(contactService);
        
        // Create an existing contact for editing
        existingContact = new Contact("John Doe", "123-456-7890", "john@example.com");
        existingContact.setId(1L);
        
        // Mock the service to return the existing contact
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(existingContact));
        
        // Initialize the bean
        contactBean.init();
    }

    @Test
    public void testCompleteEditContactWorkflow() {
        // Given - Initial state verification
        assertEquals(1, contactBean.getContacts().size());
        assertFalse(contactBean.isShowEditForm());
        assertNotNull(contactBean.getSelectedContact());

        // When - User clicks "Edit" button for a contact
        contactBean.showEditContactForm(existingContact);

        // Then - Edit form should be displayed with pre-populated data
        assertTrue(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowDeleteConfirmation());
        
        // Verify the selected contact is a copy with the same data
        assertEquals(existingContact.getId(), contactBean.getSelectedContact().getId());
        assertEquals(existingContact.getName(), contactBean.getSelectedContact().getName());
        assertEquals(existingContact.getPhoneNumber(), contactBean.getSelectedContact().getPhoneNumber());
        assertEquals(existingContact.getEmail(), contactBean.getSelectedContact().getEmail());
        
        // Verify it's a different object (copy, not reference)
        assertNotSame(existingContact, contactBean.getSelectedContact());

        // Given - User modifies contact details
        Contact selectedContact = contactBean.getSelectedContact();
        selectedContact.setName("John Smith");
        selectedContact.setPhoneNumber("987-654-3210");
        selectedContact.setEmail("johnsmith@example.com");

        // Mock service to return the updated contact
        Contact updatedContact = new Contact("John Smith", "987-654-3210", "johnsmith@example.com");
        updatedContact.setId(1L);
        when(contactService.updateContact(any(Contact.class))).thenReturn(updatedContact);
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(updatedContact));

        // When - User submits the form
        contactBean.updateContact();

        // Then - Contact should be updated and form hidden
        verify(contactService).updateContact(any(Contact.class));
        verify(contactService, times(2)).getAllContacts(); // Once in init, once after update
        assertFalse(contactBean.isShowEditForm());
        assertEquals(1, contactBean.getContacts().size());
        assertEquals("John Smith", contactBean.getContacts().get(0).getName());
        assertEquals("987-654-3210", contactBean.getContacts().get(0).getPhoneNumber());
        assertEquals("johnsmith@example.com", contactBean.getContacts().get(0).getEmail());
    }

    @Test
    public void testEditContactWithValidationErrors() {
        // Given - Show edit form
        contactBean.showEditContactForm(existingContact);
        assertTrue(contactBean.isShowEditForm());

        // When - User submits form with empty name
        Contact selectedContact = contactBean.getSelectedContact();
        selectedContact.setName(""); // Invalid - empty name
        selectedContact.setPhoneNumber("987-654-3210");
        selectedContact.setEmail("john@example.com");

        contactBean.updateContact();

        // Then - Form should still be visible and no service call made
        assertTrue(contactBean.isShowEditForm());
        verify(contactService, never()).updateContact(any());

        // When - User submits form with empty phone
        selectedContact.setName("John Doe");
        selectedContact.setPhoneNumber(""); // Invalid - empty phone
        selectedContact.setEmail("john@example.com");

        contactBean.updateContact();

        // Then - Form should still be visible and no service call made
        assertTrue(contactBean.isShowEditForm());
        verify(contactService, never()).updateContact(any());
    }

    @Test
    public void testEditContactWithOptionalEmail() {
        // Given - Show edit form and modify to remove email
        contactBean.showEditContactForm(existingContact);
        Contact selectedContact = contactBean.getSelectedContact();
        selectedContact.setName("John Doe");
        selectedContact.setPhoneNumber("123-456-7890");
        selectedContact.setEmail(null); // Remove email

        // Mock service response
        Contact updatedContact = new Contact("John Doe", "123-456-7890", null);
        updatedContact.setId(1L);
        when(contactService.updateContact(any(Contact.class))).thenReturn(updatedContact);
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(updatedContact));

        // When - User submits the form
        contactBean.updateContact();

        // Then - Contact should be updated successfully
        verify(contactService).updateContact(any(Contact.class));
        assertFalse(contactBean.isShowEditForm());
        assertEquals(1, contactBean.getContacts().size());
        assertEquals("John Doe", contactBean.getContacts().get(0).getName());
        assertNull(contactBean.getContacts().get(0).getEmail());
    }

    @Test
    public void testEditContactNotFound() {
        // Given - Show edit form and modify contact
        contactBean.showEditContactForm(existingContact);
        Contact selectedContact = contactBean.getSelectedContact();
        selectedContact.setName("Updated Name");
        selectedContact.setPhoneNumber("555-9999");

        // Mock service to throw ContactNotFoundException
        when(contactService.updateContact(any(Contact.class)))
            .thenThrow(new ContactNotFoundException("Contact not found"));
        when(contactService.getAllContacts()).thenReturn(Collections.emptyList());

        // When - User submits the form
        contactBean.updateContact();

        // Then - Form should be hidden and contacts refreshed
        verify(contactService).updateContact(any(Contact.class));
        verify(contactService, times(2)).getAllContacts(); // Once in init, once after error
        assertFalse(contactBean.isShowEditForm());
        assertEquals(0, contactBean.getContacts().size());
    }

    @Test
    public void testEditContactWithServiceValidationException() {
        // Given - Show edit form and modify contact
        contactBean.showEditContactForm(existingContact);
        Contact selectedContact = contactBean.getSelectedContact();
        selectedContact.setName("Updated Name");
        selectedContact.setPhoneNumber("invalid-phone");

        // Mock service to throw validation exception
        when(contactService.updateContact(any(Contact.class)))
            .thenThrow(new ContactValidationException("Invalid phone number format"));

        // When - User submits the form
        contactBean.updateContact();

        // Then - Form should still be visible for correction
        verify(contactService).updateContact(any(Contact.class));
        assertTrue(contactBean.isShowEditForm());
        // Contacts list should not be refreshed on validation error
        verify(contactService, times(1)).getAllContacts(); // Only the initial call
    }

    @Test
    public void testCancelEditContactForm() {
        // Given - Show edit form and modify some data
        contactBean.showEditContactForm(existingContact);
        Contact selectedContact = contactBean.getSelectedContact();
        selectedContact.setName("Modified Name");
        selectedContact.setPhoneNumber("555-1234");

        // When - User cancels the form
        contactBean.hideEditContactForm();

        // Then - Form should be hidden and no contact updated
        assertFalse(contactBean.isShowEditForm());
        verify(contactService, never()).updateContact(any());
        assertEquals(1, contactBean.getContacts().size());
        // Original contact should remain unchanged
        assertEquals("John Doe", contactBean.getContacts().get(0).getName());
        assertEquals("123-456-7890", contactBean.getContacts().get(0).getPhoneNumber());
    }

    @Test
    public void testEditContactFormStateManagement() {
        // Given - Set other forms to visible
        contactBean.setShowAddForm(true);
        contactBean.setShowDeleteConfirmation(true);

        // When - Show edit form
        contactBean.showEditContactForm(existingContact);

        // Then - Only edit form should be visible
        assertTrue(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowDeleteConfirmation());
    }

    @Test
    public void testEditContactWithNullContact() {
        // When - Try to edit null contact
        contactBean.showEditContactForm(null);

        // Then - Edit form should not be shown
        assertFalse(contactBean.isShowEditForm());
        verify(contactService, never()).updateContact(any());
    }

    @Test
    public void testSearchFilterMaintainedAfterEdit() {
        // Given - Set up multiple contacts and search
        Contact contact1 = new Contact("John Doe", "111-1111", "john@example.com");
        contact1.setId(1L);
        Contact contact2 = new Contact("Jane Smith", "222-2222", "jane@example.com");
        contact2.setId(2L);
        
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(contact1, contact2));
        when(contactService.searchContacts("John")).thenReturn(Collections.singletonList(contact1));
        
        contactBean.init();
        contactBean.setSearchTerm("John");
        contactBean.searchContacts();
        assertEquals(1, contactBean.getFilteredContacts().size());

        // When - Edit the contact that matches search
        contactBean.showEditContactForm(contact1);
        Contact selectedContact = contactBean.getSelectedContact();
        selectedContact.setName("John Updated");
        selectedContact.setPhoneNumber("333-3333");
        
        // Mock service to return updated contact
        Contact updatedContact = new Contact("John Updated", "333-3333", "john@example.com");
        updatedContact.setId(1L);
        when(contactService.updateContact(any(Contact.class))).thenReturn(updatedContact);
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(updatedContact, contact2));
        when(contactService.searchContacts("John")).thenReturn(Collections.singletonList(updatedContact));
        
        contactBean.updateContact();

        // Then - Search should be maintained and show updated contact
        assertEquals("John", contactBean.getSearchTerm());
        assertEquals(1, contactBean.getFilteredContacts().size());
        assertEquals("John Updated", contactBean.getFilteredContacts().get(0).getName());
    }

    @Test
    public void testSelectedContactObjectReset() {
        // Given - Edit a contact
        contactBean.showEditContactForm(existingContact);
        Contact selectedContact = contactBean.getSelectedContact();
        selectedContact.setName("Modified Name");
        selectedContact.setPhoneNumber("555-1234");

        // Mock successful update
        Contact updatedContact = new Contact("Modified Name", "555-1234", "john@example.com");
        updatedContact.setId(1L);
        when(contactService.updateContact(any(Contact.class))).thenReturn(updatedContact);
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(updatedContact));

        // When - Submit form
        contactBean.updateContact();

        // Then - Selected contact object should be reset
        assertFalse(contactBean.isShowEditForm());
        assertNotNull(contactBean.getSelectedContact());
        // The selected contact should be a fresh instance
        assertTrue(contactBean.getSelectedContact().getName() == null || 
                  contactBean.getSelectedContact().getName().isEmpty());
        assertNull(contactBean.getSelectedContact().getId());
    }

    @Test
    public void testEditContactPreservesOriginalOnCancel() {
        // Given - Edit a contact and modify values
        contactBean.showEditContactForm(existingContact);
        Contact selectedContact = contactBean.getSelectedContact();
        String originalName = selectedContact.getName();
        String originalPhone = selectedContact.getPhoneNumber();
        String originalEmail = selectedContact.getEmail();
        
        // Modify the selected contact
        selectedContact.setName("Changed Name");
        selectedContact.setPhoneNumber("999-9999");
        selectedContact.setEmail("changed@example.com");

        // When - Cancel the edit
        contactBean.hideEditContactForm();

        // Then - Original contact in the list should be unchanged
        assertEquals(1, contactBean.getContacts().size());
        Contact contactInList = contactBean.getContacts().get(0);
        assertEquals(originalName, contactInList.getName());
        assertEquals(originalPhone, contactInList.getPhoneNumber());
        assertEquals(originalEmail, contactInList.getEmail());
        
        // And no service call should have been made
        verify(contactService, never()).updateContact(any());
    }
}