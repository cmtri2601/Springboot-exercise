#!/bin/bash

# Step 1: Apply namespace first
kubectl apply -f development/namespace.yaml

# Step 2: Wait for namespace to be ready (optional but safe)
echo "Waiting for namespace 'person-app' to be active..."
while [[ $(kubectl get ns person-app -o jsonpath='{.status.phase}') != "Active" ]]; do
  sleep 1
done

# Step 3: Apply the rest of the resources
kubectl apply -f development/