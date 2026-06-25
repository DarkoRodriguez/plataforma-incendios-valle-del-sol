#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if ! command -v helm >/dev/null 2>&1; then
  echo "helm is not installed. Install Helm 3 before running this script."
  exit 1
fi

if kubectl get ingressclass traefik >/dev/null 2>&1; then
  echo "Traefik IngressClass already installed."
  exit 0
fi

CONTEXT="$(kubectl config current-context 2>/dev/null || true)"
IS_KIND=false
if [[ "$CONTEXT" == kind-* ]]; then
  IS_KIND=true
fi

helm repo add traefik https://traefik.github.io/charts >/dev/null 2>&1 || true
helm repo update traefik

if [[ "$IS_KIND" == true ]]; then
  echo "Installing Traefik for kind (hostPort 80/443)..."
  helm upgrade --install traefik traefik/traefik \
    --namespace traefik --create-namespace \
    --set providers.kubernetesIngress.enabled=true \
    --set providers.kubernetesIngress.allowEmptyServices=true \
    --set ingressClass.enabled=true \
    --set ingressClass.isDefaultClass=true \
    --set ingressClass.name=traefik \
    --set service.type=ClusterIP \
    --set ports.web.hostPort=80 \
    --set ports.websecure.hostPort=443
else
  echo "Installing Traefik for local Kubernetes (LoadBalancer)..."
  helm upgrade --install traefik traefik/traefik \
    --namespace traefik --create-namespace \
    --set providers.kubernetesIngress.enabled=true \
    --set providers.kubernetesIngress.allowEmptyServices=true \
    --set ingressClass.enabled=true \
    --set ingressClass.isDefaultClass=true \
    --set ingressClass.name=traefik \
    --set service.type=LoadBalancer
fi

kubectl -n traefik rollout status deployment/traefik --timeout=180s
echo "Traefik installed in namespace 'traefik'."
