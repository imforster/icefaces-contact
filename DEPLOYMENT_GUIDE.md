# ICEfaces Phonebook Deployment Guide

## ✅ Configuration Validation Results

The application has been successfully configured with:

- **JSF Servlet**: Properly configured with `*.xhtml` URL pattern
- **ICEfaces 4.3.0**: Theme (sam) and resource handling configured
- **Message Bundle**: Internationalization support enabled
- **CSS Resources**: Custom styling included
- **WAR Size**: 13MB with all dependencies

## 🚀 Deployment Instructions

### Option 1: WildFly Standalone Deployment

1. **Download WildFly 26.1.3.Final**:
   ```bash
   wget https://github.com/wildfly/wildfly/releases/download/26.1.3.Final/wildfly-26.1.3.Final.tar.gz
   tar -xzf wildfly-26.1.3.Final.tar.gz
   ```

2. **Start WildFly**:
   ```bash
   cd wildfly-26.1.3.Final
   ./bin/standalone.sh
   ```

3. **Deploy Application**:
   ```bash
   cp target/icefaces-phonebook.war wildfly-26.1.3.Final/standalone/deployments/
   ```

4. **Access Application**:
   - URL: http://localhost:8080/icefaces-phonebook/
   - Admin Console: http://localhost:9990/

### Option 2: Docker Deployment

1. **Start WildFly Container**:
   ```bash
   docker run -d --name wildfly-phonebook \
     -p 8080:8080 -p 9990:9990 \
     quay.io/wildfly/wildfly:26.1.3.Final-jdk11 \
     /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0
   ```

2. **Deploy Application**:
   ```bash
   docker cp target/icefaces-phonebook.war wildfly-phonebook:/opt/jboss/wildfly/standalone/deployments/
   ```

3. **Monitor Deployment**:
   ```bash
   docker logs -f wildfly-phonebook
   ```

## 🔍 Testing Endpoints

Once deployed, test these URLs:

- **Root**: http://localhost:8080/icefaces-phonebook/
- **Index**: http://localhost:8080/icefaces-phonebook/index.xhtml
- **Contacts**: http://localhost:8080/icefaces-phonebook/contacts.xhtml (will be created in next tasks)

## 📋 Expected Behavior

1. **Index Page**: Should display welcome message and redirect to contacts page
2. **ICEfaces Theme**: Sam theme should be applied
3. **Resource Loading**: CSS and JavaScript resources should load properly
4. **JSF Navigation**: Navigation rules should work correctly

## 🛠️ Configuration Details

### JSF Configuration (web.xml)
- JSF 2.2 with Facelets
- ICEfaces ACE theme: `sam`
- Session timeout: 30 minutes
- Welcome file: `contacts.xhtml`

### ICEfaces Configuration
- Combined resource mode enabled
- Strict session timeout disabled
- Session expired redirect to contacts page
- Resource directory: `/resources`

### Message Bundle
- Base name: `messages`
- Variable: `msg`
- Default locale: English

## 🚨 Troubleshooting

### Common Issues:

1. **404 Error**: Check if WAR deployed successfully in deployments folder
2. **JSF Not Loading**: Verify JSF libraries are in WEB-INF/lib
3. **ICEfaces Components Not Working**: Check ICEfaces JARs and theme configuration
4. **CSS Not Loading**: Verify resource mapping in web.xml

### Deployment Status Check:
```bash
# Check deployment status
ls -la wildfly-26.1.3.Final/standalone/deployments/
# Look for .deployed file next to WAR
```

### Log Locations:
- **WildFly Logs**: `wildfly-26.1.3.Final/standalone/log/server.log`
- **Application Logs**: Check server.log for application-specific messages

## ✅ Validation Checklist

- [ ] WAR file builds successfully (13MB)
- [ ] web.xml contains JSF servlet configuration
- [ ] faces-config.xml contains ICEfaces factories
- [ ] messages.properties exists in classpath
- [ ] CSS resources are included
- [ ] ICEfaces JARs are in WEB-INF/lib
- [ ] Application deploys without errors
- [ ] Index page loads and redirects
- [ ] ICEfaces theme is applied

## 🎯 Next Steps

After successful deployment:
1. Implement ContactController (Task 7)
2. Create contacts.xhtml page (Task 8)
3. Test full CRUD functionality
4. Verify ICEfaces components work properly