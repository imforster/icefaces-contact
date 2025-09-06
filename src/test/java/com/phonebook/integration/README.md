# Integration Tests for ICEfaces Phonebook Application

This directory contains comprehensive integration tests for the ICEfaces Phonebook application using Arquillian framework with WildFly container.

## Test Coverage

### 1. PhonebookIntegrationTest
**Requirements: 1.1, 2.2, 3.2, 4.2, 5.1, 7.1, 7.2**

- **CDI Injection Verification**: Tests that all CDI beans are properly injected
- **Complete CRUD Workflow**: Tests Create, Read, Update, Delete operations end-to-end
- **Search Functionality**: Tests contact search with various criteria
- **Transaction Handling**: Tests database transaction commit and rollback scenarios
- **Concurrent Access**: Tests multiple users accessing the system simultaneously
- **Managed Bean Integration**: Tests integration between JSF managed beans and service layer
- **Database Constraints**: Tests database schema constraints and validation
- **Data Persistence**: Tests that data persists correctly across transactions

### 2. JSFNavigationIntegrationTest
**Requirements: 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 5.4, 7.3**

- **Add Contact Workflow**: Tests complete add contact process through managed bean
- **Edit Contact Workflow**: Tests contact editing including form population and updates
- **Delete Contact Workflow**: Tests contact deletion with confirmation handling
- **Search Workflow**: Tests search functionality through the UI layer
- **Form Validation**: Tests client-side and server-side validation
- **Managed Bean State Management**: Tests proper state handling in view-scoped beans
- **Error Handling**: Tests error scenarios and message display
- **Concurrent Modifications**: Tests handling of concurrent data modifications

### 3. DatabaseTransactionIntegrationTest
**Requirements: 7.1, 7.2**

- **Basic Transaction Operations**: Tests commit and rollback operations
- **Constraint Violation Rollback**: Tests rollback on database constraint violations
- **Validation Error Rollback**: Tests rollback on Bean Validation errors
- **Nested Transaction Behavior**: Tests behavior with nested operations
- **Transaction Isolation**: Tests transaction isolation levels
- **Concurrent Transactions**: Tests multiple simultaneous transactions
- **Transaction Timeout**: Tests transaction timeout handling
- **Connection Recovery**: Tests database connection pool recovery
- **Multi-Entity Rollback**: Tests rollback with multiple entities in one transaction

## Test Architecture

### Arquillian Configuration
- Uses WildFly embedded container for realistic Java EE environment
- Deploys actual WAR files with all dependencies
- Uses H2 database with JTA transactions
- Supports both unit and integration test persistence units

### Test Data Management
- Each test method starts with clean database state
- Uses `@Before` and `@After` methods for setup/cleanup
- Supports both programmatic and declarative transaction management
- Includes helper methods for creating test data

### Deployment Strategy
- Creates minimal WAR deployments with only necessary classes
- Includes all required dependencies via Maven resolver
- Uses proper CDI configuration with beans.xml
- Includes test-specific web.xml configuration

## Running the Tests

### Prerequisites
- Java 8 or higher
- Maven 3.x
- WildFly 26.1.3.Final extracted in project root directory

### Command Line Options

#### Run All Integration Tests
```bash
mvn failsafe:integration-test failsafe:verify
```

#### Run Specific Test Class
```bash
mvn failsafe:integration-test -Dtest=PhonebookIntegrationTest
```

#### Run with Integration Test Profile
```bash
mvn test -Pintegration-tests
```

#### Use the Test Runner Script
```bash
./run-integration-tests.sh
```

### Test Reports
- Surefire reports: `target/surefire-reports/`
- Failsafe reports: `target/failsafe-reports/`
- Arquillian deployments: `target/arquillian-deployments/`

## Test Configuration Files

### arquillian.xml
Configures Arquillian container settings:
- WildFly home directory
- Management ports and addresses
- Deployment export settings

### persistence.xml (test)
Defines persistence units for testing:
- JTA persistence unit for integration tests
- Resource-local persistence unit for unit tests
- H2 database configuration

### test-web.xml
Web application configuration for tests:
- JSF servlet configuration
- ICEfaces settings
- Security constraints (if any)

## Troubleshooting

### Common Issues

1. **WildFly Not Found**
   - Ensure WildFly 26.1.3.Final is extracted in project root
   - Check arquillian.xml configuration

2. **Port Conflicts**
   - Ensure ports 8080, 9990 are available
   - Check for running WildFly instances

3. **Database Issues**
   - H2 database runs in-memory for tests
   - Check persistence.xml configuration
   - Verify JTA datasource availability

4. **CDI Injection Failures**
   - Ensure beans.xml is present in deployment
   - Check CDI bean scopes and annotations
   - Verify proper package scanning

5. **Test Timeouts**
   - Integration tests may take longer due to container startup
   - Increase timeout values if needed
   - Check system resources

### Debug Mode
To run tests in debug mode:
```bash
mvn failsafe:integration-test -Dmaven.failsafe.debug
```

### Verbose Logging
Enable detailed logging by setting:
```bash
-Djava.util.logging.config.file=src/test/resources/logging.properties
```

## Best Practices

1. **Test Isolation**: Each test should be independent and not rely on other tests
2. **Data Cleanup**: Always clean up test data in @After methods
3. **Transaction Management**: Use proper transaction boundaries in tests
4. **Resource Management**: Close resources properly to avoid leaks
5. **Assertions**: Use meaningful assertions with descriptive messages
6. **Test Documentation**: Document complex test scenarios and expected behavior

## Contributing

When adding new integration tests:

1. Follow the existing naming conventions (*IntegrationTest.java)
2. Include proper Arquillian deployment methods
3. Add appropriate requirement references in comments
4. Ensure proper cleanup in @After methods
5. Update this README with new test descriptions
6. Add tests to the PhonebookIntegrationTestSuite if appropriate