#!/usr/bin/env bash
set -euo pipefail

CLUSTER_NAME=plataforma

# Example image builds - ajusta según los servicios que quieras levantar
docker build -t ms-users:local ./backend/ms-users
docker build -t ms-reports:local ./backend/ms-reports
docker build -t ms-bff:local ./backend/bff

echo "Loading images into kind cluster ${CLUSTER_NAME}..."
kind load docker-image ms-users:local --name ${CLUSTER_NAME}
kind load docker-image ms-reports:local --name ${CLUSTER_NAME}
kind load docker-image ms-bff:local --name ${CLUSTER_NAME}

echo "Images loaded. Apply manifests with: kubectl apply -R -f k8s/"
