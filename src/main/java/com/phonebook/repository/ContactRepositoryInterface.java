package com.phonebook.repository;

import com.phonebook.entity.Contact;
import java.util.List;

public interface ContactRepositoryInterface {
    List<Contact> findAll();
    Contact save(Contact contact);
    void delete(Long id);
    Contact findById(Long id);
    List<Contact> findByNameContaining(String name);
}
