package com.phonebook.integration;

import com.phonebook.bean.ContactBean;
import com.phonebook.entity.Contact;
import com.phonebook.service.ContactService;
import com.phonebook.service.ContactNotFoundException;
import com.phonebook.service.ContactServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration-style tests for delete contact workflow.
 * Tests the complete workflow from UI bean through service layer.
 * Uses mocks to simulate the persistence layer for reliable testing.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteContactWorkflowTest {

    @Mock
    private ContactService contactService;

    private ContactBean contactBean;
    private Contact existingContact;

    @Before
    public void setUp() {
        contactBean = new ContactBean();
        contactBean.setContactService(contactService);
        
        // Create an existing contact for deletion
        existingContact = new Contact("John Doe", "123-456-7890", "john@example.com");
        existingContact.setId(1L);
        
        // Mock the service to return the existing contact
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(existingContact));
        
        // Initialize the bean
        contactBean.init();
    }

    @Test
    public void testCompleteDeleteContactWorkflow() {
        // Given - Initial state verification
        assertEquals(1, contactBean.getContacts().size());
        assertFalse(contactBean.isShowDeleteConfirmation());
        assertNotNull(contactBean.getSelectedContact());

        // When - User clicks "Delete" button for a contact
        contactBean.showDeleteConfirmation(existingContact);

        // Then - Delete confirmation dialog should be displayed
        assertTrue(contactBean.isShowDeleteConfirmation());
        assertFalse(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        
        // Verify the selected contact is set correctly
        assertEquals(existingContact.getId(), contactBean.getSelectedContact().getId());
        assertEquals(existingContact.getName(), contactBean.getSelectedContact().getName());
        assertEquals(existingContact.getPhoneNumber(), contactBean.getSelectedContact().getPhoneNumber());
        assertEquals(existingContact.getEmail(), contactBean.getSelectedContact().getEmail());

        // Mock service to simulate successful deletion
        doNothing().when(contactService).deleteContact(1L);
        when(contactService.getAllContacts()).thenReturn(Collections.emptyList());

        // When - User confirms deletion
        contactBean.deleteContact();

        // Then - Contact should be deleted and confirmation dialog hidden
        verify(contactService).deleteContact(1L);
        verify(contactService, times(2)).getAllContacts(); // Once in init, once after delete
        assertFalse(contactBean.isShowDeleteConfirmation());
        assertEquals(0, contactBean.getContacts().size());
        assertEquals(0, contactBean.getFilteredContacts().size());
    }

    @Test
    public void testDeleteContactNotFound() {
        // Given - Show delete confirmation
        contactBean.showDeleteConfirmation(existingContact);
        assertTrue(contactBean.isShowDeleteConfirmation());

        // Mock service to throw ContactNotFoundException
        doThrow(new ContactNotFoundException("Contact not found"))
            .when(contactService).deleteContact(1L);
        when(contactService.getAllContacts()).thenReturn(Collections.emptyList());

        // When - User confirms deletion
        contactBean.deleteContact();

        // Then - Confirmation dialog should be hidden and contacts refreshed
        verify(contactService).deleteContact(1L);
        verify(contactService, times(2)).getAllContacts(); // Once in init, once after error
        assertFalse(contactBean.isShowDeleteConfirmation());
        assertEquals(0, contactBean.getContacts().size());
    }

    @Test
    public void testDeleteContactWithServiceException() {
        // Given - Show delete confirmation
        contactBean.showDeleteConfirmation(existingContact);
        assertTrue(contactBean.isShowDeleteConfirmation());

        // Mock service to throw ContactServiceException
        doThrow(new ContactServiceException("Database error"))
            .when(contactService).deleteContact(1L);

        // When - User confirms deletion
        contactBean.deleteContact();

        // Then - Confirmation dialog should remain visible for retry
        verify(contactService).deleteContact(1L);
        assertTrue(contactBean.isShowDeleteConfirmation());
        // Contacts list should not be refreshed on service error
        verify(contactService, times(1)).getAllContacts(); // Only the initial call
        assertEquals(1, contactBean.getContacts().size());
    }

    @Test
    public void testCancelDeleteConfirmation() {
        // Given - Show delete confirmation
        contactBean.showDeleteConfirmation(existingContact);
        assertTrue(contactBean.isShowDeleteConfirmation());
        assertEquals(existingContact, contactBean.getSelectedContact());

        // When - User cancels deletion
        contactBean.hideDeleteConfirmation();

        // Then - Confirmation dialog should be hidden and no contact deleted
        assertFalse(contactBean.isShowDeleteConfirmation());
        verify(contactService, never()).deleteContact(any());
        assertEquals(1, contactBean.getContacts().size());
        // Selected contact should be reset
        assertNotNull(contactBean.getSelectedContact());
        assertTrue(contactBean.getSelectedContact().getName() == null || 
                  contactBean.getSelectedContact().getName().isEmpty());
    }

    @Test
    public void testDeleteContactFormStateManagement() {
        // Given - Set other forms to visible
        contactBean.setShowAddForm(true);
        contactBean.setShowEditForm(true);

        // When - Show delete confirmation
        contactBean.showDeleteConfirmation(existingContact);

        // Then - Only delete confirmation should be visible
        assertTrue(contactBean.isShowDeleteConfirmation());
        assertFalse(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
    }

    @Test
    public void testDeleteContactWithNullContact() {
        // When - Try to delete null contact
        contactBean.showDeleteConfirmation(null);

        // Then - Delete confirmation should not be shown
        assertFalse(contactBean.isShowDeleteConfirmation());
        verify(contactService, never()).deleteContact(any());
    }

    @Test
    public void testDeleteContactWithNullSelectedContact() {
        // Given - Set selected contact to null
        contactBean.setSelectedContact(null);
        contactBean.setShowDeleteConfirmation(true);

        // When - Try to delete with null selected contact
        contactBean.deleteContact();

        // Then - No service call should be made and dialog should remain open
        verify(contactService, never()).deleteContact(any());
        // The dialog remains open because the method returns early without closing it
        assertTrue(contactBean.isShowDeleteConfirmation());
    }

    @Test
    public void testDeleteContactWithNullId() {
        // Given - Create contact without ID
        Contact contactWithoutId = new Contact("Test User", "555-1234", "test@example.com");
        contactWithoutId.setId(null);
        
        contactBean.showDeleteConfirmation(contactWithoutId);
        assertTrue(contactBean.isShowDeleteConfirmation());

        // When - Try to delete contact without ID
        contactBean.deleteContact();

        // Then - No service call should be made and dialog should remain open
        verify(contactService, never()).deleteContact(any());
        // The dialog remains open because the method returns early without closing it
        assertTrue(contactBean.isShowDeleteConfirmation());
    }

    @Test
    public void testSearchFilterMaintainedAfterDelete() {
        // Given - Set up multiple contacts and search
        Contact contact1 = new Contact("John Doe", "111-1111", "john@example.com");
        contact1.setId(1L);
        Contact contact2 = new Contact("Jane Smith", "222-2222", "jane@example.com");
        contact2.setId(2L);
        Contact contact3 = new Contact("John Smith", "333-3333", "johnsmith@example.com");
        contact3.setId(3L);
        
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(contact1, contact2, contact3));
        when(contactService.searchContacts("John")).thenReturn(Arrays.asList(contact1, contact3));
        
        contactBean.init();
        contactBean.setSearchTerm("John");
        contactBean.searchContacts();
        assertEquals(2, contactBean.getFilteredContacts().size());

        // When - Delete one of the contacts that matches search
        contactBean.showDeleteConfirmation(contact1);
        
        // Mock service to simulate successful deletion
        doNothing().when(contactService).deleteContact(1L);
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(contact2, contact3));
        when(contactService.searchContacts("John")).thenReturn(Collections.singletonList(contact3));
        
        contactBean.deleteContact();

        // Then - Search should be maintained and show remaining matching contact
        assertEquals("John", contactBean.getSearchTerm());
        assertEquals(1, contactBean.getFilteredContacts().size());
        assertEquals("John Smith", contactBean.getFilteredContacts().get(0).getName());
    }

    @Test
    public void testDeleteLastContactInSearchResults() {
        // Given - Set up search that returns only one contact
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

        // When - Delete the only contact that matches search
        contactBean.showDeleteConfirmation(contact1);
        
        // Mock service to simulate successful deletion
        doNothing().when(contactService).deleteContact(1L);
        when(contactService.getAllContacts()).thenReturn(Collections.singletonList(contact2));
        when(contactService.searchContacts("John")).thenReturn(Collections.emptyList());
        
        contactBean.deleteContact();

        // Then - Search should be maintained but show no results
        assertEquals("John", contactBean.getSearchTerm());
        assertEquals(0, contactBean.getFilteredContacts().size());
    }

    @Test
    public void testDeleteContactFromMultipleContacts() {
        // Given - Set up multiple contacts
        Contact contact1 = new Contact("John Doe", "111-1111", "john@example.com");
        contact1.setId(1L);
        Contact contact2 = new Contact("Jane Smith", "222-2222", "jane@example.com");
        contact2.setId(2L);
        Contact contact3 = new Contact("Bob Johnson", "333-3333", "bob@example.com");
        contact3.setId(3L);
        
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(contact1, contact2, contact3));
        
        contactBean.init();
        assertEquals(3, contactBean.getContacts().size());

        // When - Delete middle contact
        contactBean.showDeleteConfirmation(contact2);
        
        // Mock service to simulate successful deletion
        doNothing().when(contactService).deleteContact(2L);
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(contact1, contact3));
        
        contactBean.deleteContact();

        // Then - Only the deleted contact should be removed
        verify(contactService).deleteContact(2L);
        assertFalse(contactBean.isShowDeleteConfirmation());
        assertEquals(2, contactBean.getContacts().size());
        assertEquals(2, contactBean.getFilteredContacts().size());
        
        // Verify remaining contacts are correct
        assertTrue(contactBean.getContacts().stream()
                .anyMatch(c -> c.getName().equals("John Doe")));
        assertTrue(contactBean.getContacts().stream()
                .anyMatch(c -> c.getName().equals("Bob Johnson")));
        assertFalse(contactBean.getContacts().stream()
                .anyMatch(c -> c.getName().equals("Jane Smith")));
    }

    @Test
    public void testSelectedContactObjectReset() {
        // Given - Show delete confirmation
        contactBean.showDeleteConfirmation(existingContact);
        assertEquals(existingContact, contactBean.getSelectedContact());

        // Mock successful deletion
        doNothing().when(contactService).deleteContact(1L);
        when(contactService.getAllContacts()).thenReturn(Collections.emptyList());

        // When - Confirm deletion
        contactBean.deleteContact();

        // Then - Selected contact object should be reset
        assertFalse(contactBean.isShowDeleteConfirmation());
        assertNotNull(contactBean.getSelectedContact());
        // The selected contact should be a fresh instance
        assertTrue(contactBean.getSelectedContact().getName() == null || 
                  contactBean.getSelectedContact().getName().isEmpty());
        assertNull(contactBean.getSelectedContact().getId());
    }

    @Test
    public void testDeleteContactPreservesOthersOnCancel() {
        // Given - Multiple contacts
        Contact contact1 = new Contact("John Doe", "111-1111", "john@example.com");
        contact1.setId(1L);
        Contact contact2 = new Contact("Jane Smith", "222-2222", "jane@example.com");
        contact2.setId(2L);
        
        when(contactService.getAllContacts()).thenReturn(Arrays.asList(contact1, contact2));
        contactBean.init();
        
        // Show delete confirmation for one contact
        contactBean.showDeleteConfirmation(contact1);
        assertTrue(contactBean.isShowDeleteConfirmation());

        // When - Cancel the deletion
        contactBean.hideDeleteConfirmation();

        // Then - All contacts should remain unchanged
        assertEquals(2, contactBean.getContacts().size());
        assertTrue(contactBean.getContacts().stream()
                .anyMatch(c -> c.getName().equals("John Doe")));
        assertTrue(contactBean.getContacts().stream()
                .anyMatch(c -> c.getName().equals("Jane Smith")));
        
        // And no service call should have been made
        verify(contactService, never()).deleteContact(any());
    }
}