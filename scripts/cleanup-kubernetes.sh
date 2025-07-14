#!/bin/bash

# Kubernetes Cleanup Script for Congen
# This script cleans up the local Kubernetes environment

set -e

echo "🧹 Cleaning up Kubernetes environment for Congen..."

# Delete all resources in congen namespace
echo "🗑️  Deleting congen namespace and all resources..."
kubectl delete namespace congen --ignore-not-found=true

# Stop Minikube
echo "🛑 Stopping Minikube..."
minikube stop

echo "✅ Cleanup complete!"
echo ""
echo "To completely remove Minikube cluster:"
echo "minikube delete" 