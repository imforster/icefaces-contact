package com.phonebook.bean;

import com.phonebook.entity.Contact;
import com.phonebook.service.SimpleContactService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SimpleContactBean implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(SimpleContactBean.class.getName());
    
    private SimpleContactService contactService;
    private List<Contact> contacts;
    private List<Contact> filteredContacts;
    private Contact newContact;
    private Contact selectedContact;
    private String searchTerm;
    private boolean searchActive = false;
    private boolean showAddForm = false;
    private boolean showEditForm = false;
    private boolean showDeleteConfirmation = false;
    
    @PostConstruct
    public void init() {
        LOGGER.info("Initializing SimpleContactBean");
        contactService = new SimpleContactService();
        newContact = new Contact();
        selectedContact = new Contact();
        searchTerm = "";
        
        // Handle form submission
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            String action = context.getExternalContext().getRequestParameterMap().get("action");
            String searchParam = context.getExternalContext().getRequestParameterMap().get("searchTerm");
            
            if ("add".equals(action)) {
                handleFormSubmission();
            } else if ("edit".equals(action)) {
                handleEditSubmission();
            } else if ("delete".equals(action)) {
                handleDeleteSubmission();
            } else if ("search".equals(action) || searchParam != null) {
                handleSearchSubmission();
            }
        }
        
        loadContacts();
    }
    
    private void handleFormSubmission() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String name = context.getExternalContext().getRequestParameterMap().get("name");
            String phone = context.getExternalContext().getRequestParameterMap().get("phoneNumber");
            String email = context.getExternalContext().getRequestParameterMap().get("email");
            
            if (name != null && !name.trim().isEmpty() && phone != null && !phone.trim().isEmpty()) {
                Contact contact = new Contact();
                contact.setName(name.trim());
                contact.setPhoneNumber(phone.trim());
                if (email != null && !email.trim().isEmpty()) {
                    contact.setEmail(email.trim());
                }
                
                contactService.saveContact(contact);
                addMessage("Contact added successfully!");
            }
        } catch (Exception e) {
            LOGGER.severe("Error adding contact: " + e.getMessage());
            addMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleEditSubmission() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String idStr = context.getExternalContext().getRequestParameterMap().get("id");
            String name = context.getExternalContext().getRequestParameterMap().get("name");
            String phone = context.getExternalContext().getRequestParameterMap().get("phoneNumber");
            String email = context.getExternalContext().getRequestParameterMap().get("email");
            
            if (idStr != null && name != null && !name.trim().isEmpty() && phone != null && !phone.trim().isEmpty()) {
                Long id = Long.parseLong(idStr);
                Contact contact = contactService.getContactById(id);
                if (contact != null) {
                    contact.setName(name.trim());
                    contact.setPhoneNumber(phone.trim());
                    contact.setEmail(email != null ? email.trim() : "");
                    
                    contactService.updateContact(contact);
                    addMessage("Contact updated successfully!");
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error updating contact: " + e.getMessage());
            addMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleDeleteSubmission() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String idStr = context.getExternalContext().getRequestParameterMap().get("id");
            
            if (idStr != null) {
                Long id = Long.parseLong(idStr);
                contactService.deleteContact(id);
                addMessage("Contact deleted successfully!");
            }
        } catch (Exception e) {
            LOGGER.severe("Error deleting contact: " + e.getMessage());
            addMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleSearchSubmission() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            String searchParam = context.getExternalContext().getRequestParameterMap().get("searchTerm");
            
            if (searchParam != null) {
                searchTerm = searchParam.trim();
            }
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                searchActive = true;
                filteredContacts = new ArrayList<>();
                for (Contact contact : contacts) {
                    if (contact.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                        filteredContacts.add(contact);
                    }
                }
            } else {
                searchActive = false;
                filteredContacts = new ArrayList<>(contacts);
            }
        } catch (Exception e) {
            LOGGER.severe("Error searching contacts: " + e.getMessage());
        }
    }
    
    public void clearSearch() {
        searchTerm = "";
        if (contacts != null) {
            filteredContacts = new ArrayList<>(contacts);
        }
    }
    
    // Override isSearchActive to check if there's text in search field
    public boolean isSearchActive() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }
    
    private void loadContacts() {
        try {
            contacts = contactService.getAllContacts();
            if (contacts == null) {
                contacts = new ArrayList<>();
            }
            filteredContacts = new ArrayList<>(contacts);
        } catch (Exception e) {
            LOGGER.severe("Error loading contacts: " + e.getMessage());
            contacts = new ArrayList<>();
            filteredContacts = new ArrayList<>();
        }
    }
    
    private void addMessage(String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(null, new FacesMessage(message));
        }
    }
    
    // Getters and setters
    public List<Contact> getContacts() { return contacts; }
    public List<Contact> getFilteredContacts() { return filteredContacts; }
    public Contact getNewContact() { return newContact; }
    public Contact getSelectedContact() { return selectedContact; }
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
    public boolean isShowAddForm() { return showAddForm; }
    public boolean isShowEditForm() { return showEditForm; }
    public boolean isShowDeleteConfirmation() { return showDeleteConfirmation; }
    public int getContactCount() { return filteredContacts != null ? filteredContacts.size() : 0; }
    
    public void searchContacts() {
        try {
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                filteredContacts = new ArrayList<>();
                String searchLower = searchTerm.toLowerCase().trim();
                for (Contact contact : contacts) {
                    if (contact.getName().toLowerCase().contains(searchLower)) {
                        filteredContacts.add(contact);
                    }
                }
            } else {
                filteredContacts = new ArrayList<>(contacts);
            }
        } catch (Exception e) {
            LOGGER.severe("Error searching contacts: " + e.getMessage());
        }
    }
    
    // Dummy methods for compatibility
    public void showAddContactForm() {}
    public void hideAddContactForm() {}
    public void showEditContactForm(Contact contact) {}
    public void hideEditContactForm() {}
    public void showDeleteConfirmation(Contact contact) {}
    public void hideDeleteConfirmation() {}
    
    public String addContact() {
        try {
            if (newContact.getName() == null || newContact.getName().trim().isEmpty()) {
                addMessage("Name is required");
                return null;
            }
            if (newContact.getPhoneNumber() == null || newContact.getPhoneNumber().trim().isEmpty()) {
                addMessage("Phone number is required");
                return null;
            }
            
            contactService.saveContact(newContact);
            addMessage("Contact added successfully!");
            
            // Reset form
            newContact = new Contact();
            
            // Redirect to contacts page
            return "contacts.xhtml?faces-redirect=true";
        } catch (Exception e) {
            LOGGER.severe("Error adding contact: " + e.getMessage());
            addMessage("Error: " + e.getMessage());
            return null;
        }
    }
    
    public void updateContact() {}
    public void deleteContact() {}
    public void testMethod() {}
}
