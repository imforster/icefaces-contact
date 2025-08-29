package com.phonebook.repository;

import com.phonebook.entity.Contact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContactRepositoryImpl.
 * Tests CRUD operations and search functionality using mocked EntityManager.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContactRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Contact> typedQuery;

    @InjectMocks
    private ContactRepositoryImpl contactRepository;

    private Contact testContact1;
    private Contact testContact2;
    private Contact testContact3;

    @Before
    public void setUp() {
        testContact1 = new Contact("John Doe", "123-456-7890", "john@example.com");
        testContact1.setId(1L);

        testContact2 = new Contact("Jane Smith", "987-654-3210", "jane@example.com");
        testContact2.setId(2L);

        testContact3 = new Contact("Bob Johnson", "555-123-4567");
        testContact3.setId(3L);
    }

    @Test
    public void testFindAll_ReturnsAllContacts() {
        // Arrange
        List<Contact> expectedContacts = Arrays.asList(testContact1, testContact2, testContact3);
        when(entityManager.createQuery(anyString(), eq(Contact.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedContacts);

        // Act
        List<Contact> actualContacts = contactRepository.findAll();

        // Assert
        assertNotNull("Contact list should not be null", actualContacts);
        assertEquals("Should return all contacts", 3, actualContacts.size());
        assertEquals("Should return expected contacts", expectedContacts, actualContacts);
        
        verify(entityManager).createQuery("SELECT c FROM Contact c ORDER BY c.name ASC", Contact.class);
        verify(typedQuery).getResultList();
    }

    @Test
    public void testFindAll_ReturnsEmptyList_WhenNoContacts() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Contact.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // Act
        List<Contact> actualContacts = contactRepository.findAll();

        // Assert
        assertNotNull("Contact list should not be null", actualContacts);
        assertTrue("Should return empty list when no contacts", actualContacts.isEmpty());
    }

    @Test
    public void testFindAll_ThrowsRuntimeException_OnDatabaseError() {
        // Arrange
        when(entityManager.createQuery(anyString(), eq(Contact.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        try {
            contactRepository.findAll();
            fail("Should throw RuntimeException on database error");
        } catch (RuntimeException e) {
            assertEquals("Failed to retrieve contacts", e.getMessage());
        }
    }

    @Test
    public void testFindById_ReturnsContact_WhenExists() {
        // Arrange
        Long contactId = 1L;
        when(entityManager.find(Contact.class, contactId)).thenReturn(testContact1);

        // Act
        Contact actualContact = contactRepository.findById(contactId);

        // Assert
        assertNotNull("Should return contact when it exists", actualContact);
        assertEquals("Should return correct contact", testContact1, actualContact);
        verify(entityManager).find(Contact.class, contactId);
    }

    @Test
    public void testFindById_ReturnsNull_WhenNotExists() {
        // Arrange
        Long contactId = 999L;
        when(entityManager.find(Contact.class, contactId)).thenReturn(null);

        // Act
        Contact actualContact = contactRepository.findById(contactId);

        // Assert
        assertNull("Should return null when contact doesn't exist", actualContact);
        verify(entityManager).find(Contact.class, contactId);
    }

    @Test
    public void testFindById_ReturnsNull_WhenIdIsNull() {
        // Act
        Contact actualContact = contactRepository.findById(null);

        // Assert
        assertNull("Should return null when ID is null", actualContact);
        verify(entityManager, never()).find(any(), any());
    }

    @Test
    public void testFindById_ThrowsRuntimeException_OnDatabaseError() {
        // Arrange
        Long contactId = 1L;
        when(entityManager.find(Contact.class, contactId))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        try {
            contactRepository.findById(contactId);
            fail("Should throw RuntimeException on database error");
        } catch (RuntimeException e) {
            assertEquals("Failed to find contact with ID: 1", e.getMessage());
        }
    }

    @Test
    public void testSave_PersistsNewContact_WhenIdIsNull() {
        // Arrange
        Contact newContact = new Contact("New Contact", "111-222-3333");
        
        // Act
        Contact savedContact = contactRepository.save(newContact);

        // Assert
        assertNotNull("Should return saved contact", savedContact);
        assertEquals("Should return same contact", newContact, savedContact);
        verify(entityManager).persist(newContact);
        verify(entityManager, never()).merge(any());
    }

    @Test
    public void testSave_MergesExistingContact_WhenIdExists() {
        // Arrange
        Contact existingContact = new Contact("Updated Contact", "111-222-3333");
        existingContact.setId(1L);
        when(entityManager.merge(existingContact)).thenReturn(existingContact);

        // Act
        Contact savedContact = contactRepository.save(existingContact);

        // Assert
        assertNotNull("Should return saved contact", savedContact);
        assertEquals("Should return merged contact", existingContact, savedContact);
        verify(entityManager).merge(existingContact);
        verify(entityManager, never()).persist(any());
    }

    @Test
    public void testSave_ThrowsIllegalArgumentException_WhenContactIsNull() {
        // Act & Assert
        try {
            contactRepository.save(null);
            fail("Should throw IllegalArgumentException when contact is null");
        } catch (IllegalArgumentException e) {
            assertEquals("Contact cannot be null", e.getMessage());
        }
        
        verify(entityManager, never()).persist(any());
        verify(entityManager, never()).merge(any());
    }

    @Test
    public void testSave_ThrowsRuntimeException_OnDatabaseError() {
        // Arrange
        Contact newContact = new Contact("New Contact", "111-222-3333");
        doThrow(new RuntimeException("Database error")).when(entityManager).persist(newContact);

        // Act & Assert
        try {
            contactRepository.save(newContact);
            fail("Should throw RuntimeException on database error");
        } catch (RuntimeException e) {
            assertEquals("Failed to save contact: New Contact", e.getMessage());
        }
    }

    @Test
    public void testDelete_RemovesContact_WhenExists() {
        // Arrange
        Long contactId = 1L;
        when(entityManager.find(Contact.class, contactId)).thenReturn(testContact1);

        // Act
        contactRepository.delete(contactId);

        // Assert
        verify(entityManager).find(Contact.class, contactId);
        verify(entityManager).remove(testContact1);
    }

    @Test
    public void testDelete_ThrowsRuntimeException_WhenContactNotExists() {
        // Arrange
        Long contactId = 999L;
        when(entityManager.find(Contact.class, contactId)).thenReturn(null);

        // Act & Assert
        try {
            contactRepository.delete(contactId);
            fail("Should throw RuntimeException when contact doesn't exist");
        } catch (RuntimeException e) {
            assertEquals("Contact not found with ID: 999", e.getMessage());
        }
        
        verify(entityManager).find(Contact.class, contactId);
        verify(entityManager, never()).remove(any());
    }

    @Test
    public void testDelete_ThrowsIllegalArgumentException_WhenIdIsNull() {
        // Act & Assert
        try {
            contactRepository.delete(null);
            fail("Should throw IllegalArgumentException when ID is null");
        } catch (IllegalArgumentException e) {
            assertEquals("Contact ID cannot be null", e.getMessage());
        }
        
        verify(entityManager, never()).find(any(), any());
        verify(entityManager, never()).remove(any());
    }

    @Test
    public void testDelete_ThrowsRuntimeException_OnDatabaseError() {
        // Arrange
        Long contactId = 1L;
        when(entityManager.find(Contact.class, contactId)).thenReturn(testContact1);
        doThrow(new RuntimeException("Database error")).when(entityManager).remove(testContact1);

        // Act & Assert
        try {
            contactRepository.delete(contactId);
            fail("Should throw RuntimeException on database error");
        } catch (RuntimeException e) {
            assertEquals("Failed to delete contact with ID: 1", e.getMessage());
        }
    }

    @Test
    public void testFindByNameContaining_ReturnsMatchingContacts() {
        // Arrange
        String searchTerm = "John";
        List<Contact> expectedContacts = Arrays.asList(testContact1, testContact3);
        when(entityManager.createQuery(anyString(), eq(Contact.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("searchTerm"), anyString())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedContacts);

        // Act
        List<Contact> actualContacts = contactRepository.findByNameContaining(searchTerm);

        // Assert
        assertNotNull("Contact list should not be null", actualContacts);
        assertEquals("Should return matching contacts", 2, actualContacts.size());
        assertEquals("Should return expected contacts", expectedContacts, actualContacts);
        
        verify(entityManager).createQuery(
            "SELECT c FROM Contact c WHERE LOWER(c.name) LIKE LOWER(:searchTerm) ORDER BY c.name ASC", 
            Contact.class);
        verify(typedQuery).setParameter("searchTerm", "%John%");
        verify(typedQuery).getResultList();
    }

    @Test
    public void testFindByNameContaining_ReturnsEmptyList_WhenNoMatches() {
        // Arrange
        String searchTerm = "NonExistent";
        when(entityManager.createQuery(anyString(), eq(Contact.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("searchTerm"), anyString())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // Act
        List<Contact> actualContacts = contactRepository.findByNameContaining(searchTerm);

        // Assert
        assertNotNull("Contact list should not be null", actualContacts);
        assertTrue("Should return empty list when no matches", actualContacts.isEmpty());
    }

    @Test
    public void testFindByNameContaining_ReturnsAllContacts_WhenSearchTermIsNull() {
        // Arrange
        List<Contact> allContacts = Arrays.asList(testContact1, testContact2, testContact3);
        when(entityManager.createQuery("SELECT c FROM Contact c ORDER BY c.name ASC", Contact.class))
            .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(allContacts);

        // Act
        List<Contact> actualContacts = contactRepository.findByNameContaining(null);

        // Assert
        assertNotNull("Contact list should not be null", actualContacts);
        assertEquals("Should return all contacts when search term is null", 3, actualContacts.size());
        assertEquals("Should return all contacts", allContacts, actualContacts);
    }

    @Test
    public void testFindByNameContaining_ReturnsAllContacts_WhenSearchTermIsEmpty() {
        // Arrange
        List<Contact> allContacts = Arrays.asList(testContact1, testContact2, testContact3);
        when(entityManager.createQuery("SELECT c FROM Contact c ORDER BY c.name ASC", Contact.class))
            .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(allContacts);

        // Act
        List<Contact> actualContacts = contactRepository.findByNameContaining("   ");

        // Assert
        assertNotNull("Contact list should not be null", actualContacts);
        assertEquals("Should return all contacts when search term is empty", 3, actualContacts.size());
        assertEquals("Should return all contacts", allContacts, actualContacts);
    }

    @Test
    public void testFindByNameContaining_IsCaseInsensitive() {
        // Arrange
        String searchTerm = "JOHN";
        List<Contact> expectedContacts = Arrays.asList(testContact1);
        when(entityManager.createQuery(anyString(), eq(Contact.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("searchTerm"), anyString())).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expectedContacts);

        // Act
        List<Contact> actualContacts = contactRepository.findByNameContaining(searchTerm);

        // Assert
        assertNotNull("Contact list should not be null", actualContacts);
        assertEquals("Should return matching contacts regardless of case", 1, actualContacts.size());
        verify(typedQuery).setParameter("searchTerm", "%JOHN%");
    }

    @Test
    public void testFindByNameContaining_ThrowsRuntimeException_OnDatabaseError() {
        // Arrange
        String searchTerm = "John";
        when(entityManager.createQuery(anyString(), eq(Contact.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        try {
            contactRepository.findByNameContaining(searchTerm);
            fail("Should throw RuntimeException on database error");
        } catch (RuntimeException e) {
            assertEquals("Failed to search contacts with term: John", e.getMessage());
        }
    }
}