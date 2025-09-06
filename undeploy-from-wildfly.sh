#!/bin/bash

# Undeploy ICEfaces Phonebook Application from WildFly

set -e

# Configuration
WILDFLY_HOME="${WILDFLY_HOME:-./wildfly-26.1.3.Final}"
APP_NAME="icefaces-phonebook"

echo "=== ICEfaces Phonebook Undeployment Script ==="
echo "WildFly Home: $WILDFLY_HOME"
echo "Application: $APP_NAME"

# Check if WildFly exists
if [ ! -d "$WILDFLY_HOME" ]; then
    echo "ERROR: WildFly not found at $WILDFLY_HOME"
    exit 1
fi

# Check if WildFly is running
if ! $WILDFLY_HOME/bin/jboss-cli.sh --connect --command=":read-attribute(name=server-state)" 2>/dev/null | grep -q "running"; then
    echo "ERROR: WildFly is not running"
    exit 1
fi

# Undeploy the application
echo "Undeploying application..."
if $WILDFLY_HOME/bin/jboss-cli.sh --connect --command="undeploy ${APP_NAME}.war" 2>/dev/null; then
    echo "Application undeployed successfully"
else
    echo "Application was not deployed or undeploy failed"
fi

# Optionally remove datasource (uncomment if needed)
# echo "Removing datasource..."
# $WILDFLY_HOME/bin/jboss-cli.sh --connect --command="data-source remove --name=PhonebookDS"

echo "Undeployment completed!"