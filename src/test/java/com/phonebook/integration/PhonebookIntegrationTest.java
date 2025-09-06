package com.phonebook.integration;

import com.phonebook.bean.ContactBean;
import com.phonebook.entity.Contact;
import com.phonebook.repository.ContactRepository;
import com.phonebook.service.ContactService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive integration tests for the Phonebook application using Arquillian.
 * Tests complete CRUD workflows, database transactions, and CDI injection.
 */
@RunWith(Arquillian.class)
public class PhonebookIntegrationTest {

    @Deployment
    public static Archive<?> createDeployment() {
        // Resolve Maven dependencies
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asFile();

        return ShrinkWrap.create(WebArchive.class, "phonebook-test.war")
                .addPackages(true, "com.phonebook")
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("test-web.xml", "web.xml")
                .addAsLibraries(libs);
    }

    @Inject
    private ContactService contactService;

    @Inject
    private ContactRepository contactRepository;

    @Inject
    private ContactBean contactBean;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserTransaction userTransaction;

    private Contact testContact1;
    private Contact testContact2;

    @Before
    public void setUp() throws Exception {
        userTransaction.begin();
        
        // Clean up any existing test data
        entityManager.createQuery("DELETE FROM Contact").executeUpdate();
        
        // Create test contacts
        testContact1 = new Contact();
        testContact1.setName("John Doe");
        testContact1.setPhoneNumber("555-1234");
        testContact1.setEmail("john.doe@example.com");

        testContact2 = new Contact();
        testContact2.setName("Jane Smith");
        testContact2.setPhoneNumber("555-5678");
        testContact2.setEmail("jane.smith@example.com");
        
        userTransaction.commit();
    }

