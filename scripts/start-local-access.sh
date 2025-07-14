#!/bin/bash

# Script to access the local Congen application
# This script sets up port forwarding to make the application accessible on localhost

set -e

echo "🚀 Setting up local access to Congen application..."

# Kill any existing port forward
echo "Cleaning up existing port forwards..."
pkill -f "kubectl port-forward.*congen" || true

# Start port forward in background
echo "Starting port forward..."
kubectl port-forward service/congen 8080:8080 -n congen > /tmp/congen-port-forward.log 2>&1 &
PORT_FORWARD_PID=$!

# Wait for port forward to establish
sleep 3

# Check if port forward is working
if ! nc -z localhost 8080 2>/dev/null; then
    echo "❌ Port forward failed to establish"
    kill $PORT_FORWARD_PID 2>/dev/null || true
    exit 1
fi

echo "✅ Port forward established (PID: $PORT_FORWARD_PID)"
echo "📝 Application is now accessible at:"
echo "   - Health check: http://localhost:8080/actuator/health"
echo "   - Main application: http://localhost:8080"
echo ""
echo "To stop the port forward, run: kill $PORT_FORWARD_PID"
echo "Or use: pkill -f 'kubectl port-forward.*congen'"
echo ""
echo "Port forward logs are available at: /tmp/congen-port-forward.log"

# Save PID for easy cleanup
echo $PORT_FORWARD_PID > /tmp/congen-port-forward.pid

# Test the health endpoint
echo ""
echo "🧪 Testing health endpoint..."
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ Application is healthy and accessible!"
    echo ""
    echo "🎉 You can now access the application at:"
    echo "   http://localhost:8080"
    echo ""
    echo "💡 Alternative access methods:"
    echo "   - Fixed NodePort: http://$(minikube ip):30080"
    echo "   - Port forwarding: http://localhost:8080 (current)"
else
    echo "❌ Application health check failed"
    exit 1
fi 