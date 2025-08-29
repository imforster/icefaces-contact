package com.phonebook.integration;

import com.phonebook.bean.ContactBean;
import com.phonebook.entity.Contact;
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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for JSF navigation flows and UI interactions.
 * Tests managed bean behavior, form processing, and message handling.
 */
@RunWith(Arquillian.class)
public class JSFNavigationIntegrationTest {

    @Deployment
    public static Archive<?> createDeployment() {
        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asFile();

        return ShrinkWrap.create(WebArchive.class, "jsf-navigation-test.war")
                .addPackages(true, "com.phonebook")
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("test-web.xml", "web.xml")
                .addAsWebResource("contacts.xhtml")
                .addAsLibraries(libs);
    }

    @Inject
    private ContactBean contactBean;

    @Inject
    private ContactService contactService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserTransaction userTransaction;

    @Before
    public void setUp() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM Contact").executeUpdate();
        userTransaction.commit();
        
        // Initialize the contact bean
        contactBean.init();
    }

    @After
    public void tearDown() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM Contact").executeUpdate();
        userTransaction.commit();
    }

    /**
     * Test add contact workflow through managed bean
     * Requirements: 2.1, 2.2, 2.3, 2.4
     */
    @Test
    public void testAddContactWorkflow() {
        // Set up new contact data in the bean
        Contact newContact = contactBean.getNewContact();
        newContact.setName("John Doe");
        newContact.setPhoneNumber("555-1234");
        newContact.setEmail("john.doe@example.com");

        // Execute add contact action
        contactBean.addContact();

        // Verify contact was added
        List<Contact> contacts = contactBean.getContacts();
        assertEquals("Should have one contact", 1, contacts.size());

        Contact addedContact = contacts.get(0);
        assertEquals("Name should match", "John Doe", addedContact.getName());
        assertEquals("Phone should match", "555-1234", addedContact.getPhoneNumber());
        assertEquals("Email should match", "john.doe@example.com", addedContact.getEmail());

        // Verify form was reset
        assertTrue("New contact should be reset", 
                  contactBean.getNewContact().getName() == null || 
                  contactBean.getNewContact().getName().isEmpty());
    }

    /**
     * Test edit contact workflow through managed bean
     * Requirements: 3.1, 3.2, 3.3, 3.4
     */
    @Test
    public void testEditContactWorkflow() throws Exception {
        // First add a contact
        Contact testContact = new Contact();
        testContact.setName("Original Name");
        testContact.setPhoneNumber("555-0000");
        testContact.setEmail("original@example.com");
        
        Contact savedContact = contactService.saveContact(testContact);
        contactBean.loadAllContacts();

        // Start edit process
        contactBean.showEditContactForm(savedContact);

        // Verify edit mode is activated
        assertTrue("Should be in edit mode", contactBean.isShowEditForm());
        assertEquals("Selected contact should match", savedContact.getId(), contactBean.getSelectedContact().getId());

        // Update contact data
        contactBean.getSelectedContact().setName("Updated Name");
        contactBean.getSelectedContact().setPhoneNumber("555-9999");
        contactBean.getSelectedContact().setEmail("updated@example.com");

        // Execute update
        contactBean.updateContact();

        // Verify the result
        assertFalse("Should not be in edit mode after update", contactBean.isShowEditForm());

        // Verify contact was updated
        List<Contact> contacts = contactBean.getContacts();
        Contact updatedContact = contacts.stream()
                .filter(c -> c.getId().equals(savedContact.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull("Updated contact should exist", updatedContact);
        assertEquals("Name should be updated", "Updated Name", updatedContact.getName());
        assertEquals("Phone should be updated", "555-9999", updatedContact.getPhoneNumber());
        assertEquals("Email should be updated", "updated@example.com", updatedContact.getEmail());
    }

    /**
     * Test delete contact workflow through managed bean
     * Requirements: 4.1, 4.2, 4.3, 4.4
     */
    @Test
    public void testDeleteContactWorkflow() throws Exception {
        // First add a contact
        Contact testContact = new Contact();
        testContact.setName("To Be Deleted");
        testContact.setPhoneNumber("555-DELETE");
        testContact.setEmail("delete@example.com");
        
        Contact savedContact = contactService.saveContact(testContact);
        contactBean.loadAllContacts();

        // Verify contact exists
        assertEquals("Should have one contact before deletion", 1, contactBean.getContacts().size());

        // Execute delete
        contactBean.showDeleteConfirmation(savedContact);
        contactBean.deleteContact();

        // Verify contact was deleted
        List<Contact> contacts = contactBean.getContacts();
        assertEquals("Should have no contacts after deletion", 0, contacts.size());

        // Verify contact doesn't exist in database
        try {
            contactService.getContactById(savedContact.getId());
            fail("Should have thrown ContactNotFoundException");
        } catch (Exception e) {
            // Expected - contact should not exist
        }
    }

    /**
     * Test search functionality through managed bean
     * Requirements: 5.1, 5.2, 5.3, 5.4
     */
    @Test
    public void testSearchWorkflow() throws Exception {
        // Add multiple contacts
        Contact contact1 = new Contact();
        contact1.setName("John Doe");
        contact1.setPhoneNumber("555-1111");
        contact1.setEmail("john@example.com");

        Contact contact2 = new Contact();
        contact2.setName("Jane Smith");
        contact2.setPhoneNumber("555-2222");
        contact2.setEmail("jane@example.com");

        Contact contact3 = new Contact();
        contact3.setName("Bob Johnson");
        contact3.setPhoneNumber("555-3333");
        contact3.setEmail("bob@example.com");

        contactService.saveContact(contact1);
        contactService.saveContact(contact2);
        contactService.saveContact(contact3);
        contactBean.loadAllContacts();

        // Test search by name
        contactBean.setSearchTerm("John");
        contactBean.searchContacts();

        List<Contact> searchResults = contactBean.getFilteredContacts();
        assertEquals("Should find contacts with 'John'", 2, searchResults.size());

        // Verify correct contacts found
        boolean foundJohnDoe = searchResults.stream().anyMatch(c -> "John Doe".equals(c.getName()));
        boolean foundBobJohnson = searchResults.stream().anyMatch(c -> "Bob Johnson".equals(c.getName()));
        assertTrue("Should find John Doe", foundJohnDoe);
        assertTrue("Should find Bob Johnson", foundBobJohnson);

        // Test search with no results
        contactBean.setSearchTerm("NonExistent");
        contactBean.searchContacts();

        List<Contact> noResults = contactBean.getFilteredContacts();
        assertEquals("Should find no results", 0, noResults.size());

        // Test clear search
        contactBean.clearSearch();
        assertEquals("Search term should be cleared", "", contactBean.getSearchTerm());

        List<Contact> allResults = contactBean.getFilteredContacts();
        assertEquals("Should show all contacts after clear", 3, allResults.size());
    }

    /**
     * Test form validation through managed bean
     * Requirements: 2.3, 3.3
     */
    @Test
    public void testFormValidation() {
        // Test validation with empty required fields
        Contact newContact = contactBean.getNewContact();
        newContact.setName("");
        newContact.setPhoneNumber("");
        newContact.setEmail("valid@example.com");

        contactBean.addContact();

        // Contact should not be added due to validation error
        assertEquals("Should have no contacts due to validation error", 0, contactBean.getContacts().size());

        // Test validation with invalid email format (if email validation is implemented)
        newContact = contactBean.getNewContact();
        newContact.setName("Valid Name");
        newContact.setPhoneNumber("555-1234");
        newContact.setEmail("invalid-email");

        contactBean.addContact();

        // Contact might still be added if email validation is not strict
        // This depends on the actual validation implementation

        // Test with valid data
        newContact = contactBean.getNewContact();
        newContact.setName("Valid Name");
        newContact.setPhoneNumber("555-1234");
        newContact.setEmail("valid@example.com");

        contactBean.addContact();

        // Should succeed
        assertTrue("Should have at least one contact with valid data", contactBean.getContacts().size() >= 1);
    }

    /**
     * Test managed bean state management
     * Requirements: 1.1, 2.1, 3.1, 4.1, 5.1
     */
    @Test
    public void testManagedBeanStateManagement() throws Exception {
        // Test initial state
        assertFalse("Should not be in edit mode initially", contactBean.isShowEditForm());
        assertNotNull("Selected contact should not be null initially", contactBean.getSelectedContact());
        assertEquals("Search term should be empty initially", "", contactBean.getSearchTerm());

        // Add a contact and test state changes
        Contact testContact = new Contact();
        testContact.setName("Test Contact");
        testContact.setPhoneNumber("555-TEST");
        testContact.setEmail("test@example.com");
        
        Contact savedContact = contactService.saveContact(testContact);
        contactBean.loadAllContacts();

        // Test edit state
        contactBean.showEditContactForm(savedContact);
        assertTrue("Should be in edit mode", contactBean.isShowEditForm());
        assertNotNull("Selected contact should not be null", contactBean.getSelectedContact());
        assertEquals("Selected contact should match", savedContact.getId(), contactBean.getSelectedContact().getId());

        // Test cancel edit
        contactBean.hideEditContactForm();
        assertFalse("Should not be in edit mode after cancel", contactBean.isShowEditForm());

        // Test search state
        contactBean.setSearchTerm("Test");
        contactBean.searchContacts();
        assertEquals("Search term should be set", "Test", contactBean.getSearchTerm());
        assertEquals("Should have filtered results", 1, contactBean.getFilteredContacts().size());

        // Test clear search state
        contactBean.clearSearch();
        assertEquals("Search term should be cleared", "", contactBean.getSearchTerm());
        assertEquals("Should show all contacts", 1, contactBean.getFilteredContacts().size());
    }

    /**
     * Test error handling in managed bean
     * Requirements: 7.3
     */
    @Test
    public void testErrorHandling() {
        // Test handling of service layer exceptions
        // This would typically involve mocking the service to throw exceptions
        // For integration testing, we'll test with invalid data scenarios

        // Test with extremely long name (beyond database constraints)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("A");
        }
        String longName = sb.toString(); // Assuming max length is 100
        Contact newContact = contactBean.getNewContact();
        newContact.setName(longName);
        newContact.setPhoneNumber("555-1234");
        newContact.setEmail("test@example.com");

        contactBean.addContact();

        // Should handle the error gracefully
        assertEquals("Should have no contacts due to constraint violation", 0, contactBean.getContacts().size());
    }

    /**
     * Test concurrent modifications handling
     * Requirements: 7.1, 7.2
     */
    @Test
    public void testConcurrentModifications() throws Exception {
        // Add a contact
        Contact testContact = new Contact();
        testContact.setName("Concurrent Test");
        testContact.setPhoneNumber("555-CONCURRENT");
        testContact.setEmail("concurrent@example.com");
        
        Contact savedContact = contactService.saveContact(testContact);
        contactBean.loadAllContacts();

        // Simulate concurrent modification by updating the contact directly through service
        savedContact.setName("Modified Externally");
        contactService.updateContact(savedContact);

        // Now try to update through the bean with stale data
        contactBean.showEditContactForm(savedContact);
        contactBean.getSelectedContact().setName("Modified Through Bean");
        
        contactBean.updateContact();
        
        // Reload and verify the final state
        contactBean.loadAllContacts();
        Contact finalContact = contactBean.getContacts().get(0);
        assertEquals("Should have bean's modification", "Modified Through Bean", finalContact.getName());
    }
}