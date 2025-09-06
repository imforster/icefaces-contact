# Comprehensive Form Test Plan

## Overview
Since the Playwright MCP setup needs more configuration, let's use a combination of manual testing and browser automation to thoroughly test the form functionality.

## Current Status
✅ **Application**: Running at http://localhost:8080/phonebook/
✅ **Form Configuration**: ICEfaces `ace:pushButton` with `actionListener`
✅ **Validation**: Temporarily removed for testing
✅ **Logging**: Backend methods have detailed logging

## Test Method 1: Manual Testing with Server Log Monitoring

### Step 1: Open Two Terminal Windows

**Terminal 1 - Monitor Logs:**
```bash
tail -f ./wildfly-26.1.3.Final/standalone/log/server.log
```

**Terminal 2 - Test Commands:**
```bash
# Test application accessibility
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/phonebook/
```

### Step 2: Manual Browser Testing
1. Open http://localhost:8080/phonebook/
2. Click "Add Contact" button
3. **Expected in logs**: `INFO: Showing add contact form`
4. Fill form fields:
   - Name: Test User
   - Phone: 123-456-7890
   - Email: test@example.com
5. Click "Cancel" button
6. **Expected in logs**: `INFO: === HIDE ADD CONTACT FORM METHOD CALLED ===`
7. Repeat steps 2-4, then click "Save" button
8. **Expected in logs**: `INFO: === ADD CONTACT METHOD CALLED ===`

## Test Method 2: Browser Console Automation

### Step 1: Load Test Script
1. Open http://localhost:8080/phonebook/
2. Open browser developer tools (F12)
3. Go to Console tab
4. Copy and paste the contents of `test-form-automation.js`
5. Press Enter to load the test functions

### Step 2: Run Automated Tests
```javascript
// Run all tests automatically
formTests.runAll();

// Or run individual tests
formTests.testLoad();        // Test if application loads
formTests.testAddButton();   // Test Add Contact button
formTests.testFill();        // Test form filling
formTests.testCancel();      // Test Cancel button
formTests.testSave();        // Test Save button
```

## Test Method 3: Network Analysis

### Step 1: Monitor Network Requests
1. Open browser developer tools (F12)
2. Go to Network tab
3. Click "Add Contact" button
4. Fill form and click "Save"
5. Look for AJAX requests in the network tab

### Expected Network Activity:
- ✅ Initial page load requests
- ✅ AJAX request when "Add Contact" is clicked
- ✅ AJAX request when "Save" is clicked
- ❌ If no AJAX request on Save, the button isn't working

## Test Results Analysis

### If Cancel Button Works:
✅ **Logs show**: `=== HIDE ADD CONTACT FORM METHOD CALLED ===`
✅ **UI**: Dialog closes
✅ **Conclusion**: ICEfaces `ace:pushButton` + `actionListener` works

### If Cancel Button Doesn't Work:
❌ **Logs show**: No log entry
❌ **UI**: Dialog stays open
❌ **Conclusion**: ICEfaces component issue

### If Save Button Works:
✅ **Logs show**: `=== ADD CONTACT METHOD CALLED ===`
✅ **UI**: Contact appears in table, dialog closes
✅ **Conclusion**: Form submission works

### If Save Button Doesn't Work:
❌ **Logs show**: No log entry
❌ **UI**: No contact added, dialog stays open
❌ **Possible causes**:
   - JavaScript error blocking AJAX
   - Form validation failing silently
   - AJAX configuration issue
   - Method binding problem

## Troubleshooting Steps

### If Both Buttons Fail:
1. **Check JavaScript Console** for errors
2. **Check Network Tab** for failed requests
3. **Verify ICEfaces Resources** are loading
4. **Consider reverting** to standard JSF components

### If Only Save Button Fails:
1. **Check form validation** (even though removed)
2. **Verify method signature** matches actionListener requirements
3. **Test with simpler AJAX configuration**
4. **Check AJAX execute/render attributes**

## Next Steps Based on Results

### If Cancel Works, Save Doesn't:
- Focus on save button specific issues
- Check method signature compatibility
- Test different AJAX configurations

### If Neither Works:
- Revert to standard JSF `h:commandButton`
- Use `f:ajax` instead of `ace:ajax`
- Match the working simple version exactly

### If Both Work:
- Re-add form validation
- Test edge cases
- Complete the form functionality

## Run the Tests Now
Please run through these tests and report back:
1. What do you see in the server logs?
2. Do the buttons work in the browser?
3. Any JavaScript errors in the console?
4. What network requests do you see?

This will help us determine the exact issue and the best fix.