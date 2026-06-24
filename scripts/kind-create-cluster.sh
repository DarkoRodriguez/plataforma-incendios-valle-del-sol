#!/usr/bin/env bash
set -euo pipefail

# Create kind cluster using kind-config.yaml
kind create cluster --name plataforma --config kind-config.yaml
kubectl cluster-info --context kind-plataforma

echo "Cluster 'plataforma' created. Next: install Traefik (see scripts/install-traefik.sh) and load images."
