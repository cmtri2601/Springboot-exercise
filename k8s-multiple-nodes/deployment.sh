#!/bin/bash

# Create namespace and configs first
kubectl apply -f config.yaml

# Deploy infrastructure components
kubectl apply -f postgres.yaml
kubectl apply -f kafka.yaml

# Wait for infrastructure to be ready
echo "Waiting for database and kafka to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres --timeout=120s -n person-app
kubectl wait --for=condition=ready pod -l app=kafka --timeout=120s -n person-app

# Deploy application components
kubectl apply -f person-service.yaml
kubectl apply -f cron-service.yaml
kubectl apply -f kafka-ui.yaml

echo "Deployment completed successfully!"