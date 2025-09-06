// Simple JavaScript test script for form functionality
// Run this in the browser console to test the form

console.log("Starting form functionality test...");

// Test 1: Check if application loads
function testApplicationLoad() {
    console.log("Test 1: Application Load");
    const title = document.title;
    const header = document.querySelector('h1');
    console.log("Page title:", title);
    console.log("Header text:", header ? header.textContent : "Not found");
    return title.includes("Phonebook") || (header && header.textContent.includes("Phonebook"));
}

// Test 2: Test Add Contact button (opens form)
function testAddContactButton() {
    console.log("Test 2: Add Contact Button");
    const addButton = document.querySelector('[id*="addContactBtn"]') || 
                     document.querySelector('input[value*="Add"]') ||
                     document.querySelector('button[value*="Add"]');
    
    if (addButton) {
        console.log("Add Contact button found:", addButton);
        console.log("Clicking Add Contact button...");
        addButton.click();
        
        // Wait a moment for the form to appear
        setTimeout(() => {
            const form = document.querySelector('[id*="addForm"]') || 
                        document.querySelector('.dialog-content') ||
                        document.querySelector('[style*="position: fixed"]');
            console.log("Form appeared:", !!form);
            return !!form;
        }, 1000);
    } else {
        console.log("Add Contact button not found");
        return false;
    }
}

// Test 3: Fill form fields
function testFillForm() {
    console.log("Test 3: Fill Form Fields");
    
    // Find form inputs
    const nameInput = document.querySelector('[id*="addName"]') || 
                     document.querySelector('input[placeholder*="name"]');
    const phoneInput = document.querySelector('[id*="addPhone"]') || 
                      document.querySelector('input[placeholder*="phone"]');
    const emailInput = document.querySelector('[id*="addEmail"]') || 
                      document.querySelector('input[placeholder*="email"]');
    
    console.log("Name input found:", !!nameInput);
    console.log("Phone input found:", !!phoneInput);
    console.log("Email input found:", !!emailInput);
    
    if (nameInput && phoneInput) {
        nameInput.value = "Test User";
        phoneInput.value = "123-456-7890";
        if (emailInput) emailInput.value = "test@example.com";
        
        // Trigger change events
        [nameInput, phoneInput, emailInput].forEach(input => {
            if (input) {
                input.dispatchEvent(new Event('change', { bubbles: true }));
                input.dispatchEvent(new Event('input', { bubbles: true }));
            }
        });
        
        console.log("Form filled successfully");
        return true;
    } else {
        console.log("Required form inputs not found");
        return false;
    }
}

// Test 4: Test Cancel button
function testCancelButton() {
    console.log("Test 4: Cancel Button");
    
    const cancelButton = document.querySelector('[value*="Cancel"]') || 
                        document.querySelector('input[value*="cancel"]') ||
                        document.querySelector('button[value*="cancel"]');
    
    if (cancelButton) {
        console.log("Cancel button found:", cancelButton);
        console.log("Clicking Cancel button...");
        cancelButton.click();
        
        // Check if form closes
        setTimeout(() => {
            const form = document.querySelector('[id*="addForm"]') || 
                        document.querySelector('.dialog-content') ||
                        document.querySelector('[style*="position: fixed"]');
            const formClosed = !form || form.style.display === 'none';
            console.log("Form closed:", formClosed);
            return formClosed;
        }, 1000);
    } else {
        console.log("Cancel button not found");
        return false;
    }
}

// Test 5: Test Save button
function testSaveButton() {
    console.log("Test 5: Save Button");
    
    // First open the form again
    testAddContactButton();
    
    setTimeout(() => {
        testFillForm();
        
        setTimeout(() => {
            const saveButton = document.querySelector('[value*="Save"]') || 
                              document.querySelector('input[value*="save"]') ||
                              document.querySelector('button[value*="save"]');
            
            if (saveButton) {
                console.log("Save button found:", saveButton);
                console.log("Clicking Save button...");
                saveButton.click();
                
                // Check if contact was added
                setTimeout(() => {
                    const contactsTable = document.querySelector('table') || 
                                         document.querySelector('[id*="contactsTable"]');
                    const contacts = contactsTable ? contactsTable.querySelectorAll('tr').length : 0;
                    console.log("Contacts in table:", contacts);
                    
                    // Check for success message
                    const messages = document.querySelector('[id*="messages"]') || 
                                   document.querySelector('.message');
                    console.log("Success message found:", !!messages);
                    
                    return contacts > 1; // Header row + at least one contact
                }, 2000);
            } else {
                console.log("Save button not found");
                return false;
            }
        }, 1000);
    }, 1000);
}

// Run all tests
function runAllTests() {
    console.log("=== FORM FUNCTIONALITY TEST SUITE ===");
    
    const test1 = testApplicationLoad();
    console.log("Application Load:", test1 ? "PASS" : "FAIL");
    
    setTimeout(() => {
        testAddContactButton();
        
        setTimeout(() => {
            testCancelButton();
            
            setTimeout(() => {
                testSaveButton();
            }, 2000);
        }, 2000);
    }, 1000);
}

// Export functions for manual testing
window.formTests = {
    runAll: runAllTests,
    testLoad: testApplicationLoad,
    testAddButton: testAddContactButton,
    testFill: testFillForm,
    testCancel: testCancelButton,
    testSave: testSaveButton
};

console.log("Form test functions loaded. Run formTests.runAll() to start testing.");