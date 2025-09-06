package com.phonebook.repository;

import com.phonebook.entity.Contact;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

public class H2ContactRepository implements ContactRepositoryInterface {
    
    private static final Logger LOGGER = Logger.getLogger(H2ContactRepository.class.getName());
    private EntityManagerFactory emf;
    
    public H2ContactRepository() {
        try {
            emf = Persistence.createEntityManagerFactory("phonebookPU");
            LOGGER.info("H2 EntityManagerFactory created successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to create H2 EntityManagerFactory: " + e.getMessage());
            throw new RuntimeException("H2 Database initialization failed", e);
        }
    }
    
    public List<Contact> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Contact> query = em.createQuery("SELECT c FROM Contact c ORDER BY c.name", Contact.class);
            List<Contact> contacts = query.getResultList();
            LOGGER.info("Found " + contacts.size() + " contacts in H2");
            return contacts;
        } catch (Exception e) {
            LOGGER.severe("Error finding all contacts in H2: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve contacts from H2", e);
        } finally {
            em.close();
        }
    }
    
    public Contact save(Contact contact) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Contact savedContact;
            if (contact.getId() == null) {
                em.persist(contact);
                savedContact = contact;
                LOGGER.info("Created new contact in H2: " + contact.getName());
            } else {
                savedContact = em.merge(contact);
                LOGGER.info("Updated contact in H2: " + contact.getName());
            }
            em.getTransaction().commit();
            return savedContact;
        } catch (Exception e) {
            em.getTransaction().rollback();
            LOGGER.severe("Error saving contact to H2: " + e.getMessage());
            throw new RuntimeException("Failed to save contact to H2", e);
        } finally {
            em.close();
        }
    }
    
    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Contact contact = em.find(Contact.class, id);
            if (contact != null) {
                em.remove(contact);
                LOGGER.info("Deleted contact from H2 with ID: " + id);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            LOGGER.severe("Error deleting contact from H2: " + e.getMessage());
            throw new RuntimeException("Failed to delete contact from H2", e);
        } finally {
            em.close();
        }
    }
    
    public Contact findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Contact.class, id);
        } catch (Exception e) {
            LOGGER.severe("Error finding contact by ID in H2: " + e.getMessage());
            throw new RuntimeException("Failed to find contact in H2", e);
        } finally {
            em.close();
        }
    }
    
    public List<Contact> findByNameContaining(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Contact> query = em.createQuery(
                "SELECT c FROM Contact c WHERE LOWER(c.name) LIKE LOWER(:name) ORDER BY c.name", 
                Contact.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.severe("Error searching contacts in H2: " + e.getMessage());
            throw new RuntimeException("Failed to search contacts in H2", e);
        } finally {
            em.close();
        }
    }
}
