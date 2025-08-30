#!/bin/bash

# Deploy ICEfaces Phonebook Application to WildFly
# This script sets up the H2 datasource and deploys the application

set -e

# Configuration
WILDFLY_HOME="${WILDFLY_HOME:-./wildfly-26.1.3.Final}"
APP_NAME="icefaces-phonebook"
WAR_FILE="target/${APP_NAME}.war"
H2_JAR_VERSION="1.4.200"

echo "=== ICEfaces Phonebook Deployment Script ==="
echo "WildFly Home: $WILDFLY_HOME"
echo "Application: $APP_NAME"
echo "WAR File: $WAR_FILE"

# Check if WildFly exists
if [ ! -d "$WILDFLY_HOME" ]; then
    echo "ERROR: WildFly not found at $WILDFLY_HOME"
    echo "Please set WILDFLY_HOME environment variable or ensure WildFly is extracted in current directory"
    exit 1
fi

# Check if WildFly is running
echo "Checking if WildFly is running..."
if ! $WILDFLY_HOME/bin/jboss-cli.sh --connect --command=":read-attribute(name=server-state)" 2>/dev/null | grep -q "running"; then
    echo "Starting WildFly server..."
    $WILDFLY_HOME/bin/standalone.sh &
    WILDFLY_PID=$!
    
    # Wait for WildFly to start
    echo "Waiting for WildFly to start..."
    for i in {1..30}; do
        if $WILDFLY_HOME/bin/jboss-cli.sh --connect --command=":read-attribute(name=server-state)" 2>/dev/null | grep -q "running"; then
            echo "WildFly started successfully"
            break
        fi
        echo "Waiting... ($i/30)"
        sleep 2
    done
    
    if [ $i -eq 30 ]; then
        echo "ERROR: WildFly failed to start within 60 seconds"
        exit 1
    fi
else
    echo "WildFly is already running"
fi

# Setup H2 module if not exists
H2_MODULE_DIR="$WILDFLY_HOME/modules/system/layers/base/com/h2database/h2/main"
if [ ! -d "$H2_MODULE_DIR" ]; then
    echo "Setting up H2 database module..."
    mkdir -p "$H2_MODULE_DIR"
    
    # Copy H2 JAR from Maven repository
    H2_JAR="$HOME/.m2/repository/com/h2database/h2/$H2_JAR_VERSION/h2-$H2_JAR_VERSION.jar"
    if [ ! -f "$H2_JAR" ]; then
        echo "H2 JAR not found in Maven repository. Building project first..."
        mvn dependency:resolve
    fi
    
    cp "$H2_JAR" "$H2_MODULE_DIR/"
    cp "src/main/resources/h2-module.xml" "$H2_MODULE_DIR/module.xml"
    echo "H2 module setup completed"
else
    echo "H2 module already exists"
fi

# Build the application
echo "Building application..."
mvn clean package -DskipTests

# Check if WAR file exists
if [ ! -f "$WAR_FILE" ]; then
    echo "ERROR: WAR file not found at $WAR_FILE"
    echo "Build may have failed"
    exit 1
fi

# Configure datasource
echo "Configuring H2 datasource..."
if $WILDFLY_HOME/bin/jboss-cli.sh --connect --command="/subsystem=datasources/data-source=PhonebookDS:read-resource" 2>/dev/null | grep -q 'success'; then
    echo "Datasource PhonebookDS already exists."
else
    echo "Configuring H2 datasource..."
    $WILDFLY_HOME/bin/jboss-cli.sh --connect --file=src/main/resources/wildfly-datasource.cli
fi

# Deploy the application
echo "Deploying application..."
$WILDFLY_HOME/bin/jboss-cli.sh --connect --command="deploy $WAR_FILE --force"

# Verify deployment
echo "Verifying deployment..."
if $WILDFLY_HOME/bin/jboss-cli.sh --connect --command="deployment-info --name=${APP_NAME}.war" | grep -q "OK"; then
    echo "=== Deployment Successful ==="
    echo "Application URL: http://localhost:8080/phonebook"
    echo "Admin Console: http://localhost:9990"
    echo ""
    echo "To undeploy: $WILDFLY_HOME/bin/jboss-cli.sh --connect --command=\"undeploy ${APP_NAME}.war\""
else
    echo "ERROR: Deployment verification failed"
    exit 1
fi

echo "Deployment completed successfully!"