#!/bin/bash

# Verify ICEfaces Phonebook Application deployment on WildFly

set -e

# Configuration
WILDFLY_HOME="${WILDFLY_HOME:-./wildfly-26.1.3.Final}"
APP_NAME="icefaces-phonebook"
APP_URL="http://localhost:8080/phonebook"
ADMIN_URL="http://localhost:9990"

echo "=== ICEfaces Phonebook Deployment Verification ==="
echo "WildFly Home: $WILDFLY_HOME"
echo "Application: $APP_NAME"
echo "Application URL: $APP_URL"

# Check if WildFly is running
echo "1. Checking WildFly server status..."
if $WILDFLY_HOME/bin/jboss-cli.sh --connect --command=":read-attribute(name=server-state)" 2>/dev/null | grep -q "running"; then
    echo "✓ WildFly server is running"
else
    echo "✗ WildFly server is not running"
    exit 1
fi

# Check deployment status
echo "2. Checking application deployment status..."
if $WILDFLY_HOME/bin/jboss-cli.sh --connect --command="deployment-info --name=${APP_NAME}.war" 2>/dev/null | grep -q "OK"; then
    echo "✓ Application is deployed successfully"
else
    echo "✗ Application deployment failed or not found"
    exit 1
fi

# Check datasource
echo "3. Checking datasource configuration..."
if $WILDFLY_HOME/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=PhonebookDS:test-connection-in-pool" 2>/dev/null | grep -q "success"; then
    echo "✓ Datasource connection is working"
else
    echo "✗ Datasource connection failed"
    exit 1
fi

# Check H2 database
echo "4. Checking H2 database initialization..."
DB_FILE="$HOME/phonebook.mv.db"
if [ -f "$DB_FILE" ]; then
    echo "✓ H2 database file exists at $DB_FILE"
else
    echo "! H2 database file not found (will be created on first access)"
fi

# Test HTTP connectivity
echo "5. Testing HTTP connectivity..."
if command -v curl >/dev/null 2>&1; then
    if curl -s -o /dev/null -w "%{http_code}" "$APP_URL" | grep -q "200"; then
        echo "✓ Application is accessible at $APP_URL"
    else
        echo "✗ Application is not accessible at $APP_URL"
        echo "  Check if the application started correctly"
    fi
else
    echo "! curl not available, skipping HTTP test"
    echo "  Manually verify: $APP_URL"
fi

# Check Java version
echo "6. Checking Java version compatibility..."
JAVA_VERSION=$($WILDFLY_HOME/bin/jboss-cli.sh --connect --command=":read-attribute(name=java-version)" 2>/dev/null | grep -o '"[^"]*"' | tr -d '"')
if [[ "$JAVA_VERSION" == 1.8* ]]; then
    echo "✓ Java 8 compatibility confirmed: $JAVA_VERSION"
else
    echo "! Java version: $JAVA_VERSION (expected 1.8.x)"
fi

# Check ICEfaces resources
echo "7. Checking ICEfaces resources..."
if curl -s -o /dev/null -w "%{http_code}" "$APP_URL/javax.faces.resource/icefaces.js.xhtml?ln=ice.core" | grep -q "200"; then
    echo "✓ ICEfaces resources are accessible"
else
    echo "! ICEfaces resources may not be properly loaded"
fi

echo ""
echo "=== Verification Summary ==="
echo "Application URL: $APP_URL"
echo "Admin Console: $ADMIN_URL"
echo "Database Location: $HOME/phonebook.mv.db"
echo ""
echo "To view logs: tail -f $WILDFLY_HOME/standalone/log/server.log"
echo "To undeploy: ./undeploy-from-wildfly.sh"
echo ""
echo "✓ Deployment verification completed successfully!"