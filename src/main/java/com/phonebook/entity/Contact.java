package com.phonebook.entity;

import com.phonebook.validation.PhoneNumber;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * Contact entity representing a phone book contact.
 * Maps to the 'contacts' table in the H2 database.
 */
@Entity
@Table(name = "contacts", indexes = {
    @Index(name = "idx_contact_name", columnList = "name")
})
public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Phone number is required")
    @PhoneNumber(allowInternational = true, allowExtensions = false, minLength = 7, maxLength = 15)
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$|^$", 
             message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(name = "email", length = 100)
    private String email;

    /**
     * Default constructor required by JPA.
     */
    public Contact() {
    }

    /**
     * Constructor with required fields.
     *
     * @param name        the contact's name
     * @param phoneNumber the contact's phone number
     */
    public Contact(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Constructor with all fields.
     *
     * @param name        the contact's name
     * @param phoneNumber the contact's phone number
     * @param email       the contact's email address
     */
    public Contact(String name, String phoneNumber, String email) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Equals method based on business key (name and phoneNumber).
     * Two contacts are considered equal if they have the same name and phone number.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Contact contact = (Contact) obj;
        return Objects.equals(name, contact.name) && 
               Objects.equals(phoneNumber, contact.phoneNumber);
    }

    /**
     * HashCode method based on business key (name and phoneNumber).
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, phoneNumber);
    }

    /**
     * String representation of the Contact entity.
     */
    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}