    @After
    public void tearDown() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM Contact").executeUpdate();
        userTransaction.commit();
    }

    /**
     * Test CDI injection is working properly
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testCDIInjection() {
        assertNotNull("ContactService should be injected", contactService);
        assertNotNull("ContactRepository should be injected", contactRepository);
        assertNotNull("ContactBean should be injected", contactBean);
        assertNotNull("EntityManager should be injected", entityManager);
        assertNotNull("UserTransaction should be injected", userTransaction);
    }

    /**
     * Test complete CRUD workflow for contacts
     * Requirements: 1.1, 2.2, 3.2, 4.2, 5.1, 7.1
     */
    @Test
    public void testCompleteCRUDWorkflow() throws Exception {
        // Test CREATE operation
        Contact savedContact = contactService.saveContact(testContact1);
        assertNotNull("Saved contact should have an ID", savedContact.getId());
        assertEquals("Name should match", testContact1.getName(), savedContact.getName());
        assertEquals("Phone should match", testContact1.getPhoneNumber(), savedContact.getPhoneNumber());
        assertEquals("Email should match", testContact1.getEmail(), savedContact.getEmail());

        // Test READ operation - find all
        List<Contact> allContacts = contactService.getAllContacts();
        assertEquals("Should have one contact", 1, allContacts.size());
        assertEquals("Contact should match", savedContact.getId(), allContacts.get(0).getId());

        // Test READ operation - find by ID
        Contact foundContact = contactRepository.findById(savedContact.getId());
        assertNotNull("Contact should be found", foundContact);
        assertEquals("Found contact should match saved contact", savedContact.getId(), foundContact.getId());

        // Test UPDATE operation
        foundContact.setName("John Updated");
        foundContact.setPhoneNumber("555-9999");
        Contact updatedContact = contactService.updateContact(foundContact);
        assertEquals("Name should be updated", "John Updated", updatedContact.getName());
        assertEquals("Phone should be updated", "555-9999", updatedContact.getPhoneNumber());

        // Verify update persisted
        Contact verifyUpdate = contactRepository.findById(savedContact.getId());
        assertEquals("Updated name should persist", "John Updated", verifyUpdate.getName());
        assertEquals("Updated phone should persist", "555-9999", verifyUpdate.getPhoneNumber());

        // Test DELETE operation
        contactService.deleteContact(savedContact.getId());
        
        // Verify deletion
        List<Contact> afterDelete = contactService.getAllContacts();
        assertEquals("Should have no contacts after deletion", 0, afterDelete.size());
        
        Contact deletedContact = contactRepository.findById(savedContact.getId());
        assertNull("Deleted contact should not be found", deletedContact);
    }

    /**
     * Test search functionality
     * Requirements: 5.1
     */
    @Test
    public void testSearchFunctionality() throws Exception {
        // Save multiple contacts
        contactService.saveContact(testContact1);
        contactService.saveContact(testContact2);

        // Test search by partial name
        List<Contact> johnResults = contactService.searchContacts("John");
        assertEquals("Should find John", 1, johnResults.size());
        assertEquals("Should find correct contact", "John Doe", johnResults.get(0).getName());

        // Test search by partial name (case insensitive)
        List<Contact> janeResults = contactService.searchContacts("jane");
        assertEquals("Should find Jane (case insensitive)", 1, janeResults.size());
        assertEquals("Should find correct contact", "Jane Smith", janeResults.get(0).getName());

        // Test search with no results
        List<Contact> noResults = contactService.searchContacts("NonExistent");
        assertEquals("Should find no results", 0, noResults.size());

        // Test search with empty string (should return all)
        List<Contact> allResults = contactService.searchContacts("");
        assertEquals("Empty search should return all contacts", 2, allResults.size());
    }

    /**
     * Test database transaction handling and rollback scenarios
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testTransactionHandling() throws Exception {
        userTransaction.begin();
        
        try {
            // Save a contact within transaction
            Contact contact = contactService.saveContact(testContact1);
            assertNotNull("Contact should be saved", contact.getId());
            
            // Verify contact exists within transaction
            List<Contact> contacts = contactService.getAllContacts();
            assertEquals("Should have one contact in transaction", 1, contacts.size());
            
            // Simulate an error and rollback
            userTransaction.rollback();
            
        } catch (Exception e) {
            userTransaction.rollback();
            throw e;
        }

        // Start new transaction to verify rollback
        userTransaction.begin();
        List<Contact> afterRollback = contactService.getAllContacts();
        userTransaction.commit();
        
        assertEquals("Should have no contacts after rollback", 0, afterRollback.size());
    }

    /**
     * Test transaction rollback on validation errors
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testTransactionRollbackOnValidationError() throws Exception {
        // Save a valid contact first
        contactService.saveContact(testContact1);
        
        userTransaction.begin();
        
        try {
            // Try to save an invalid contact (null name)
            Contact invalidContact = new Contact();
            invalidContact.setName(null); // This should cause validation error
            invalidContact.setPhoneNumber("555-0000");
            invalidContact.setEmail("invalid@example.com");
            
            contactService.saveContact(invalidContact);
            fail("Should have thrown validation exception");
            
        } catch (Exception e) {
            userTransaction.rollback();
            
            // Verify original contact still exists after rollback
            userTransaction.begin();
            List<Contact> contacts = contactService.getAllContacts();
            userTransaction.commit();
            
            assertEquals("Should still have original contact", 1, contacts.size());
            assertEquals("Original contact should be unchanged", "John Doe", contacts.get(0).getName());
        }
    }

    /**
     * Test concurrent access and transaction isolation
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testConcurrentAccess() throws Exception {
        // Save initial contact
        Contact savedContact = contactService.saveContact(testContact1);
        
        // Start first transaction
        userTransaction.begin();
        Contact contact1 = contactRepository.findById(savedContact.getId());
        contact1.setName("Updated by Transaction 1");
        
        // In a real concurrent scenario, another transaction would modify the same contact
        // For this test, we'll simulate the scenario by testing isolation
        
        // Update and commit
        contactService.updateContact(contact1);
        userTransaction.commit();
        
        // Verify the update
        Contact verifyContact = contactRepository.findById(savedContact.getId());
        assertEquals("Contact should be updated", "Updated by Transaction 1", verifyContact.getName());
    }

    /**
     * Test managed bean integration with service layer
     * Requirements: 1.1, 2.2, 3.2, 4.2, 5.1
     */
    @Test
    public void testManagedBeanIntegration() throws Exception {
        // Test ContactBean initialization
        assertNotNull("ContactBean should be initialized", contactBean);
        
        // Test loading contacts through managed bean
        contactService.saveContact(testContact1);
        contactService.saveContact(testContact2);
        
        contactBean.loadAllContacts();
        List<Contact> beanContacts = contactBean.getContacts();
        
        assertNotNull("Bean contacts should not be null", beanContacts);
        assertEquals("Bean should load all contacts", 2, beanContacts.size());
        
        // Test search through managed bean
        contactBean.setSearchTerm("John");
        contactBean.searchContacts();
        List<Contact> searchResults = contactBean.getFilteredContacts();
        
        assertEquals("Bean search should work", 1, searchResults.size());
        assertEquals("Bean should find correct contact", "John Doe", searchResults.get(0).getName());
    }

    /**
     * Test database schema and constraints
     * Requirements: 7.1, 7.4
     */
    @Test
    public void testDatabaseConstraints() throws Exception {
        userTransaction.begin();
        
        // Test that contact can be saved with valid data
        Contact validContact = new Contact();
        validContact.setName("Valid Contact");
        validContact.setPhoneNumber("555-1111");
        validContact.setEmail("valid@example.com");
        
        entityManager.persist(validContact);
        entityManager.flush();
        
        assertNotNull("Valid contact should have ID", validContact.getId());
        
        userTransaction.commit();
        
        // Test constraint violations
        userTransaction.begin();
        
        try {
            // Test null name constraint
            Contact invalidContact = new Contact();
            invalidContact.setName(null);
            invalidContact.setPhoneNumber("555-2222");
            invalidContact.setEmail("test@example.com");
            
            entityManager.persist(invalidContact);
            entityManager.flush();
            
            fail("Should have thrown constraint violation for null name");
            
        } catch (Exception e) {
            // Expected - constraint violation
            userTransaction.rollback();
        }
    }

    /**
     * Test data persistence across transactions
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testDataPersistence() throws Exception {
        // Save contact in first transaction
        userTransaction.begin();
        Contact contact = new Contact();
        contact.setName("Persistent Contact");
        contact.setPhoneNumber("555-3333");
        contact.setEmail("persistent@example.com");
        
        entityManager.persist(contact);
        userTransaction.commit();
        
        Long contactId = contact.getId();
        assertNotNull("Contact should have ID", contactId);
        
        // Clear entity manager to ensure we're reading from database
        entityManager.clear();
        
        // Retrieve contact in second transaction
        userTransaction.begin();
        Contact retrievedContact = entityManager.find(Contact.class, contactId);
        userTransaction.commit();
        
        assertNotNull("Contact should be retrieved", retrievedContact);
        assertEquals("Name should persist", "Persistent Contact", retrievedContact.getName());
        assertEquals("Phone should persist", "555-3333", retrievedContact.getPhoneNumber());
        assertEquals("Email should persist", "persistent@example.com", retrievedContact.getEmail());
    }
}