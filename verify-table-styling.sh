#!/bin/bash

echo "=== Table Styling Implementation Verification ==="
echo "Checking Task 5: Redesign table and data display"
echo ""

CSS_FILE="src/main/webapp/resources/css/phonebook.css"

if [ ! -f "$CSS_FILE" ]; then
    echo "❌ CSS file not found: $CSS_FILE"
    exit 1
fi

echo "✅ CSS file found: $CSS_FILE"
echo ""

# Check for enhanced table header styling
echo "🔍 Checking table header styling..."
if grep -q "background-image: linear-gradient" "$CSS_FILE" && grep -q "text-transform: uppercase" "$CSS_FILE"; then
    echo "✅ Enhanced table header styling implemented"
else
    echo "❌ Table header styling missing or incomplete"
fi

# Check for alternating row colors
echo "🔍 Checking alternating row colors..."
if grep -q "nth-child(even)" "$CSS_FILE" && grep -q "nth-child(odd)" "$CSS_FILE"; then
    echo "✅ Alternating row colors implemented"
else
    echo "❌ Alternating row colors missing"
fi

# Check for hover effects
echo "🔍 Checking hover effects..."
if grep -q "transform: translateY(-1px)" "$CSS_FILE" && grep -q "box-shadow.*rgba(37, 99, 235" "$CSS_FILE"; then
    echo "✅ Enhanced hover effects implemented"
else
    echo "❌ Hover effects missing or incomplete"
fi

# Check for empty state styling
echo "🔍 Checking empty state styling..."
if grep -q "ui-datatable-empty-message" "$CSS_FILE" && grep -q "dashed.*border" "$CSS_FILE"; then
    echo "✅ Enhanced empty state styling implemented"
else
    echo "❌ Empty state styling missing"
fi

# Check for ICEfaces datatable overrides
echo "🔍 Checking ICEfaces datatable overrides..."
if grep -q "ui-datatable" "$CSS_FILE" && grep -q "border-radius.*radius-lg" "$CSS_FILE"; then
    echo "✅ ICEfaces datatable overrides implemented"
else
    echo "❌ ICEfaces datatable overrides missing"
fi

# Check for responsive design
echo "🔍 Checking responsive design..."
if grep -q "@media.*max-width.*768px" "$CSS_FILE" && grep -q "font-size.*12px" "$CSS_FILE"; then
    echo "✅ Responsive table design implemented"
else
    echo "❌ Responsive design missing or incomplete"
fi

echo ""
echo "=== Summary ==="
echo "Task 5 implementation includes:"
echo "• Enhanced table header with gradient background and typography"
echo "• Alternating row colors for better readability"
echo "• Subtle hover effects with elevation and color changes"
echo "• Enhanced empty state with icon and dashed border"
echo "• ICEfaces component overrides for consistency"
echo "• Responsive design for mobile devices"
echo ""
echo "🎯 Open test-table-styling.html in your browser to verify visually"
echo "🌐 Or visit: http://localhost:8080/icefaces-phonebook/contacts.xhtml"