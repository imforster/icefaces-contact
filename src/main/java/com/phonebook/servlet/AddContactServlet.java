package com.phonebook.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Inject;

import com.phonebook.entity.Contact;
import com.phonebook.service.ContactService;

@WebServlet("/AddContactServlet")
public class AddContactServlet extends HttpServlet {
    
    @Inject
    private ContactService contactService;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String phoneNumber = request.getParameter("phoneNumber");
        String email = request.getParameter("email");
        
        try {
            Contact contact = new Contact();
            contact.setName(name);
            contact.setPhoneNumber(phoneNumber);
            contact.setEmail(email);
            
            contactService.saveContact(contact);
            
            response.sendRedirect("contacts.xhtml?success=true");
            
        } catch (Exception e) {
            request.setAttribute("error", "Failed to add contact: " + e.getMessage());
            request.getRequestDispatcher("add-contact-simple.html").forward(request, response);
        }
    }
}
