# ICEfaces Phonebook Application

A phone book application built with ICEfaces 4, deployable on Java 8 and WildFly application server, using H2 database for contact storage.

## Project Structure

```
icefaces-phonebook/
├── pom.xml                           # Maven configuration with ICEfaces 4.3.0, H2, and Java EE 7 dependencies
├── src/
│   ├── main/
│   │   ├── java/com/phonebook/
│   │   │   ├── entity/               # JPA entities
│   │   │   ├── repository/           # Data access layer
│   │   │   ├── service/              # Business logic layer
│   │   │   └── bean/                 # JSF managed beans
│   │   ├── resources/
│   │   │   └── META-INF/             # JPA persistence.xml and other resources
│   │   └── webapp/
│   │       └── WEB-INF/              # Web application configuration
│   └── test/
│       ├── java/com/phonebook/       # Unit and integration tests
│       └── resources/                # Test resources
└── README.md
```

## Technology Stack

- **Java**: 8
- **Framework**: ICEfaces 4.3.0 with JSF 2.2
- **Database**: H2 Database (embedded mode)
- **Application Server**: WildFly (Java EE 7 compatible)
- **Build Tool**: Maven 3.x

## Build Commands

```bash
# Validate project
mvn validate

# Compile project
mvn compile

# Run tests
mvn test

# Package as WAR
mvn package

# Clean build
mvn clean compile
```

## Requirements

- Java 8 or higher
- Maven 3.x
- WildFly application server for deployment