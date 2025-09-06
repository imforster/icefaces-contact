package com.phonebook.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Integration test suite that runs all integration tests for the Phonebook application.
 * This suite covers:
 * - Complete CRUD workflows
 * - Database transaction handling and rollback scenarios
 * - CDI injection verification
 * - JSF navigation flows
 * - Concurrent access patterns
 * - Error handling and recovery
 * 
 * Requirements covered: 1.1, 2.2, 3.2, 4.2, 5.1, 7.1, 7.2
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    PhonebookIntegrationTest.class,
    JSFNavigationIntegrationTest.class,
    DatabaseTransactionIntegrationTest.class
})
public class PhonebookIntegrationTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}