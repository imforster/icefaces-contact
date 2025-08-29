package com.phonebook.service;

import com.phonebook.entity.Contact;
import com.phonebook.repository.ContactRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business service layer for Contact operations.
 * Provides business logic and delegates to ContactRepository for data access.
 * Uses CDI for dependency injection and EJB for transaction management.
 */
@Stateless
public class ContactService {

    private static final Logger LOGGER = Logger.getLogger(ContactService.class.getName());

    @Inject
    private ContactRepository contactRepository;

    @Inject
    private Validator validator;

    /**
     * Setter for ContactRepository - used for testing.
     * @param contactRepository the repository to set
     */
    public void setContactRepository(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    /**
     * Retrieves all contacts from the database.
     *
     * @return List of all contacts, empty list if no contacts exist
     * @throws ContactServiceException if there's an error retrieving contacts
     */
    public List<Contact> getAllContacts() {
        try {
            LOGGER.info("Retrieving all contacts");
            List<Contact> contacts = contactRepository.findAll();
            LOGGER.info("Retrieved " + contacts.size() + " contacts");
            return contacts;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all contacts", e);
            throw new ContactServiceException("Failed to retrieve contacts", e);
        }
    }

    /**
     * Finds a contact by its unique identifier.
     *
     * @param id the contact ID
     * @return the contact if found
     * @throws ContactServiceException if contact not found or error occurs
     */
    public Contact getContactById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Contact ID cannot be null");
        }

        try {
            LOGGER.info("Retrieving contact with ID: " + id);
            Contact contact = contactRepository.findById(id);
            if (contact == null) {
                throw new ContactNotFoundException("Contact not found with ID: " + id);
            }
            LOGGER.info("Retrieved contact: " + contact.getName());
            return contact;
        } catch (ContactNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving contact with ID: " + id, e);
            throw new ContactServiceException("Failed to retrieve contact", e);
        }
    }

    /**
     * Saves a new contact to the database.
     * Validates the contact before saving.
     *
     * @param contact the contact to save
     * @return the saved contact with generated ID
     * @throws ContactServiceException if validation fails or error occurs during save
     */
    public Contact saveContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }

        // Validate the contact
        validateContact(contact);

        try {
            LOGGER.info("Saving new contact: " + contact.getName());
            Contact savedContact = contactRepository.save(contact);
            LOGGER.info("Successfully saved contact with ID: " + savedContact.getId());
            return savedContact;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving contact: " + contact.getName(), e);
            throw new ContactServiceException("Failed to save contact", e);
        }
    }

    /**
     * Updates an existing contact in the database.
     * Validates the contact before updating.
     *
     * @param contact the contact to update
     * @return the updated contact
     * @throws ContactServiceException if validation fails, contact not found, or error occurs
     */
    public Contact updateContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        if (contact.getId() == null) {
            throw new IllegalArgumentException("Contact ID cannot be null for update operation");
        }

        // Validate the contact
        validateContact(contact);

        // Check if contact exists
        Contact existingContact = contactRepository.findById(contact.getId());
        if (existingContact == null) {
            throw new ContactNotFoundException("Contact not found with ID: " + contact.getId());
        }

        try {
            LOGGER.info("Updating contact with ID: " + contact.getId());
            Contact updatedContact = contactRepository.save(contact);
            LOGGER.info("Successfully updated contact: " + updatedContact.getName());
            return updatedContact;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating contact with ID: " + contact.getId(), e);
            throw new ContactServiceException("Failed to update contact", e);
        }
    }

    /**
     * Deletes a contact by its unique identifier.
     *
     * @param id the ID of the contact to delete
     * @throws ContactServiceException if contact not found or error occurs during deletion
     */
    public void deleteContact(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Contact ID cannot be null");
        }

        // Check if contact exists
        Contact existingContact = contactRepository.findById(id);
        if (existingContact == null) {
            throw new ContactNotFoundException("Contact not found with ID: " + id);
        }

        try {
            LOGGER.info("Deleting contact with ID: " + id);
            contactRepository.delete(id);
            LOGGER.info("Successfully deleted contact with ID: " + id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting contact with ID: " + id, e);
            throw new ContactServiceException("Failed to delete contact", e);
        }
    }

    /**
     * Searches for contacts whose name contains the specified search term.
     * The search is case-insensitive.
     *
     * @param searchTerm the term to search for in contact names
     * @return List of contacts matching the search criteria, empty list if no matches
     * @throws ContactServiceException if error occurs during search
     */
    public List<Contact> searchContacts(String searchTerm) {
        if (searchTerm == null) {
            searchTerm = "";
        }

        try {
            LOGGER.info("Searching contacts with term: " + searchTerm);
            List<Contact> contacts = contactRepository.findByNameContaining(searchTerm.trim());
            LOGGER.info("Found " + contacts.size() + " contacts matching search term");
            return contacts;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching contacts with term: " + searchTerm, e);
            throw new ContactServiceException("Failed to search contacts", e);
        }
    }

    /**
     * Validates a contact using Bean Validation.
     *
     * @param contact the contact to validate
     * @throws ContactValidationException if validation fails
     */
    private void validateContact(Contact contact) {
        Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Contact validation failed: ");
            for (ConstraintViolation<Contact> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            String errorMessage = sb.toString();
            LOGGER.warning(errorMessage);
            throw new ContactValidationException(errorMessage);
        }
    }
}