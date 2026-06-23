#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

function error() {
  echo "❌ $1"
  exit 1
}

for cmd in docker kubectl; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    error "Required command not found: $cmd"
  fi
done

echo "✅ Docker and kubectl detected"

if ! kubectl cluster-info >/dev/null 2>&1; then
  error "kubectl cannot reach a Kubernetes cluster. Enable Docker Desktop Kubernetes or set KUBECONFIG to a valid cluster."
fi

echo "✅ Kubernetes cluster reachable"

if [[ ! -f .env ]]; then
  error ".env not found. Copy .env.example to .env and fill in VAPID key values."
fi

function get_env_value() {
  local key="$1"
  grep -E "^[[:space:]]*${key}=" .env | tail -n 1 | cut -d'=' -f2-
}

VAPID_PUBLIC_KEY="$(get_env_value VAPID_PUBLIC_KEY)"
VAPID_PRIVATE_KEY="$(get_env_value VAPID_PRIVATE_KEY)"
VAPID_SUBJECT="$(get_env_value VAPID_SUBJECT)"

if [[ -z "$VAPID_PUBLIC_KEY" || -z "$VAPID_PRIVATE_KEY" ]]; then
  error "VAPID_PUBLIC_KEY and VAPID_PRIVATE_KEY must be set in .env to create ms-alerts secret."
fi
if [[ -z "$VAPID_SUBJECT" ]]; then
  VAPID_SUBJECT="mailto:admin@example.com"
fi

echo "➡ Creating namespace"
kubectl apply -f k8s/namespace.yaml

if [[ ! -f private_key.pem || ! -f public_key.pem ]]; then
  error "private_key.pem and/or public_key.pem not found in repository root. Generate or copy them before running this script."
fi

echo "➡ Creating namespace"
kubectl apply -f k8s/namespace.yaml

echo "➡ Creating VAPID secret for ms-alerts"
kubectl create secret generic ms-alerts-keys \
  --from-literal=VAPID_PUBLIC_KEY="$VAPID_PUBLIC_KEY" \
  --from-literal=VAPID_PRIVATE_KEY="$VAPID_PRIVATE_KEY" \
  --from-literal=VAPID_SUBJECT="$VAPID_SUBJECT" \
  -n plataforma-incendios --dry-run=client -o yaml | kubectl apply -f -

if [[ ! -f private_key.pem || ! -f public_key.pem ]]; then
  error "private_key.pem and/or public_key.pem not found in repository root. Generate or copy them before running this script."
fi

echo "➡ Creating JWT key secret for ms-usuarios"
kubectl create secret generic ms-usuarios-keys \
  --from-file=private_key.pem=private_key.pem \
  --from-file=public_key.pem=public_key.pem \
  -n plataforma-incendios --dry-run=client -o yaml | kubectl apply -f -

echo "➡ Building Docker images"
docker build -t plataforma/ms-users:latest backend/ms-users
docker build -t plataforma/ms-reports:latest backend/ms-reports
docker build -t plataforma/ms-alerts:latest backend/ms-alerts
docker build -t plataforma/ms-bff:latest backend/bff
docker build --build-arg VITE_VAPID_PUBLIC_KEY="$VAPID_PUBLIC_KEY" -t plataforma/mfe-mapeo:latest frontend/mfe-mapeo

echo "➡ Applying Kubernetes manifests"
kubectl apply -f k8s/postgres/configmap.yaml
kubectl apply -f k8s/postgres/pvc.yaml
kubectl apply -f k8s/postgres/deployment.yaml
kubectl apply -f k8s/postgres/service.yaml
kubectl apply -f k8s/minio/pvc.yaml
kubectl apply -f k8s/minio/deployment.yaml
kubectl apply -f k8s/minio/service.yaml
kubectl apply -f k8s/krakend/configmap.yaml
kubectl apply -f k8s/krakend/deployment.yaml
kubectl apply -f k8s/krakend/service.yaml
kubectl apply -f backend/ms-users/k8s/deployment.yaml
kubectl apply -f backend/ms-users/k8s/service.yaml
kubectl apply -f backend/ms-reports/k8s/deployment.yaml
kubectl apply -f backend/ms-reports/k8s/service.yaml
kubectl apply -f backend/ms-alerts/k8s/deployment.yaml
kubectl apply -f backend/ms-alerts/k8s/service.yaml
kubectl apply -f backend/bff/k8s/deployment.yaml
kubectl apply -f backend/bff/k8s/service.yaml
kubectl apply -f frontend/mfe-mapeo/k8s/deployment.yaml
kubectl apply -f frontend/mfe-mapeo/k8s/service.yaml
kubectl apply -f k8s/ingress/ingress.yaml

echo "➡ Waiting for deployments to become ready"
for deployment in postgres minio krakend ms-usuarios ms-reports ms-alerts ms-bff mfe-mapeo; do
  echo "Waiting for deployment/$deployment..."
  kubectl rollout status deployment/$deployment -n plataforma-incendios --timeout=180s || true
done

echo "✅ Kubernetes deployment finished"
echo "Local access URLs:"
echo "  Frontend: http://localhost:30080"
echo "  BFF gateway: http://localhost:30081"
echo "  MinIO API: http://localhost:30090"
echo "  MinIO Console: http://localhost:30091"
echo "  BFF swagger / openapi: http://localhost:30081/swagger-ui"
echo "  Optional ingress path (if Traefik ingress and host routing are configured): http://localhost/api"
