package com.phonebook.repository;

import com.phonebook.entity.Contact;
import java.util.List;

/**
 * Repository interface for Contact entity operations.
 * Defines CRUD operations and search functionality for contacts.
 */
public interface ContactRepository {

    /**
     * Retrieves all contacts from the database.
     *
     * @return List of all contacts, empty list if no contacts exist
     */
    List<Contact> findAll();

    /**
     * Finds a contact by its unique identifier.
     *
     * @param id the contact ID
     * @return the contact if found, null otherwise
     */
    Contact findById(Long id);

    /**
     * Saves a new contact or updates an existing one.
     * If the contact has no ID, it will be persisted as a new entity.
     * If the contact has an ID, it will be merged with the existing entity.
     *
     * @param contact the contact to save
     * @return the saved contact with generated ID if it was a new entity
     */
    Contact save(Contact contact);

    /**
     * Deletes a contact by its unique identifier.
     *
     * @param id the ID of the contact to delete
     */
    void delete(Long id);

    /**
     * Searches for contacts whose name contains the specified search term.
     * The search is case-insensitive.
     *
     * @param searchTerm the term to search for in contact names
     * @return List of contacts matching the search criteria, empty list if no matches
     */
    List<Contact> findByNameContaining(String searchTerm);
}