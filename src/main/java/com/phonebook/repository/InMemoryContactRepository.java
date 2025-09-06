package com.phonebook.repository;

import com.phonebook.entity.Contact;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class InMemoryContactRepository implements ContactRepositoryInterface {
    
    private static final Logger LOGGER = Logger.getLogger(InMemoryContactRepository.class.getName());
    private static final Map<Long, Contact> contacts = new HashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1);
    
    public List<Contact> findAll() {
        LOGGER.info("Finding all contacts, current count: " + contacts.size());
        return new ArrayList<>(contacts.values());
    }
    
    public Contact save(Contact contact) {
        if (contact.getId() == null) {
            contact.setId(idGenerator.getAndIncrement());
        }
        contacts.put(contact.getId(), contact);
        LOGGER.info("Saved contact: " + contact.getName() + " with ID: " + contact.getId());
        return contact;
    }
    
    public void delete(Long id) {
        contacts.remove(id);
        LOGGER.info("Deleted contact with ID: " + id);
    }
    
    public Contact findById(Long id) {
        return contacts.get(id);
    }
    
    public List<Contact> findByNameContaining(String name) {
        List<Contact> result = new ArrayList<>();
        for (Contact contact : contacts.values()) {
            if (contact.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(contact);
            }
        }
        return result;
    }
}
