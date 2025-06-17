#!/bin/bash

# This script labels Kubernetes nodes for service-specific workloads

# Label nodes for different service types
kubectl label nodes worker-node-1 service-type=application
kubectl label nodes worker-node-2 service-type=batch
kubectl label nodes worker-node-3 service-type=database
kubectl label nodes worker-node-4 service-type=messaging
kubectl label nodes worker-node-5 service-type=ui

echo "Nodes labeled successfully for service-specific workloads"