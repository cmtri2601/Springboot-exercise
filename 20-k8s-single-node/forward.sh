#!/bin/bash

echo "Starting port-forwarding..."

kubectl port-forward svc/kafka-ui 8000:8000 &
kubectl port-forward svc/person-service 8080:8080 &
kubectl port-forward svc/cron-service 8081:8081 &
kubectl port-forward pod/postgres-0 5432:5432 &

echo "Port-forwarding started. Press Ctrl+C to stop."

wait