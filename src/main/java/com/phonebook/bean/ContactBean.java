package com.phonebook.bean;

import com.phonebook.entity.Contact;
import com.phonebook.service.ContactService;
import com.phonebook.service.ContactServiceException;
import com.phonebook.service.ContactNotFoundException;
import com.phonebook.service.ContactValidationException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Managed bean for handling contact-related UI interactions.
 * Provides the bridge between the JSF view layer and the business services.
 * Uses CDI for dependency injection and JSF ViewScoped for state management.
 */
@Named
@ViewScoped
public class ContactBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ContactBean.class.getName());

    @Inject
    private ContactService contactService;

    /**
     * Setter for ContactService - used for testing.
     * @param contactService the service to set
     */
    public void setContactService(ContactService contactService) {
        this.contactService = contactService;
    }

    // Properties for contact management
    private List<Contact> contacts;
    private List<Contact> filteredContacts;
    private Contact selectedContact;
    private Contact newContact;
    private String searchTerm;

    // Form state properties
    private boolean showAddForm;
    private boolean showEditForm;
    private boolean showDeleteConfirmation;

    /**
     * Initializes the bean after construction.
     * Loads all contacts and initializes form objects.
     */
    @PostConstruct
    public void init() {
        LOGGER.info("Initializing ContactBean");
        newContact = new Contact();
        selectedContact = new Contact();
        searchTerm = "";
        showAddForm = false;
        showEditForm = false;
        showDeleteConfirmation = false;
        loadAllContacts();
    }

    /**
     * Loads all contacts from the service layer.
     * Handles exceptions and displays appropriate error messages.
     */
    public void loadAllContacts() {
        try {
            LOGGER.info("Loading all contacts");
            contacts = contactService.getAllContacts();
            filteredContacts = new ArrayList<>(contacts);
            LOGGER.info("Loaded " + contacts.size() + " contacts");
        } catch (ContactServiceException e) {
            LOGGER.log(Level.SEVERE, "Error loading contacts", e);
            addErrorMessage("Error loading contacts", "Unable to load contacts from database. Please try again.");
            contacts = new ArrayList<>();
            filteredContacts = new ArrayList<>();
        }
    }

    /**
     * Action method to show the add contact form.
     */
    public void showAddContactForm() {
        LOGGER.info("Showing add contact form");
        newContact = new Contact();
        showAddForm = true;
        showEditForm = false;
        showDeleteConfirmation = false;
    }

    /**
     * Action method to hide the add contact form.
     */
    public void hideAddContactForm() {
        LOGGER.info("Hiding add contact form");
        showAddForm = false;
        newContact = new Contact();
    }

    /**
     * Action method to add a new contact.
     * Validates input, saves the contact, and refreshes the contact list.
     */
    public void addContact() {
        try {
            LOGGER.info("Adding new contact: " + newContact.getName());
            
            if (newContact.getName() == null || newContact.getName().trim().isEmpty()) {
                addErrorMessage("Validation Error", "Contact name is required.");
                return;
            }
            
            if (newContact.getPhoneNumber() == null || newContact.getPhoneNumber().trim().isEmpty()) {
                addErrorMessage("Validation Error", "Phone number is required.");
                return;
            }

            Contact savedContact = contactService.saveContact(newContact);
            LOGGER.info("Successfully added contact with ID: " + savedContact.getId());
            
            // Refresh contact list and reset form
            loadAllContacts();
            applySearchFilter();
            newContact = new Contact();
            showAddForm = false;
            
            addInfoMessage("Success", "Contact '" + savedContact.getName() + "' has been added successfully.");
            
        } catch (ContactValidationException e) {
            LOGGER.log(Level.WARNING, "Validation error adding contact", e);
            addErrorMessage("Validation Error", e.getMessage());
        } catch (ContactServiceException e) {
            LOGGER.log(Level.SEVERE, "Error adding contact", e);
            addErrorMessage("Error", "Unable to add contact. Please try again.");
        }
    }

    /**
     * Action method to show the edit contact form for the selected contact.
     *
     * @param contact the contact to edit
     */
    public void showEditContactForm(Contact contact) {
        if (contact == null) {
            addErrorMessage("Error", "No contact selected for editing.");
            return;
        }
        
        LOGGER.info("Showing edit form for contact: " + contact.getName());
        
        // Create a copy of the contact for editing to avoid modifying the original
        selectedContact = new Contact();
        selectedContact.setId(contact.getId());
        selectedContact.setName(contact.getName());
        selectedContact.setPhoneNumber(contact.getPhoneNumber());
        selectedContact.setEmail(contact.getEmail());
        
        showEditForm = true;
        showAddForm = false;
        showDeleteConfirmation = false;
    }

    /**
     * Action method to hide the edit contact form.
     */
    public void hideEditContactForm() {
        LOGGER.info("Hiding edit contact form");
        showEditForm = false;
        selectedContact = new Contact();
    }

    /**
     * Action method to update an existing contact.
     * Validates input, updates the contact, and refreshes the contact list.
     */
    public void updateContact() {
        try {
            LOGGER.info("Updating contact with ID: " + selectedContact.getId());
            
            if (selectedContact.getName() == null || selectedContact.getName().trim().isEmpty()) {
                addErrorMessage("Validation Error", "Contact name is required.");
                return;
            }
            
            if (selectedContact.getPhoneNumber() == null || selectedContact.getPhoneNumber().trim().isEmpty()) {
                addErrorMessage("Validation Error", "Phone number is required.");
                return;
            }

            Contact updatedContact = contactService.updateContact(selectedContact);
            LOGGER.info("Successfully updated contact: " + updatedContact.getName());
            
            // Refresh contact list and reset form
            loadAllContacts();
            applySearchFilter();
            selectedContact = new Contact();
            showEditForm = false;
            
            addInfoMessage("Success", "Contact '" + updatedContact.getName() + "' has been updated successfully.");
            
        } catch (ContactNotFoundException e) {
            LOGGER.log(Level.WARNING, "Contact not found for update", e);
            addErrorMessage("Error", "Contact not found. It may have been deleted by another user.");
            loadAllContacts();
            applySearchFilter();
            showEditForm = false;
        } catch (ContactValidationException e) {
            LOGGER.log(Level.WARNING, "Validation error updating contact", e);
            addErrorMessage("Validation Error", e.getMessage());
        } catch (ContactServiceException e) {
            LOGGER.log(Level.SEVERE, "Error updating contact", e);
            addErrorMessage("Error", "Unable to update contact. Please try again.");
        }
    }

    /**
     * Action method to show delete confirmation for the selected contact.
     *
     * @param contact the contact to delete
     */
    public void showDeleteConfirmation(Contact contact) {
        if (contact == null) {
            addErrorMessage("Error", "No contact selected for deletion.");
            return;
        }
        
        LOGGER.info("Showing delete confirmation for contact: " + contact.getName());
        selectedContact = contact;
        showDeleteConfirmation = true;
        showAddForm = false;
        showEditForm = false;
    }

    /**
     * Action method to hide the delete confirmation dialog.
     */
    public void hideDeleteConfirmation() {
        LOGGER.info("Hiding delete confirmation");
        showDeleteConfirmation = false;
        selectedContact = new Contact();
    }

    /**
     * Action method to delete the selected contact.
     * Removes the contact and refreshes the contact list.
     */
    public void deleteContact() {
        try {
            if (selectedContact == null || selectedContact.getId() == null) {
                addErrorMessage("Error", "No contact selected for deletion.");
                return;
            }
            
            LOGGER.info("Deleting contact with ID: " + selectedContact.getId());
            String contactName = selectedContact.getName();
            
            contactService.deleteContact(selectedContact.getId());
            LOGGER.info("Successfully deleted contact: " + contactName);
            
            // Refresh contact list and reset form
            loadAllContacts();
            applySearchFilter();
            selectedContact = new Contact();
            showDeleteConfirmation = false;
            
            addInfoMessage("Success", "Contact '" + contactName + "' has been deleted successfully.");
            
        } catch (ContactNotFoundException e) {
            LOGGER.log(Level.WARNING, "Contact not found for deletion", e);
            addErrorMessage("Error", "Contact not found. It may have already been deleted.");
            loadAllContacts();
            applySearchFilter();
            showDeleteConfirmation = false;
        } catch (ContactServiceException e) {
            LOGGER.log(Level.SEVERE, "Error deleting contact", e);
            addErrorMessage("Error", "Unable to delete contact. Please try again.");
        }
    }

    /**
     * Action method to perform contact search.
     * Filters the contact list based on the search term.
     */
    public void searchContacts() {
        try {
            LOGGER.info("Searching contacts with term: " + searchTerm);
            
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                // If search term is empty, show all contacts
                filteredContacts = new ArrayList<>(contacts);
            } else {
                List<Contact> searchResults = contactService.searchContacts(searchTerm.trim());
                filteredContacts = searchResults;
            }
            
            LOGGER.info("Search returned " + filteredContacts.size() + " contacts");
            
        } catch (ContactServiceException e) {
            LOGGER.log(Level.SEVERE, "Error searching contacts", e);
            addErrorMessage("Search Error", "Unable to search contacts. Please try again.");
            filteredContacts = new ArrayList<>(contacts);
        }
    }

    /**
     * Action method to clear the search and show all contacts.
     */
    public void clearSearch() {
        LOGGER.info("Clearing search");
        searchTerm = "";
        filteredContacts = new ArrayList<>(contacts);
    }

    /**
     * Applies the current search filter to the contact list.
     * This method is called after CRUD operations to maintain search state.
     */
    private void applySearchFilter() {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            searchContacts();
        } else {
            filteredContacts = new ArrayList<>(contacts);
        }
    }

    /**
     * Adds an info message to be displayed to the user.
     *
     * @param summary the message summary
     * @param detail  the detailed message
     */
    private void addInfoMessage(String summary, String detail) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
        }
    }

    /**
     * Adds an error message to be displayed to the user.
     *
     * @param summary the message summary
     * @param detail  the detailed message
     */
    private void addErrorMessage(String summary, String detail) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
        }
    }

    /**
     * Adds a warning message to be displayed to the user.
     *
     * @param summary the message summary
     * @param detail  the detailed message
     */
    private void addWarningMessage(String summary, String detail) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            facesContext.addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, summary, detail));
        }
    }

    // Getters and Setters

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<Contact> getFilteredContacts() {
        return filteredContacts;
    }

    public void setFilteredContacts(List<Contact> filteredContacts) {
        this.filteredContacts = filteredContacts;
    }

    public Contact getSelectedContact() {
        return selectedContact;
    }

    public void setSelectedContact(Contact selectedContact) {
        this.selectedContact = selectedContact;
    }

    public Contact getNewContact() {
        return newContact;
    }

    public void setNewContact(Contact newContact) {
        this.newContact = newContact;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public boolean isShowAddForm() {
        return showAddForm;
    }

    public void setShowAddForm(boolean showAddForm) {
        this.showAddForm = showAddForm;
    }

    public boolean isShowEditForm() {
        return showEditForm;
    }

    public void setShowEditForm(boolean showEditForm) {
        this.showEditForm = showEditForm;
    }

    public boolean isShowDeleteConfirmation() {
        return showDeleteConfirmation;
    }

    public void setShowDeleteConfirmation(boolean showDeleteConfirmation) {
        this.showDeleteConfirmation = showDeleteConfirmation;
    }

    /**
     * Utility method to check if there are any contacts to display.
     *
     * @return true if there are contacts, false otherwise
     */
    public boolean hasContacts() {
        return filteredContacts != null && !filteredContacts.isEmpty();
    }

    /**
     * Utility method to check if search is active.
     *
     * @return true if search term is not empty, false otherwise
     */
    public boolean isSearchActive() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }

    /**
     * Gets the count of filtered contacts for display purposes.
     *
     * @return the number of contacts currently displayed
     */
    public int getContactCount() {
        return filteredContacts != null ? filteredContacts.size() : 0;
    }
}