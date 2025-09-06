#!/bin/bash

# Integration Test Runner for ICEfaces Phonebook Application
# This script runs the comprehensive integration tests using Arquillian and WildFly

echo "Starting ICEfaces Phonebook Integration Tests..."
echo "================================================"

# Check if WildFly is available
if [ ! -d "wildfly-26.1.3.Final" ]; then
    echo "Error: WildFly directory not found. Please ensure WildFly 26.1.3.Final is extracted in the project root."
    exit 1
fi

# Set JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    echo "Warning: JAVA_HOME not set. Using default Java installation."
fi

# Clean and compile the project
echo "Cleaning and compiling project..."
mvn clean compile test-compile

if [ $? -ne 0 ]; then
    echo "Error: Compilation failed"
    exit 1
fi

# Run integration tests
echo "Running integration tests..."
echo "This may take several minutes as it involves:"
echo "- Starting embedded WildFly container"
echo "- Deploying test applications"
echo "- Running CRUD workflow tests"
echo "- Testing database transactions"
echo "- Verifying CDI injection"
echo "- Testing JSF navigation flows"
echo ""

mvn failsafe:integration-test failsafe:verify -Dtest=PhonebookIntegrationTestSuite

TEST_RESULT=$?

if [ $TEST_RESULT -eq 0 ]; then
    echo ""
    echo "================================================"
    echo "✅ All integration tests passed successfully!"
    echo "================================================"
    echo ""
    echo "Test Coverage Summary:"
    echo "- ✅ CRUD operations (Create, Read, Update, Delete)"
    echo "- ✅ Database transaction handling"
    echo "- ✅ Transaction rollback scenarios"
    echo "- ✅ CDI dependency injection"
    echo "- ✅ JSF managed bean integration"
    echo "- ✅ Search functionality"
    echo "- ✅ Concurrent access handling"
    echo "- ✅ Error handling and recovery"
    echo "- ✅ Data persistence verification"
    echo ""
else
    echo ""
    echo "================================================"
    echo "❌ Integration tests failed!"
    echo "================================================"
    echo ""
    echo "Please check the test output above for details."
    echo "Common issues:"
    echo "- WildFly not properly configured"
    echo "- Database connection issues"
    echo "- Missing dependencies"
    echo "- Port conflicts"
    echo ""
    echo "Check target/failsafe-reports/ for detailed test reports."
fi

exit $TEST_RESULT