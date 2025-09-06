#!/bin/bash

echo "Testing Add Contact Functionality"
echo "================================="

# Test 1: Check if application is running
echo "1. Testing if application is accessible..."
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/phonebook/contacts.xhtml)
if [ "$response" = "200" ]; then
    echo "✓ Application is running"
else
    echo "✗ Application not accessible (HTTP $response)"
    exit 1
fi

# Test 2: Check initial contact count
echo "2. Checking initial contact count..."
initial_count=$(curl -s http://localhost:8080/phonebook/contacts.xhtml | grep -o "Showing [0-9]* contact" | grep -o "[0-9]*")
echo "Initial contact count: $initial_count"

# Test 3: Check server logs for any errors
echo "3. Checking recent server logs for errors..."
if tail -n 20 wildfly-26.1.3.Final/standalone/log/server.log | grep -i error; then
    echo "⚠ Found errors in server logs"
else
    echo "✓ No recent errors in server logs"
fi

echo ""
echo "Manual Test Instructions:"
echo "========================="
echo "1. Open http://localhost:8080/phonebook/contacts.xhtml in your browser"
echo "2. Click 'Add Contact' button"
echo "3. Fill in Name: 'Test User' and Phone: '555-1234'"
echo "4. Click 'Save' button"
echo "5. Check if:"
echo "   - Dialog closes"
echo "   - Success message appears"
echo "   - Contact appears in table"
echo "   - Contact count updates"
echo ""
echo "If the contact doesn't appear, check the browser's developer console for JavaScript errors."