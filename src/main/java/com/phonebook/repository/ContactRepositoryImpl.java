package com.phonebook.repository;

import com.phonebook.entity.Contact;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JPA implementation of ContactRepository interface.
 * Uses EntityManager for database operations with automatic transaction management.
 */
@Stateless
public class ContactRepositoryImpl implements ContactRepository {

    private static final Logger LOGGER = Logger.getLogger(ContactRepositoryImpl.class.getName());

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Setter for EntityManager - used for testing.
     * @param entityManager the entity manager to set
     */
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Contact> findAll() {
        LOGGER.info("--- Repository: Finding all contacts ---");
        try {
            TypedQuery<Contact> query = entityManager.createQuery(
                "SELECT c FROM Contact c ORDER BY c.name ASC", Contact.class);
            List<Contact> contacts = query.getResultList();
            LOGGER.info("--- Repository: Found " + (contacts != null ? contacts.size() : "NULL") + " contacts ---");
            return contacts;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding all contacts", e);
            throw new RuntimeException("Failed to retrieve contacts", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contact findById(Long id) {
        if (id == null) {
            LOGGER.log(Level.WARNING, "Attempted to find contact with null ID");
            return null;
        }
        
        LOGGER.log(Level.FINE, "Finding contact with ID: {0}", id);
        try {
            Contact contact = entityManager.find(Contact.class, id);
            if (contact != null) {
                LOGGER.log(Level.FINE, "Found contact: {0}", contact.getName());
            } else {
                LOGGER.log(Level.FINE, "No contact found with ID: {0}", id);
            }
            return contact;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding contact with ID: " + id, e);
            throw new RuntimeException("Failed to find contact with ID: " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contact save(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }

        try {
            if (contact.getId() == null) {
                // New contact - persist
                LOGGER.log(Level.FINE, "Persisting new contact: {0}", contact.getName());
                entityManager.persist(contact);
                LOGGER.log(Level.FINE, "Successfully persisted contact with ID: {0}", contact.getId());
            } else {
                // Existing contact - merge
                LOGGER.log(Level.FINE, "Merging existing contact with ID: {0}", contact.getId());
                contact = entityManager.merge(contact);
                LOGGER.log(Level.FINE, "Successfully merged contact: {0}", contact.getName());
            }
            return contact;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving contact: " + contact.getName(), e);
            throw new RuntimeException("Failed to save contact: " + contact.getName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Contact ID cannot be null");
        }

        LOGGER.log(Level.FINE, "Deleting contact with ID: {0}", id);
        try {
            Contact contact = entityManager.find(Contact.class, id);
            if (contact != null) {
                entityManager.remove(contact);
                LOGGER.log(Level.FINE, "Successfully deleted contact: {0}", contact.getName());
            } else {
                LOGGER.log(Level.WARNING, "Attempted to delete non-existent contact with ID: {0}", id);
                throw new RuntimeException("Contact not found with ID: " + id);
            }
        } catch (RuntimeException e) {
            // Re-throw our own RuntimeExceptions (like "Contact not found")
            if (e.getMessage().startsWith("Contact not found")) {
                throw e;
            }
            // Handle other runtime exceptions
            LOGGER.log(Level.SEVERE, "Error deleting contact with ID: " + id, e);
            throw new RuntimeException("Failed to delete contact with ID: " + id, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting contact with ID: " + id, e);
            throw new RuntimeException("Failed to delete contact with ID: " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Contact> findByNameContaining(String searchTerm) {
        if (searchTerm == null) {
            LOGGER.log(Level.FINE, "Search term is null, returning all contacts");
            return findAll();
        }

        String trimmedSearchTerm = searchTerm.trim();
        if (trimmedSearchTerm.isEmpty()) {
            LOGGER.log(Level.FINE, "Search term is empty, returning all contacts");
            return findAll();
        }

        LOGGER.log(Level.FINE, "Searching contacts with name containing: {0}", trimmedSearchTerm);
        try {
            TypedQuery<Contact> query = entityManager.createQuery(
                "SELECT c FROM Contact c WHERE LOWER(c.name) LIKE LOWER(:searchTerm) ORDER BY c.name ASC", 
                Contact.class);
            query.setParameter("searchTerm", "%" + trimmedSearchTerm + "%");
            
            List<Contact> contacts = query.getResultList();
            LOGGER.log(Level.FINE, "Found {0} contacts matching search term: {1}", 
                      new Object[]{contacts.size(), trimmedSearchTerm});
            return contacts;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching contacts with term: " + trimmedSearchTerm, e);
            throw new RuntimeException("Failed to search contacts with term: " + trimmedSearchTerm, e);
        }
    }
}