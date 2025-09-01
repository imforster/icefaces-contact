# WildFly Deployment Guide for ICEfaces Phonebook Application

This guide provides step-by-step instructions for deploying the ICEfaces Phonebook application on WildFly application server with Java 8.

## Prerequisites

- Java 8 (JDK 1.8.x)
- WildFly 10.x or later (Java EE 7 compatible)
- Maven 3.x
- H2 Database (included in dependencies)

## Quick Deployment

### Automated Deployment

1. **Build and Deploy (Recommended)**
   ```bash
   ./deploy-to-wildfly.sh
   ```
   This script will:
   - Start WildFly if not running
   - Set up H2 database module
   - Configure datasource
   - Build and deploy the application

2. **Verify Deployment**
   ```bash
   ./verify-deployment.sh
   ```

3. **Access Application**
   - Application: http://localhost:8080/phonebook
   - Admin Console: http://localhost:9990

### Manual Deployment

If you prefer manual deployment, follow these steps:

## Step 1: Prepare WildFly

1. **Download and Extract WildFly**
   ```bash
   wget https://download.jboss.org/wildfly/26.1.3.Final/wildfly-26.1.3.Final.tar.gz
   tar -xzf wildfly-26.1.3.Final.tar.gz
   export WILDFLY_HOME=./wildfly-26.1.3.Final
   ```

2. **Start WildFly**
   ```bash
   $WILDFLY_HOME/bin/standalone.sh
   ```

## Step 2: Configure H2 Database Module

1. **Create H2 Module Directory**
   ```bash
   mkdir -p $WILDFLY_HOME/modules/system/layers/base/com/h2database/h2/main
   ```

2. **Copy H2 JAR and Module Configuration**
   ```bash
   # Copy H2 JAR from Maven repository
   cp ~/.m2/repository/com/h2database/h2/1.4.200/h2-1.4.200.jar \
      $WILDFLY_HOME/modules/system/layers/base/com/h2database/h2/main/
   
   # Copy module configuration
   cp src/main/resources/h2-module.xml \
      $WILDFLY_HOME/modules/system/layers/base/com/h2database/h2/main/module.xml
   ```

## Step 3: Configure Datasource

1. **Run Datasource Configuration Script**
   ```bash
   $WILDFLY_HOME/bin/jboss-cli.sh --connect --file=src/main/resources/wildfly-datasource.cli
   ```

2. **Verify Datasource**
   ```bash
   $WILDFLY_HOME/bin/jboss-cli.sh --connect \
     --command="/subsystem=datasources/data-source=PhonebookDS:test-connection-in-pool"
   ```

## Step 4: Build and Deploy Application

1. **Build WAR File**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Deploy to WildFly**
   ```bash
   $WILDFLY_HOME/bin/jboss-cli.sh --connect \
     --command="deploy target/icefaces-phonebook.war --force"
   ```

## Step 5: Verify Deployment

1. **Check Deployment Status**
   ```bash
   $WILDFLY_HOME/bin/jboss-cli.sh --connect \
     --command="deployment-info --name=icefaces-phonebook.war"
   ```

2. **Access Application**
   - Open browser: http://localhost:8080/phonebook
   - Should display the phonebook interface with sample contacts

## Configuration Files

### WildFly-Specific Files Created

- `src/main/webapp/WEB-INF/jboss-web.xml` - WildFly web configuration
- `src/main/webapp/WEB-INF/jboss-deployment-structure.xml` - Module dependencies
- `src/main/resources/wildfly-datasource.cli` - Datasource configuration script
- `src/main/resources/h2-module.xml` - H2 database module definition

### Database Configuration

- **Datasource JNDI**: `java:jboss/datasources/PhonebookDS`
- **Database Location**: `~/phonebook.mv.db` (H2 file-based)
- **Connection URL**: `jdbc:h2:~/phonebook;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE`
- **Schema**: Auto-created on first startup
- **Sample Data**: Loaded from `META-INF/initial-data.sql`

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check if WildFly is already running
   lsof -i :8080
   lsof -i :9990
   ```

2. **H2 Module Not Found**
   ```bash
   # Verify H2 module structure
   ls -la $WILDFLY_HOME/modules/system/layers/base/com/h2database/h2/main/
   ```

3. **Datasource Connection Failed**
   ```bash
   # Test datasource connection
   $WILDFLY_HOME/bin/jboss-cli.sh --connect \
     --command="/subsystem=datasources/data-source=PhonebookDS:test-connection-in-pool"
   ```

4. **Application Not Accessible**
   - Check deployment status in admin console
   - Verify logs: `tail -f $WILDFLY_HOME/standalone/log/server.log`
   - Ensure context root is correct: `/phonebook`

### Log Files

- **Server Log**: `$WILDFLY_HOME/standalone/log/server.log`
- **Boot Log**: `$WILDFLY_HOME/standalone/log/boot.log`
- **Access Log**: `$WILDFLY_HOME/standalone/log/access.log`

## Undeployment

### Automated Undeployment
```bash
./undeploy-from-wildfly.sh
```

### Manual Undeployment
```bash
$WILDFLY_HOME/bin/jboss-cli.sh --connect \
  --command="undeploy icefaces-phonebook.war"
```

## Production Considerations

1. **Security**
   - Change default admin credentials
   - Configure SSL/TLS
   - Set up proper authentication

2. **Performance**
   - Tune JVM heap size
   - Configure connection pool settings
   - Enable compression

3. **Monitoring**
   - Set up application monitoring
   - Configure log rotation
   - Monitor database performance

4. **Backup**
   - Regular database backups
   - Configuration backups
   - Application deployment backups

## Maven Integration

The project includes WildFly Maven plugin for automated deployment:

```bash
# Deploy using Maven
mvn clean package wildfly:deploy

# Undeploy using Maven
mvn wildfly:undeploy
```

## Requirements Verification

This deployment configuration satisfies the following requirements:

- **6.1**: Application runs successfully on WildFly application server
- **6.2**: Compatible with Java 8 runtime environment  
- **6.3**: H2 database schema created automatically on first startup
- **6.4**: ICEfaces 4 web interface served correctly
- **7.4**: Empty H2 database initialized on first application start

## Support

For issues or questions:
1. Check WildFly logs for error messages
2. Verify all configuration files are in place
3. Ensure Java 8 compatibility
4. Test database connectivity separately

The deployment is now complete and ready for use!