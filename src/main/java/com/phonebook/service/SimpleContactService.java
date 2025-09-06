package com.phonebook.service;

import com.phonebook.entity.Contact;
import com.phonebook.repository.SimpleContactRepository;
import java.util.List;
import java.util.logging.Logger;

public class SimpleContactService {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleContactService.class.getName());
    private SimpleContactRepository repository;
    
    public SimpleContactService() {
        this.repository = new SimpleContactRepository();
    }
    
    public List<Contact> getAllContacts() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            LOGGER.severe("Error getting all contacts: " + e.getMessage());
            throw new RuntimeException("Failed to get contacts", e);
        }
    }
    
    public Contact saveContact(Contact contact) {
        try {
            if (contact.getName() == null || contact.getName().trim().isEmpty()) {
                throw new RuntimeException("Name is required");
            }
            if (contact.getPhoneNumber() == null || contact.getPhoneNumber().trim().isEmpty()) {
                throw new RuntimeException("Phone number is required");
            }
            
            return repository.save(contact);
        } catch (Exception e) {
            LOGGER.severe("Error saving contact: " + e.getMessage());
            throw new RuntimeException("Failed to save contact", e);
        }
    }
    
    public Contact updateContact(Contact contact) {
        try {
            if (contact.getName() == null || contact.getName().trim().isEmpty()) {
                throw new RuntimeException("Name is required");
            }
            if (contact.getPhoneNumber() == null || contact.getPhoneNumber().trim().isEmpty()) {
                throw new RuntimeException("Phone number is required");
            }
            
            return repository.save(contact);
        } catch (Exception e) {
            LOGGER.severe("Error updating contact: " + e.getMessage());
            throw new RuntimeException("Failed to update contact", e);
        }
    }
    
    public Contact getContactById(Long id) {
        try {
            return repository.findById(id);
        } catch (Exception e) {
            LOGGER.severe("Error getting contact by ID: " + e.getMessage());
            throw new RuntimeException("Failed to get contact", e);
        }
    }
    
    public void deleteContact(Long id) {
        try {
            repository.delete(id);
            LOGGER.info("Successfully deleted contact with ID: " + id);
        } catch (Exception e) {
            LOGGER.severe("Error deleting contact: " + e.getMessage());
            throw new RuntimeException("Failed to delete contact", e);
        }
    }
}
