package com.phonebook.repository;

import com.phonebook.entity.Contact;
import java.util.List;
import java.util.logging.Logger;

public class SimpleContactRepository implements ContactRepositoryInterface {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleContactRepository.class.getName());
    private ContactRepositoryInterface repository;
    
    // Configuration: set to "H2" to use H2 database, "MEMORY" for in-memory
    private static final String REPOSITORY_TYPE = System.getProperty("repository.type", "H2");
    
    public SimpleContactRepository() {
        try {
            if ("H2".equalsIgnoreCase(REPOSITORY_TYPE)) {
                repository = new H2ContactRepository();
                LOGGER.info("Using H2 database repository");
            } else {
                repository = new InMemoryContactRepository();
                LOGGER.info("Using in-memory repository");
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to initialize " + REPOSITORY_TYPE + " repository, falling back to in-memory: " + e.getMessage());
            repository = new InMemoryContactRepository();
        }
    }
    
    public List<Contact> findAll() {
        return repository.findAll();
    }
    
    public Contact save(Contact contact) {
        return repository.save(contact);
    }
    
    public void delete(Long id) {
        repository.delete(id);
    }
    
    public Contact findById(Long id) {
        return repository.findById(id);
    }
    
    public List<Contact> findByNameContaining(String name) {
        return repository.findByNameContaining(name);
    }
}
