package com.phonebook.integration;

import com.phonebook.bean.ContactBean;
import com.phonebook.entity.Contact;
import com.phonebook.repository.ContactRepository;
import com.phonebook.repository.ContactRepositoryImpl;
import com.phonebook.service.ContactService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for add contact functionality.
 * Tests the complete workflow from UI bean to database persistence using JPA directly.
 */
public class AddContactIntegrationTest {

    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private ContactRepository contactRepository;
    private ContactService contactService;
    private ContactBean contactBean;

    @Before
    public void setUp() {
        // Set up JPA for testing
        entityManagerFactory = Persistence.createEntityManagerFactory("phonebook-test");
        entityManager = entityManagerFactory.createEntityManager();
        
        // Set up the dependency chain
        contactRepository = new ContactRepositoryImpl();
        ((ContactRepositoryImpl) contactRepository).setEntityManager(entityManager);
        
        contactService = new ContactService();
        contactService.setContactRepository(contactRepository);
        
        contactBean = new ContactBean();
        contactBean.setContactService(contactService);
        
        // Clean up database before each test
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.createQuery("DELETE FROM Contact").executeUpdate();
        transaction.commit();
        
        // Initialize the contact bean
        contactBean.init();
    }

    @After
    public void tearDown() {
        if (entityManager != null) {
            entityManager.close();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @Test
    public void testAddContactWorkflow_Success() throws Exception {
        // Given
        Contact newContact = new Contact("John Doe", "123-456-7890", "john@example.com");
        contactBean.setNewContact(newContact);
        
        // Verify initial state
        assertEquals(0, contactBean.getContacts().size());
        assertFalse(contactBean.isShowAddForm());

        // When - Show add form
        contactBean.showAddContactForm();

        // Then - Form should be visible
        assertTrue(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowDeleteConfirmation());

        // When - Add contact (wrap in transaction)
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        contactBean.addContact();
        transaction.commit();

        // Then - Contact should be added and form hidden
        assertFalse(contactBean.isShowAddForm());
        assertEquals(1, contactBean.getContacts().size());
        assertEquals(1, contactBean.getFilteredContacts().size());
        
        Contact addedContact = contactBean.getContacts().get(0);
        assertEquals("John Doe", addedContact.getName());
        assertEquals("123-456-7890", addedContact.getPhoneNumber());
        assertEquals("john@example.com", addedContact.getEmail());
        assertNotNull(addedContact.getId());

        // Verify database persistence
        transaction = entityManager.getTransaction();
        transaction.begin();
        List<Contact> dbContacts = entityManager.createQuery("SELECT c FROM Contact c", Contact.class)
                .getResultList();
        transaction.commit();
        
        assertEquals(1, dbContacts.size());
        assertEquals("John Doe", dbContacts.get(0).getName());
    }

    @Test
    public void testAddContactWorkflow_ValidationError() {
        // Given - Contact with missing required fields
        Contact invalidContact = new Contact("", "", "invalid-email");
        contactBean.setNewContact(invalidContact);
        contactBean.showAddContactForm();

        // When - Try to add invalid contact
        contactBean.addContact();

        // Then - Form should still be visible and no contact added
        assertTrue(contactBean.isShowAddForm());
        assertEquals(0, contactBean.getContacts().size());
        assertEquals(0, contactBean.getFilteredContacts().size());
    }

    @Test
    public void testAddContactWorkflow_WithOptionalEmail() throws Exception {
        // Given - Contact without email
        Contact newContact = new Contact("Jane Smith", "987-654-3210", null);
        contactBean.setNewContact(newContact);
        contactBean.showAddContactForm();

        // When - Add contact (wrap in transaction)
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        contactBean.addContact();
        transaction.commit();

        // Then - Contact should be added successfully
        assertFalse(contactBean.isShowAddForm());
        assertEquals(1, contactBean.getContacts().size());
        
        Contact addedContact = contactBean.getContacts().get(0);
        assertEquals("Jane Smith", addedContact.getName());
        assertEquals("987-654-3210", addedContact.getPhoneNumber());
        assertNull(addedContact.getEmail());
    }

    @Test
    public void testAddContactWorkflow_EmptyEmail() throws Exception {
        // Given - Contact with empty email
        Contact newContact = new Contact("Bob Johnson", "555-1234", "");
        contactBean.setNewContact(newContact);
        contactBean.showAddContactForm();

        // When - Add contact (wrap in transaction)
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        contactBean.addContact();
        transaction.commit();

        // Then - Contact should be added successfully
        assertFalse(contactBean.isShowAddForm());
        assertEquals(1, contactBean.getContacts().size());
        
        Contact addedContact = contactBean.getContacts().get(0);
        assertEquals("Bob Johnson", addedContact.getName());
        assertEquals("555-1234", addedContact.getPhoneNumber());
        assertEquals("", addedContact.getEmail());
    }

    @Test
    public void testAddContactWorkflow_CancelForm() {
        // Given
        Contact newContact = new Contact("Test User", "555-9999", "test@example.com");
        contactBean.setNewContact(newContact);
        contactBean.showAddContactForm();

        // When - Cancel form
        contactBean.hideAddContactForm();

        // Then - Form should be hidden and no contact added
        assertFalse(contactBean.isShowAddForm());
        assertEquals(0, contactBean.getContacts().size());
        assertNotNull(contactBean.getNewContact()); // Should be reset
    }

    @Test
    public void testAddContactWorkflow_MultipleContacts() throws Exception {
        // Given - Add first contact
        Contact contact1 = new Contact("Alice Brown", "111-2222", "alice@example.com");
        contactBean.setNewContact(contact1);
        
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        contactBean.addContact();
        transaction.commit();

        // When - Add second contact
        Contact contact2 = new Contact("Charlie Davis", "333-4444", "charlie@example.com");
        contactBean.setNewContact(contact2);
        contactBean.showAddContactForm();
        
        transaction = entityManager.getTransaction();
        transaction.begin();
        contactBean.addContact();
        transaction.commit();

        // Then - Both contacts should be present
        assertEquals(2, contactBean.getContacts().size());
        assertEquals(2, contactBean.getFilteredContacts().size());
        
        // Verify both contacts are in the list
        List<Contact> contacts = contactBean.getContacts();
        assertTrue(contacts.stream().anyMatch(c -> "Alice Brown".equals(c.getName())));
        assertTrue(contacts.stream().anyMatch(c -> "Charlie Davis".equals(c.getName())));
    }

    @Test
    public void testAddContactWorkflow_FormStateManagement() {
        // Given - Initial state
        assertFalse(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowDeleteConfirmation());

        // When - Show add form
        contactBean.showAddContactForm();

        // Then - Only add form should be visible
        assertTrue(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowDeleteConfirmation());

        // When - Hide add form
        contactBean.hideAddContactForm();

        // Then - No forms should be visible
        assertFalse(contactBean.isShowAddForm());
        assertFalse(contactBean.isShowEditForm());
        assertFalse(contactBean.isShowDeleteConfirmation());
    }

    @Test
    public void testAddContactWorkflow_SearchFilterMaintained() throws Exception {
        // Given - Add a contact and set up search
        Contact contact1 = new Contact("John Smith", "111-1111", "john@example.com");
        contactBean.setNewContact(contact1);
        
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        contactBean.addContact();
        transaction.commit();
        
        contactBean.setSearchTerm("John");
        contactBean.searchContacts();
        assertEquals(1, contactBean.getFilteredContacts().size());

        // When - Add another contact that matches search
        Contact contact2 = new Contact("John Doe", "222-2222", "johndoe@example.com");
        contactBean.setNewContact(contact2);
        
        transaction = entityManager.getTransaction();
        transaction.begin();
        contactBean.addContact();
        transaction.commit();

        // Then - Search filter should be maintained and show both Johns
        assertEquals("John", contactBean.getSearchTerm());
        assertEquals(2, contactBean.getFilteredContacts().size());
        assertTrue(contactBean.getFilteredContacts().stream()
                .allMatch(c -> c.getName().contains("John")));
    }
}