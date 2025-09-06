# Integration Tests Setup and Execution Guide

This document provides comprehensive instructions for setting up and running the integration tests for the ICEfaces Phonebook application.

## Overview

The integration tests are built using Arquillian framework with WildFly container support. They provide end-to-end testing of:

- Complete CRUD workflows with H2 database
- Database transaction handling and rollback scenarios  
- CDI injection verification
- JSF navigation flows
- Concurrent access patterns
- Error handling and recovery

## Prerequisites

### Required Software
- Java 8 or higher
- Maven 3.x
- WildFly 26.1.3.Final (extracted in project root)

### Project Structure
```
icefaces-phonebook/
├── wildfly-26.1.3.Final/          # WildFly server installation
├── src/test/java/com/phonebook/integration/
│   ├── PhonebookIntegrationTest.java
│   ├── JSFNavigationIntegrationTest.java
│   ├── DatabaseTransactionIntegrationTest.java
│   └── PhonebookIntegrationTestSuite.java
├── src/test/resources/
│   ├── arquillian.xml              # Arquillian configuration
│   └── META-INF/persistence.xml   # Test persistence configuration
└── run-integration-tests.sh       # Test execution script
```

## Test Classes

### 1. PhonebookIntegrationTest
**Requirements: 1.1, 2.2, 3.2, 4.2, 5.1, 7.1, 7.2**

Tests comprehensive application functionality:
- CDI injection verification
- Complete CRUD workflows
- Search functionality
- Transaction handling
- Concurrent access
- Managed bean integration
- Database constraints
- Data persistence

### 2. JSFNavigationIntegrationTest  
**Requirements: 2.1-2.4, 3.1-3.4, 4.1-4.4, 5.1-5.4, 7.3**

Tests JSF layer integration:
- Add contact workflow
- Edit contact workflow  
- Delete contact workflow
- Search workflow
- Form validation
- Managed bean state management
- Error handling
- Concurrent modifications

### 3. DatabaseTransactionIntegrationTest
**Requirements: 7.1, 7.2**

Tests database transaction scenarios:
- Basic transaction commit/rollback
- Constraint violation rollback
- Validation error rollback
- Nested transaction behavior
- Transaction isolation
- Concurrent transactions
- Connection recovery
- Multi-entity rollback

## Configuration Files

### arquillian.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<arquillian xmlns="http://jboss.org/schema/arquillian"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://jboss.org/schema/arquillian
                http://jboss.org/schema/arquillian/arquillian_1_0.xsd">

    <engine>
        <property name="deploymentExportPath">target/arquillian-deployments</property>
    </engine>

    <container qualifier="wildfly-managed" default="true">
        <configuration>
            <property name="jbossHome">wildfly-26.1.3.Final</property>
            <property name="serverConfig">standalone.xml</property>
            <property name="allowConnectingToRunningServer">true</property>
            <property name="managementAddress">127.0.0.1</property>
            <property name="managementPort">9990</property>
            <property name="startupTimeoutInSeconds">120</property>
            <property name="jbossArguments">-Djboss.socket.binding.port-offset=100</property>
        </configuration>
    </container>

</arquillian>
```

### Test persistence.xml
```xml
<persistence-unit name="phonebook-test" transaction-type="JTA">
    <jta-data-source>java:jboss/datasources/ExampleDS</jta-data-source>
    <class>com.phonebook.entity.Contact</class>
    <properties>
        <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
        <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
        <property name="hibernate.show_sql" value="false"/>
        <property name="hibernate.format_sql" value="false"/>
        <property name="hibernate.transaction.jta.platform" 
                  value="org.hibernate.service.jta.platform.internal.JBossAppServerJtaPlatform"/>
    </properties>
