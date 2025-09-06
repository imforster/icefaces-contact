#!/bin/bash

echo "=== Search Container Styling Verification ==="
echo ""

# Check if the CSS file exists and contains the required styles
CSS_FILE="src/main/webapp/resources/css/phonebook.css"

if [ ! -f "$CSS_FILE" ]; then
    echo "❌ CSS file not found: $CSS_FILE"
    exit 1
fi

echo "✅ CSS file found: $CSS_FILE"
echo ""

# Check for search-container styles
echo "🔍 Checking search-container styles..."
if grep -q "\.search-container" "$CSS_FILE"; then
    echo "✅ search-container class found"
    
    # Check for surface color and gradient
    if grep -A 10 "\.search-container" "$CSS_FILE" | grep -q "background-image.*gradient"; then
        echo "✅ Surface color with gradient background implemented"
    else
        echo "❌ Surface color gradient not found"
    fi
    
    # Check for enhanced shadow
    if grep -A 10 "\.search-container" "$CSS_FILE" | grep -q "shadow-md"; then
        echo "✅ Enhanced shadow (shadow-md) implemented"
    else
        echo "❌ Enhanced shadow not found"
    fi
else
    echo "❌ search-container class not found"
fi

echo ""

# Check for search-input styles
echo "🔍 Checking search-input styles..."
if grep -q "\.search-input" "$CSS_FILE"; then
    echo "✅ search-input class found"
    
    # Check if it's included in form input styling
    if grep -B 5 -A 5 "\.search-input" "$CSS_FILE" | grep -q "form-input"; then
        echo "✅ Search input matches form input styling"
    else
        echo "❌ Search input form consistency not found"
    fi
    
    # Check for enhanced focus states
    if grep -A 5 "\.search-input:focus" "$CSS_FILE" | grep -q "transform.*translateY"; then
        echo "✅ Enhanced focus transform effects implemented"
    else
        echo "❌ Enhanced focus effects not found"
    fi
else
    echo "❌ search-input class not found"
fi

echo ""

# Check for container card enhancements
echo "🔍 Checking container card enhancements..."
if grep -q "\.container" "$CSS_FILE"; then
    echo "✅ container class found"
    
    # Check for enhanced shadow
    if grep -A 10 "\.container" "$CSS_FILE" | grep -q "shadow-lg"; then
        echo "✅ Enhanced container shadow (shadow-lg) implemented"
    else
        echo "❌ Enhanced container shadow not found"
    fi
    
    # Check for enhanced border radius
    if grep -A 10 "\.container" "$CSS_FILE" | grep -q "radius-xl"; then
        echo "✅ Enhanced border radius (radius-xl) implemented"
    else
        echo "❌ Enhanced border radius not found"
    fi
else
    echo "❌ container class not found"
fi

echo ""

# Check for search-panel styles
echo "🔍 Checking search-panel styles..."
if grep -q "\.search-panel" "$CSS_FILE"; then
    echo "✅ search-panel class found"
    
    # Check for enhanced spacing
    if grep -A 10 "\.search-panel" "$CSS_FILE" | grep -q "spacing-md"; then
        echo "✅ Enhanced panel spacing implemented"
    else
        echo "❌ Enhanced panel spacing not found"
    fi
else
    echo "❌ search-panel class not found"
fi

echo ""

# Check for search-label styles
echo "🔍 Checking search-label styles..."
if grep -q "\.search-label" "$CSS_FILE"; then
    echo "✅ search-label class found"
    
    # Check for enhanced typography
    if grep -A 10 "\.search-label" "$CSS_FILE" | grep -q "font-weight-semibold"; then
        echo "✅ Enhanced label typography implemented"
    else
        echo "❌ Enhanced label typography not found"
    fi
else
    echo "❌ search-label class not found"
fi

echo ""
echo "=== Verification Complete ==="

# Count total checks
TOTAL_CHECKS=8
PASSED_CHECKS=$(grep -c "✅" <<< "$(bash $0 2>/dev/null)" || echo "0")

echo ""
echo "Summary: $PASSED_CHECKS/$TOTAL_CHECKS checks passed"

if [ "$PASSED_CHECKS" -eq "$TOTAL_CHECKS" ]; then
    echo "🎉 All styling requirements implemented successfully!"
    exit 0
else
    echo "⚠️  Some styling requirements may need attention"
    exit 1
fi