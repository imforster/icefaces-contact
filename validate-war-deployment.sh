#!/bin/bash

# Validate ICEfaces Phonebook WAR file for WildFly deployment

set -e

WAR_FILE="target/icefaces-phonebook.war"
TEMP_DIR="/tmp/phonebook-validation"

echo "=== ICEfaces Phonebook WAR Validation ==="
echo "WAR File: $WAR_FILE"

# Check if WAR file exists
if [ ! -f "$WAR_FILE" ]; then
    echo "ERROR: WAR file not found. Building..."
    mvn clean package -DskipTests
fi

# Create temporary directory for extraction
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"

# Extract WAR file
echo "1. Extracting WAR file..."
ORIGINAL_DIR=$(pwd)
cd "$TEMP_DIR"
jar -xf "$ORIGINAL_DIR/$WAR_FILE"
cd "$ORIGINAL_DIR"

# Validate WildFly configuration files
echo "2. Validating WildFly configuration files..."

if [ -f "$TEMP_DIR/WEB-INF/jboss-web.xml" ]; then
    echo "✓ jboss-web.xml found"
else
    echo "✗ jboss-web.xml missing"
fi

if [ -f "$TEMP_DIR/WEB-INF/jboss-deployment-structure.xml" ]; then
    echo "✓ jboss-deployment-structure.xml found"
else
    echo "✗ jboss-deployment-structure.xml missing"
fi

# Validate web.xml
echo "3. Validating web.xml configuration..."
if grep -q "javax.faces.webapp.FacesServlet" "$TEMP_DIR/WEB-INF/web.xml"; then
    echo "✓ JSF servlet configured"
else
    echo "✗ JSF servlet not configured"
fi

if grep -q "org.icefaces.ace.theme" "$TEMP_DIR/WEB-INF/web.xml"; then
    echo "✓ ICEfaces theme configured"
else
    echo "✗ ICEfaces theme not configured"
fi

# Validate persistence.xml
echo "4. Validating persistence configuration..."
if [ -f "$TEMP_DIR/WEB-INF/classes/META-INF/persistence.xml" ]; then
    echo "✓ persistence.xml found"
    
    if grep -q "java:jboss/datasources/PhonebookDS" "$TEMP_DIR/WEB-INF/classes/META-INF/persistence.xml"; then
        echo "✓ WildFly datasource configured"
    else
        echo "✗ WildFly datasource not configured"
    fi
    
    if grep -q "org.hibernate.dialect.H2Dialect" "$TEMP_DIR/WEB-INF/classes/META-INF/persistence.xml"; then
        echo "✓ H2 dialect configured"
    else
        echo "✗ H2 dialect not configured"
    fi
else
    echo "✗ persistence.xml missing"
fi

# Validate ICEfaces dependencies
echo "5. Validating ICEfaces dependencies..."
if [ -f "$TEMP_DIR/WEB-INF/lib/icefaces-4.3.0.jar" ]; then
    echo "✓ ICEfaces core library included"
else
    echo "✗ ICEfaces core library missing"
fi

if [ -f "$TEMP_DIR/WEB-INF/lib/icefaces-ace-4.3.0.jar" ]; then
    echo "✓ ICEfaces ACE components included"
else
    echo "✗ ICEfaces ACE components missing"
fi

# Validate H2 database
echo "6. Validating H2 database dependency..."
if [ -f "$TEMP_DIR/WEB-INF/lib/h2-1.4.200.jar" ]; then
    echo "✓ H2 database library included"
else
    echo "✗ H2 database library missing"
fi

# Validate application classes
echo "7. Validating application classes..."
CLASSES_DIR="$TEMP_DIR/WEB-INF/classes/com/phonebook"

if [ -f "$CLASSES_DIR/entity/Contact.class" ]; then
    echo "✓ Contact entity class found"
else
    echo "✗ Contact entity class missing"
fi

if [ -f "$CLASSES_DIR/bean/ContactBean.class" ]; then
    echo "✓ ContactBean managed bean found"
else
    echo "✗ ContactBean managed bean missing"
fi

if [ -f "$CLASSES_DIR/service/ContactService.class" ]; then
    echo "✓ ContactService found"
else
    echo "✗ ContactService missing"
fi

if [ -f "$CLASSES_DIR/repository/ContactRepositoryImpl.class" ]; then
    echo "✓ ContactRepository implementation found"
else
    echo "✗ ContactRepository implementation missing"
fi

# Validate XHTML pages
echo "8. Validating XHTML pages..."
if [ -f "$TEMP_DIR/contacts.xhtml" ]; then
    echo "✓ Main contacts.xhtml page found"
    
    if grep -q "ace:dataTable" "$TEMP_DIR/contacts.xhtml"; then
        echo "✓ ICEfaces ACE components used"
    else
        echo "✗ ICEfaces ACE components not found"
    fi
else
    echo "✗ Main contacts.xhtml page missing"
fi

# Validate initial data script
echo "9. Validating database initialization..."
if [ -f "$TEMP_DIR/WEB-INF/classes/META-INF/initial-data.sql" ]; then
    echo "✓ Initial data script found"
else
    echo "✗ Initial data script missing"
fi

# Validate CSS resources
echo "10. Validating CSS resources..."
if [ -f "$TEMP_DIR/resources/css/phonebook.css" ]; then
    echo "✓ Custom CSS found"
else
    echo "✗ Custom CSS missing"
fi

# Check WAR file size
echo "11. Checking WAR file size..."
WAR_SIZE=$(du -h "$WAR_FILE" | cut -f1)
echo "WAR file size: $WAR_SIZE"

# List all JAR dependencies
echo "12. JAR dependencies included:"
ls -la "$TEMP_DIR/WEB-INF/lib/" | grep "\.jar$" | awk '{print "   " $9 " (" $5 " bytes)"}'

# Cleanup
rm -rf "$TEMP_DIR"

echo ""
echo "=== Validation Summary ==="
echo "WAR file: $WAR_FILE"
echo "Size: $WAR_SIZE"
echo ""
echo "✓ WAR file validation completed!"
echo ""
echo "Ready for WildFly deployment with Java 8 runtime."
echo "Use: ./deploy-to-wildfly.sh (with Java 8)"