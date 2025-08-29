#!/bin/bash

echo "🚀 Testing ICEfaces Phonebook Deployment"
echo "========================================"

# Check if WAR file exists
if [ ! -f "target/icefaces-phonebook.war" ]; then
    echo "❌ WAR file not found. Building application..."
    mvn clean package -DskipTests
fi

echo "📦 WAR file size: $(ls -lh target/icefaces-phonebook.war | awk '{print $5}')"

# Start WildFly container
echo "🐳 Starting WildFly container..."
docker run -d --name wildfly-test \
    -p 8080:8080 \
    -p 9990:9990 \
    quay.io/wildfly/wildfly:26.1.3.Final-jdk11 \
    /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0

# Wait for WildFly to start
echo "⏳ Waiting for WildFly to start..."
sleep 30

# Check if WildFly is running
if ! docker ps | grep -q wildfly-test; then
    echo "❌ WildFly container failed to start"
    docker logs wildfly-test
    exit 1
fi

echo "✅ WildFly container is running"

# Copy WAR file to container
echo "📋 Deploying application..."
docker cp target/icefaces-phonebook.war wildfly-test:/opt/jboss/wildfly/standalone/deployments/

# Wait for deployment
echo "⏳ Waiting for deployment..."
sleep 15

# Check deployment status
echo "🔍 Checking deployment status..."
docker exec wildfly-test ls -la /opt/jboss/wildfly/standalone/deployments/

# Test if application is accessible
echo "🌐 Testing application accessibility..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/icefaces-phonebook/ | grep -q "200\|302"; then
    echo "✅ Application is accessible!"
    echo "🔗 Application URL: http://localhost:8080/icefaces-phonebook/"
    echo ""
    echo "📋 Testing endpoints:"
    echo "   - Root: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/icefaces-phonebook/)"
    echo "   - Index: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/icefaces-phonebook/index.xhtml)"
    echo "   - Contacts: $(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/icefaces-phonebook/contacts.xhtml)"
else
    echo "❌ Application is not accessible"
    echo "📋 Container logs:"
    docker logs wildfly-test | tail -20
fi

echo ""
echo "🛠️  To manually test:"
echo "   1. Open browser to: http://localhost:8080/icefaces-phonebook/"
echo "   2. Check WildFly admin: http://localhost:9990/"
echo ""
echo "🧹 To cleanup:"
echo "   docker stop wildfly-test && docker rm wildfly-test"