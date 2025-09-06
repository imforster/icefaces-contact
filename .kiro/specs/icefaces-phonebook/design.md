# Design Document

## Overview

The ICEfaces Phone Book application is a Java EE web application that provides a user-friendly interface for managing personal contacts. Built using ICEfaces 4 framework, it leverages JSF (JavaServer Faces) for the presentation layer, JPA (Java Persistence API) for data persistence, and H2 database for storage. The application follows the Model-View-Controller (MVC) pattern and is designed to be deployed on WildFly application server with Java 8 compatibility.

## Architecture

The application follows a layered architecture pattern:

```
┌─────────────────────────────────────┐
│           Presentation Layer        │
│        (ICEfaces 4 + JSF)          │
├─────────────────────────────────────┤
│           Business Layer            │
│        (Managed Beans + CDI)       │
├─────────────────────────────────────┤
│         Persistence Layer           │
│           (JPA + Hibernate)         │
├─────────────────────────────────────┤
│           Database Layer            │
│              (H2 Database)          │
└─────────────────────────────────────┘
```

### Technology Stack
- **Frontend**: ICEfaces 4.3.0 with JSF 2.2
- **Backend**: Java 8, CDI 1.2, JPA 2.1
- **Database**: H2 Database (embedded mode)
- **Application Server**: WildFly 10.x (Java EE 7 compatible)
- **Build Tool**: Maven 3.x

## Components and Interfaces

### 1. Data Model Components

#### Contact Entity
```java
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(length = 100)
    private String email;
    
    // Constructors, getters, setters, equals, hashCode
}
```

### 2. Persistence Layer Components

#### ContactRepository Interface
```java
public interface ContactRepository {
    List<Contact> findAll();
    Contact findById(Long id);
    Contact save(Contact contact);
    void delete(Long id);
    List<Contact> findByNameContaining(String searchTerm);
}
```

#### ContactRepositoryImpl
- Implements CRUD operations using JPA EntityManager
- Handles database transactions
- Provides search functionality

### 3. Business Layer Components

#### ContactService
```java
@Stateless
public class ContactService {
    @Inject
    private ContactRepository contactRepository;
    
    public List<Contact> getAllContacts();
    public Contact saveContact(Contact contact);
    public Contact updateContact(Contact contact);
    public void deleteContact(Long id);
    public List<Contact> searchContacts(String searchTerm);
}
```

### 4. Presentation Layer Components

#### ContactBean (Managed Bean)
```java
@Named
@ViewScoped
public class ContactBean implements Serializable {
    @Inject
    private ContactService contactService;
    
    private List<Contact> contacts;
    private Contact selectedContact;
    private String searchTerm;
    private boolean showAddForm;
    private boolean showEditForm;
    
    // Action methods for CRUD operations
    // Event handlers for UI interactions
}
```

#### Main XHTML Page Structure
- **contacts.xhtml**: Main page with contact list and forms
- Uses ICEfaces components: `ace:dataTable`, `ace:dialog`, `ace:inputText`
- Implements responsive design with ICEfaces themes

## Data Models

### Contact Data Model
```
Contact
├── id: Long (Primary Key, Auto-generated)
├── name: String (Required, Max 100 chars)
├── phoneNumber: String (Required, Max 20 chars)
└── email: String (Optional, Max 100 chars)
```

### Database Schema
```sql
CREATE TABLE contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100)
);

CREATE INDEX idx_contact_name ON contacts(name);
```

### JPA Configuration
- **persistence.xml**: Configured for H2 database with automatic schema generation
- **Connection URL**: `jdbc:h2:~/phonebook;AUTO_SERVER=TRUE`
- **Hibernate Dialect**: `org.hibernate.dialect.H2Dialect`
- **Schema Generation**: `create-drop` for development, `update` for production

## Error Handling

### Validation Strategy
1. **Client-side Validation**: ICEfaces validators for immediate feedback
   - Required field validation
   - Email format validation
   - Phone number format validation

2. **Server-side Validation**: Bean Validation (JSR-303)
   - `@NotNull`, `@NotEmpty` annotations
   - `@Email` for email validation
   - Custom validators for phone number format

3. **Database Constraint Validation**: JPA/Hibernate level
   - Unique constraints
   - Length constraints
   - Not-null constraints

### Exception Handling
```java
@ApplicationScoped
public class GlobalExceptionHandler {
    public void handleDatabaseException(DatabaseException ex);
    public void handleValidationException(ValidationException ex);
    public void handleGenericException(Exception ex);
}
```

### Error Display Strategy
- Use ICEfaces `ace:growl` component for user notifications
- Categorize messages: INFO, WARN, ERROR
- Provide contextual error messages for validation failures
- Log detailed error information for debugging

## Testing Strategy

### Unit Testing
- **JUnit 4**: Core testing framework (Java 8 compatible)
- **Mockito**: Mock dependencies for isolated testing
- **Test Coverage**: Minimum 80% code coverage
- **Target Classes**: 
  - ContactService business logic
  - ContactRepository data access
  - Validation logic

### Integration Testing
- **Arquillian**: For Java EE container testing
- **H2 In-Memory Database**: For database integration tests
- **Test Scenarios**:
  - CRUD operations end-to-end
  - Search functionality
  - Validation workflows
  - Database transaction handling

### UI Testing
- **Selenium WebDriver**: For automated UI testing
- **Test Scenarios**:
  - Contact list display
  - Add/Edit/Delete workflows
  - Search functionality
  - Form validation messages
  - Responsive design verification

### Performance Testing
- **JMeter**: Load testing for concurrent users
- **Metrics**: Response time, throughput, resource utilization
- **Scenarios**: 
  - Multiple users adding contacts simultaneously
  - Large dataset search operations
  - Database connection pool stress testing

## Deployment Configuration

### WildFly Configuration
- **Datasource Configuration**: H2 datasource in standalone.xml
- **JPA Provider**: Hibernate (included in WildFly)
- **CDI**: Enabled by default in Java EE 7
- **ICEfaces Libraries**: Bundled in WAR file

### Maven Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>org.icefaces</groupId>
        <artifactId>icefaces</artifactId>
        <version>4.3.0</version>
    </dependency>
    <dependency>
        <groupId>org.icefaces</groupId>
        <artifactId>icefaces-ace</artifactId>
        <version>4.3.0</version>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>1.4.200</version>
    </dependency>
</dependencies>
```

### Application Structure
```
phonebook-app/
├── src/main/java/
│   └── com/phonebook/
│       ├── entity/Contact.java
│       ├── repository/ContactRepository.java
│       ├── service/ContactService.java
│       └── bean/ContactBean.java
├── src/main/resources/
│   └── META-INF/persistence.xml
├── src/main/webapp/
│   ├── WEB-INF/
│   │   ├── web.xml
│   │   └── faces-config.xml
│   └── contacts.xhtml
└── pom.xml
```

This design ensures a robust, maintainable, and scalable phone book application that meets all specified requirements while following Java EE best practices and ICEfaces 4 conventions.