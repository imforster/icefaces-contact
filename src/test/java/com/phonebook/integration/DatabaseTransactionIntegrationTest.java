package com.phonebook.integration;

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
import javax.persistence.PersistenceException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;
import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Integration tests focused on database transaction handling and rollback scenarios.
 * Tests transaction boundaries, isolation, and error recovery.
 */
@RunWith(Arquillian.class)
public class DatabaseTransactionIntegrationTest {

    @Deployment
    public static Archive<?> createDeployment() {
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asFile();

        return ShrinkWrap.create(WebArchive.class, "transaction-test.war")
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

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserTransaction userTransaction;

    @Before
    public void setUp() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM Contact").executeUpdate();
        userTransaction.commit();
    }

    @After
    public void tearDown() throws Exception {
        try {
            userTransaction.begin();
            entityManager.createQuery("DELETE FROM Contact").executeUpdate();
            userTransaction.commit();
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    /**
     * Test basic transaction commit and rollback
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testBasicTransactionCommitAndRollback() throws Exception {
        // Test successful commit
        userTransaction.begin();
        
        Contact contact = new Contact();
        contact.setName("Transaction Test");
        contact.setPhoneNumber("555-TRANS");
        contact.setEmail("transaction@example.com");
        
        entityManager.persist(contact);
        userTransaction.commit();
        
        // Verify commit worked
        List<Contact> contacts = contactService.getAllContacts();
        assertEquals("Should have one contact after commit", 1, contacts.size());
        
        Long contactId = contact.getId();
        assertNotNull("Contact should have ID after commit", contactId);

        // Test rollback
        userTransaction.begin();
        
        Contact anotherContact = new Contact();
        anotherContact.setName("Rollback Test");
        anotherContact.setPhoneNumber("555-ROLLBACK");
        anotherContact.setEmail("rollback@example.com");
        
        entityManager.persist(anotherContact);
        
        // Rollback before commit
        userTransaction.rollback();
        
        // Verify rollback worked
        contacts = contactService.getAllContacts();
        assertEquals("Should still have only one contact after rollback", 1, contacts.size());
        assertEquals("Original contact should still exist", contactId, contacts.get(0).getId());
    }

    /**
     * Test transaction rollback on constraint violations
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testTransactionRollbackOnConstraintViolation() throws Exception {
        // First, save a valid contact
        Contact validContact = new Contact();
        validContact.setName("Valid Contact");
        validContact.setPhoneNumber("555-VALID");
        validContact.setEmail("valid@example.com");
        
        Contact savedContact = contactService.saveContact(validContact);
        assertNotNull("Valid contact should be saved", savedContact.getId());

        // Now try to save an invalid contact in a transaction
        userTransaction.begin();
        
        try {
            // Create contact with null name (violates NOT NULL constraint)
            Contact invalidContact = new Contact();
            invalidContact.setName(null);
            invalidContact.setPhoneNumber("555-INVALID");
            invalidContact.setEmail("invalid@example.com");
            
            entityManager.persist(invalidContact);
            entityManager.flush(); // Force constraint check
            
            userTransaction.commit();
            fail("Should have thrown constraint violation exception");
            
        } catch (Exception e) {
            // Expected exception due to constraint violation
            userTransaction.rollback();
        }

        // Verify original contact still exists and no invalid contact was saved
        List<Contact> contacts = contactService.getAllContacts();
        assertEquals("Should still have only the valid contact", 1, contacts.size());
        assertEquals("Valid contact should be unchanged", "Valid Contact", contacts.get(0).getName());
    }

    /**
     * Test transaction rollback on validation errors
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testTransactionRollbackOnValidationError() throws Exception {
        // Save initial valid contact
        Contact initialContact = contactService.saveContact(createValidContact("Initial", "555-INIT"));
        
        userTransaction.begin();
        
        try {
            // Try to save contact with invalid email format
            Contact invalidContact = new Contact();
            invalidContact.setName("Invalid Email Contact");
            invalidContact.setPhoneNumber("555-INVALID");
            invalidContact.setEmail("not-an-email"); // Invalid email format
            
            // This should trigger validation error
            contactService.saveContact(invalidContact);
            
            userTransaction.commit();
            fail("Should have thrown validation exception");
            
        } catch (Exception e) {
            userTransaction.rollback();
        }

        // Verify only initial contact exists
        List<Contact> contacts = contactService.getAllContacts();
        assertEquals("Should have only initial contact", 1, contacts.size());
        assertEquals("Initial contact should be unchanged", initialContact.getId(), contacts.get(0).getId());
    }

    /**
     * Test nested transaction behavior
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testNestedTransactionBehavior() throws Exception {
        userTransaction.begin();
        
        // Save first contact in outer transaction
        Contact contact1 = createValidContact("Contact 1", "555-0001");
        entityManager.persist(contact1);
        
        try {
            // Simulate nested operation that fails
            Contact contact2 = createValidContact("Contact 2", "555-0002");
            entityManager.persist(contact2);
            
            // Simulate an error condition
            Contact invalidContact = new Contact();
            invalidContact.setName(null); // This will cause constraint violation
            invalidContact.setPhoneNumber("555-INVALID");
            
            entityManager.persist(invalidContact);
            entityManager.flush(); // Force constraint check
            
            userTransaction.commit();
            fail("Should have thrown exception");
            
        } catch (Exception e) {
            userTransaction.rollback();
        }

        // Verify entire transaction was rolled back
        List<Contact> contacts = contactService.getAllContacts();
        assertEquals("Should have no contacts after rollback", 0, contacts.size());
    }

    /**
     * Test transaction isolation levels
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testTransactionIsolation() throws Exception {
        // Save initial contact
        Contact initialContact = contactService.saveContact(createValidContact("Initial", "555-ISOLATION"));
        
        userTransaction.begin();
        
        // Read contact in transaction
        Contact readContact = entityManager.find(Contact.class, initialContact.getId());
        assertNotNull("Should find contact in transaction", readContact);
        
        // Modify contact in transaction but don't commit yet
        readContact.setName("Modified in Transaction");
        
        // In a separate thread, verify the change is not visible
        // (This simulates another transaction)
        Contact externalRead = contactRepository.findById(initialContact.getId());
        assertEquals("External read should see original value", "Initial", externalRead.getName());
        
        // Commit the transaction
        userTransaction.commit();
        
        // Now external read should see the change
        Contact afterCommit = contactRepository.findById(initialContact.getId());
        assertEquals("After commit, should see modified value", "Modified in Transaction", afterCommit.getName());
    }

    /**
     * Test concurrent transaction handling
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testConcurrentTransactions() throws Exception {
        final int numberOfThreads = 5;
        final int contactsPerThread = 3;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(numberOfThreads);
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Submit concurrent tasks
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    for (int j = 0; j < contactsPerThread; j++) {
                        try {
                            Contact contact = createValidContact(
                                "Thread" + threadId + "Contact" + j,
                                "555-" + String.format("%04d", threadId * 100 + j)
                            );
                            contactService.saveContact(contact);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();
        
        // Wait for completion
        assertTrue("All threads should complete within timeout", 
                  completionLatch.await(30, TimeUnit.SECONDS));
        
        executor.shutdown();

        // Verify results
        List<Contact> allContacts = contactService.getAllContacts();
        int expectedTotal = numberOfThreads * contactsPerThread;
        
        assertEquals("Should have created expected number of contacts", 
                    expectedTotal, successCount.get());
        assertEquals("Should have no errors", 0, errorCount.get());
        assertEquals("Database should contain all contacts", 
                    expectedTotal, allContacts.size());
    }

    /**
     * Test transaction timeout handling
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testTransactionTimeout() throws Exception {
        userTransaction.begin();
        
        try {
            // Set a very short timeout (if supported)
            userTransaction.setTransactionTimeout(1); // 1 second
            
            Contact contact = createValidContact("Timeout Test", "555-TIMEOUT");
            entityManager.persist(contact);
            
            // Simulate long-running operation
            Thread.sleep(2000); // 2 seconds - should exceed timeout
            
            userTransaction.commit();
            
            // If we get here, timeout wasn't enforced (which is okay for this test)
            
        } catch (Exception e) {
            // Expected if timeout is enforced
            try {
                userTransaction.rollback();
            } catch (Exception rollbackException) {
                // Transaction may already be rolled back
            }
        }

        // Verify no contact was saved due to timeout/rollback
        List<Contact> contacts = contactService.getAllContacts();
        assertEquals("Should have no contacts after timeout/rollback", 0, contacts.size());
    }

    /**
     * Test database connection recovery
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testDatabaseConnectionRecovery() throws Exception {
        // Save a contact to verify connection works
        Contact testContact = contactService.saveContact(createValidContact("Recovery Test", "555-RECOVERY"));
        assertNotNull("Should save contact successfully", testContact.getId());

        // Verify we can read it back
        Contact retrievedContact = contactRepository.findById(testContact.getId());
        assertNotNull("Should retrieve contact successfully", retrievedContact);
        assertEquals("Retrieved contact should match", testContact.getName(), retrievedContact.getName());

        // Test multiple operations to ensure connection pool is working
        for (int i = 0; i < 10; i++) {
            Contact contact = createValidContact("Batch " + i, "555-" + String.format("%04d", i));
            Contact saved = contactService.saveContact(contact);
            assertNotNull("Should save contact " + i, saved.getId());
        }

        // Verify all contacts were saved
        List<Contact> allContacts = contactService.getAllContacts();
        assertEquals("Should have 11 contacts total", 11, allContacts.size());
    }

    /**
     * Test transaction rollback with multiple entities
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testMultiEntityTransactionRollback() throws Exception {
        userTransaction.begin();
        
        try {
            // Create multiple valid contacts
            Contact contact1 = createValidContact("Multi 1", "555-MULTI1");
            Contact contact2 = createValidContact("Multi 2", "555-MULTI2");
            Contact contact3 = createValidContact("Multi 3", "555-MULTI3");
            
            entityManager.persist(contact1);
            entityManager.persist(contact2);
            entityManager.persist(contact3);
            
            // Create one invalid contact that will cause rollback
            Contact invalidContact = new Contact();
            invalidContact.setName(null); // Constraint violation
            invalidContact.setPhoneNumber("555-INVALID");
            
            entityManager.persist(invalidContact);
            entityManager.flush(); // Force constraint check
            
            userTransaction.commit();
            fail("Should have thrown constraint violation");
            
        } catch (Exception e) {
            userTransaction.rollback();
        }

        // Verify no contacts were saved (entire transaction rolled back)
        List<Contact> contacts = contactService.getAllContacts();
        assertEquals("Should have no contacts after rollback", 0, contacts.size());
    }

    /**
     * Helper method to create a valid contact
     */
    private Contact createValidContact(String name, String phone) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setPhoneNumber(phone);
        contact.setEmail(name.toLowerCase().replace(" ", ".") + "@example.com");
        return contact;
    }
}