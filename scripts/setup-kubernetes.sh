#!/bin/bash

# Kubernetes Setup Script for Congen
# This script sets up the local Kubernetes environment for development

set -e

echo "🚀 Setting up Kubernetes environment for Congen..."

# Check if required tools are installed
check_tool() {
    if ! command -v "${1}" &> /dev/null; then
        echo "❌ ${1} is not installed. Please install it first."
        exit 1
    fi
}

echo "📋 Checking prerequisites..."
check_tool "minikube"
check_tool "kubectl"
check_tool "skaffold"
check_tool "docker"
check_tool "kustomize"

echo "✅ All prerequisites are installed"

# Start Minikube if not running
if ! minikube status &> /dev/null; then
    echo "🔧 Starting Minikube..."
    minikube start --memory=8192 --cpus=4 --disk-size=20g
else
    echo "✅ Minikube is already running"
fi

# Enable addons
echo "🔧 Enabling Minikube addons..."
minikube addons enable ingress
minikube addons enable metrics-server

# Point shell to minikube's docker-daemon
echo "🔧 Configuring Docker environment..."
MINIKUBE_DOCKER_ENV_OUTPUT="$(minikube docker-env)"
eval "${MINIKUBE_DOCKER_ENV_OUTPUT}"

# Create namespace
echo "🔧 Creating congen namespace..."
KUBECTL_NAMESPACE_YAML_OUTPUT="$(kubectl create namespace congen --dry-run=client -o yaml)"
echo "${KUBECTL_NAMESPACE_YAML_OUTPUT}" | kubectl apply -f -

echo "✅ Kubernetes environment setup complete!"
echo ""
echo "Next steps:"
echo "1. Build the application: ./gradlew buildDockerImage"
echo "2. Deploy to local: ./gradlew deployToLocal"
echo "3. Or use Skaffold: ./gradlew skaffoldDev"
echo ""
echo "Access the application:"
MINIKUBE_IP_OUTPUT="$(minikube ip)"
echo "- Minikube IP: ${MINIKUBE_IP_OUTPUT}"
echo "- Application: http://${MINIKUBE_IP_OUTPUT}:30080"
echo "- Health check: http://${MINIKUBE_IP_OUTPUT}:30080/actuator/health" 