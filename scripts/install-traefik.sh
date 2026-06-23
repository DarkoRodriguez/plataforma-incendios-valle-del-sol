#!/usr/bin/env bash
set -euo pipefail

# Install Traefik via Helm as a DaemonSet so it can bind host ports in kind
helm repo add traefik https://traefik.github.io/charts
helm repo update

helm install traefik traefik/traefik \
  --namespace traefik --create-namespace \
  --set daemonset.enabled=true \
  --set service.type=ClusterIP \
  --set ports.web.hostPort.enabled=true \
  --set ports.web.hostPort=80 \
  --set ports.websecure.hostPort.enabled=true \
  --set ports.websecure.hostPort=443

# Wait for traefik pods
kubectl -n traefik rollout status daemonset/traefik

echo "Traefik installed in namespace 'traefik'." 