</persistence-unit>
```

## Running the Tests

### Method 1: Using the Test Script
```bash
./run-integration-tests.sh
```

### Method 2: Using Maven Commands

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

### Method 3: Using Maven Surefire (for development)
```bash
mvn test -Dtest=PhonebookIntegrationTestSuite
```

## Test Execution Flow

1. **Container Startup**: Arquillian starts WildFly container
2. **Deployment**: Test WAR is deployed with all dependencies
3. **Test Execution**: Each test method runs in container context
4. **Cleanup**: Database is cleaned between tests
5. **Container Shutdown**: WildFly container is stopped

## Expected Test Results

### Successful Execution
```
================================================
✅ All integration tests passed successfully!
================================================

Test Coverage Summary:
- ✅ CRUD operations (Create, Read, Update, Delete)
- ✅ Database transaction handling
- ✅ Transaction rollback scenarios
- ✅ CDI dependency injection
- ✅ JSF managed bean integration
- ✅ Search functionality
- ✅ Concurrent access handling
- ✅ Error handling and recovery
- ✅ Data persistence verification
```

### Test Reports
- Surefire reports: `target/surefire-reports/`
- Failsafe reports: `target/failsafe-reports/`
- Arquillian deployments: `target/arquillian-deployments/`

## Troubleshooting

### Common Issues

#### 1. WildFly Not Found
```
Error: WildFly directory not found
```
**Solution**: Ensure WildFly 26.1.3.Final is extracted in project root

#### 2. Port Conflicts
```
The port 9990 is already in use
```
**Solution**: Stop running WildFly instances or use port offset:
```bash
mvn test -Djboss.socket.binding.port-offset=100
```

#### 3. Database Issues
```
Could not create connection to database server
```
**Solution**: Check H2 database configuration and JTA datasource

#### 4. CDI Injection Failures
```
Unsatisfied dependencies for type ContactService
```
**Solution**: Ensure beans.xml is present in test deployment

#### 5. Test Timeouts
```
Test timed out after 120 seconds
```
**Solution**: Increase timeout or check system resources:
```xml
<property name="startupTimeoutInSeconds">300</property>
```

### Debug Mode
```bash
mvn failsafe:integration-test -Dmaven.failsafe.debug
```

### Verbose Logging
```bash
mvn test -Djava.util.logging.config.file=src/test/resources/logging.properties
```

## Performance Considerations

- **Container Startup**: ~30-60 seconds
- **Test Execution**: ~2-5 minutes for full suite
- **Memory Usage**: ~512MB-1GB for WildFly container
- **Disk Space**: ~200MB for temporary files

## Continuous Integration

### Jenkins Pipeline Example
```groovy
pipeline {
    agent any
    stages {
        stage('Integration Tests') {
            steps {
                sh 'mvn clean compile test-compile'
                sh './run-integration-tests.sh'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/failsafe-reports/*.xml'
                }
            }
        }
    }
}
```

### GitHub Actions Example
```yaml
name: Integration Tests
on: [push, pull_request]
jobs:
  integration-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 8
        uses: actions/setup-java@v2
        with:
          java-version: '8'
          distribution: 'adopt'
      - name: Run Integration Tests
        run: ./run-integration-tests.sh
```

## Best Practices

1. **Test Isolation**: Each test starts with clean database state
2. **Resource Cleanup**: Always clean up resources in @After methods
3. **Transaction Management**: Use proper transaction boundaries
4. **Meaningful Assertions**: Include descriptive assertion messages
5. **Error Scenarios**: Test both success and failure paths
6. **Performance**: Monitor test execution times
7. **Documentation**: Keep test documentation up to date

## Contributing

When adding new integration tests:

1. Follow existing naming conventions (*IntegrationTest.java)
2. Include proper Arquillian deployment methods
3. Add requirement references in comments
4. Ensure proper cleanup in @After methods
5. Update this documentation
6. Add tests to PhonebookIntegrationTestSuite if appropriate

## Support

For issues with integration tests:

1. Check WildFly logs: `wildfly-26.1.3.Final/standalone/log/server.log`
2. Review test reports in `target/failsafe-reports/`
3. Enable debug logging for detailed output
4. Verify all prerequisites are met
5. Check for port conflicts and resource constraints