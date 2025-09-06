#!/bin/bash

# Script to update the Kubernetes ConfigMap with the built theme JAR
# This script creates a separate ConfigMap for the JAR due to size limitations

set -e

# Paths
JAR_FILE="dist_keycloak/congen-account-theme.jar"
K8S_THEME_DIR="../k8s/base/stage-6/keycloak-theme"
JAR_CONFIGMAP_FILE="$K8S_THEME_DIR/keycloak-account-theme-jar-configmap.yaml"

echo "🔧 Creating Keycloak account theme JAR ConfigMap..."

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: JAR file not found at $JAR_FILE"
    echo "   Please run './scripts/build-theme.sh' first"
    exit 1
fi

# Create the k8s theme directory if it doesn't exist
mkdir -p "$K8S_THEME_DIR"

# Copy the JAR file to the k8s directory
cp "$JAR_FILE" "$K8S_THEME_DIR/"

# Create a ConfigMap YAML that references the JAR file
cat > "$JAR_CONFIGMAP_FILE" << EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: keycloak-account-theme-jar
  namespace: congen
binaryData:
  "congen-account-theme.jar": |
$(base64 -i "$JAR_FILE" | sed 's/^/    /')
EOF

echo "✅ JAR ConfigMap created successfully!"
echo "📁 ConfigMap file: $JAR_CONFIGMAP_FILE"
echo "📦 JAR size: $(wc -c < "$JAR_FILE") bytes"
echo "📁 JAR copied to: $K8S_THEME_DIR/congen-account-theme.jar"
echo ""
echo "🚀 Ready for deployment!"
echo "   Remember to update kustomization.yaml to include the new ConfigMap"
