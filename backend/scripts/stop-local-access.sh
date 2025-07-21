#!/bin/bash

# Script to stop local access to the Congen application

echo "🛑 Stopping local access to Congen application..."

# Kill port forward processes
echo "Stopping port forward processes..."
pkill -f "kubectl port-forward.*congen" || true

# Remove PID file
rm -f /tmp/congen-port-forward.pid

# Remove log file
rm -f /tmp/congen-port-forward.log

echo "✅ Local access stopped"
echo "📝 To restart local access, run: ./scripts/access-local-app.sh" 