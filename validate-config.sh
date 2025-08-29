#!/bin/bash

echo "🔍 Validating ICEfaces Phonebook Configuration"
echo "=============================================="

# Check WAR file exists
if [ -f "target/icefaces-phonebook.war" ]; then
    echo "✅ WAR file exists: $(ls -lh target/icefaces-phonebook.war | awk '{print $5}')"
else
    echo "❌ WAR file not found"
    exit 1
fi

echo ""
echo "📋 WAR file contents:"
jar -tf target/icefaces-phonebook.war | head -20

echo ""
echo "🔧 Configuration files in WAR:"
echo "================================"

# Check web.xml
if jar -tf target/icefaces-phonebook.war | grep -q "WEB-INF/web.xml"; then
    echo "✅ web.xml found"
    echo "   JSF Servlet configuration:"
    jar -xf target/icefaces-phonebook.war WEB-INF/web.xml
    if grep -q "javax.faces.webapp.FacesServlet" WEB-INF/web.xml; then
        echo "   ✅ JSF Servlet configured"
    fi
    if grep -q "org.icefaces" WEB-INF/web.xml; then
        echo "   ✅ ICEfaces parameters configured"
    fi
    rm -rf WEB-INF
else
    echo "❌ web.xml not found"
fi

# Check faces-config.xml
if jar -tf target/icefaces-phonebook.war | grep -q "WEB-INF/faces-config.xml"; then
    echo "✅ faces-config.xml found"
    jar -xf target/icefaces-phonebook.war WEB-INF/faces-config.xml
    if grep -q "org.icefaces" WEB-INF/faces-config.xml; then
        echo "   ✅ ICEfaces factories configured"
    fi
    rm -rf WEB-INF
else
    echo "❌ faces-config.xml not found"
fi

# Check messages.properties
if jar -tf target/icefaces-phonebook.war | grep -q "WEB-INF/classes/messages.properties"; then
    echo "✅ messages.properties found"
else
    echo "❌ messages.properties not found"
fi

# Check CSS resources
if jar -tf target/icefaces-phonebook.war | grep -q "resources/css/phonebook.css"; then
    echo "✅ CSS resources found"
else
    echo "❌ CSS resources not found"
fi

# Check XHTML files
if jar -tf target/icefaces-phonebook.war | grep -q "index.xhtml"; then
    echo "✅ index.xhtml found"
else
    echo "❌ index.xhtml not found"
fi

echo ""
echo "📚 Dependencies in WAR:"
echo "======================="
jar -tf target/icefaces-phonebook.war | grep "WEB-INF/lib" | grep -E "(icefaces|jsf)" | head -10

echo ""
echo "🎯 Key Configuration Validation:"
echo "================================"

# Extract and validate web.xml
jar -xf target/icefaces-phonebook.war WEB-INF/web.xml
echo "JSF Servlet mapping:"
grep -A 3 -B 1 "servlet-mapping" WEB-INF/web.xml | grep -E "(servlet-name|url-pattern)"

echo ""
echo "ICEfaces theme configuration:"
grep -A 1 "org.icefaces.ace.theme" WEB-INF/web.xml

echo ""
echo "Welcome file:"
grep -A 2 -B 1 "welcome-file" WEB-INF/web.xml

# Cleanup
rm -rf WEB-INF META-INF

echo ""
echo "✅ Configuration validation complete!"
echo ""
echo "🚀 Ready for deployment to WildFly 26.1.3.Final"
echo "📋 Application context: /icefaces-phonebook"
echo "🌐 Expected URL: http://localhost:8080/icefaces-phonebook/"