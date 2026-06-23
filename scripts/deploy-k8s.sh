#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

NS="plataforma-incendios"
IMAGES=(
  "plataforma/ms-users:latest"
  "plataforma/ms-reports:latest"
  "plataforma/ms-alerts:latest"
  "plataforma/ms-bff:latest"
  "plataforma/mfe-mapeo:latest"
)

MANIFESTS=(
  k8s/00-namespace.yaml
  k8s/01-postgres-configmap.yaml
  k8s/02-postgres-pvc.yaml
  k8s/03-postgres-deployment.yaml
  k8s/04-postgres-service.yaml
  k8s/05-minio-pvc.yaml
  k8s/06-minio-deployment.yaml
  k8s/07-minio-service.yaml
  k8s/09-ms-users-deployment.yaml
  k8s/10-ms-users-service.yaml
  k8s/11-ms-reports-deployment.yaml
  k8s/12-ms-reports-service.yaml
  k8s/13-ms-alerts-deployment.yaml
  k8s/14-ms-alerts-service.yaml
  k8s/15-ms-bff-deployment.yaml
  k8s/16-ms-bff-service.yaml
  k8s/17-krakend-configmap.yaml
  k8s/18-krakend-deployment.yaml
  k8s/19-krakend-service.yaml
  k8s/20-mfe-mapeo-deployment.yaml
  k8s/21-mfe-mapeo-service.yaml
  k8s/22-ingress.yaml
)

DEPLOYMENTS=(
  postgres
  minio
  ms-users
  ms-reports
  ms-alerts
  ms-bff
  krakend
  mfe-mapeo
)

error() {
  echo "ERROR: $1" >&2
  exit 1
}

for cmd in docker kubectl; do
  command -v "$cmd" >/dev/null 2>&1 || error "Required command not found: $cmd"
done

kubectl cluster-info >/dev/null 2>&1 || error "kubectl cannot reach a Kubernetes cluster."

if [[ ! -f .env ]]; then
  error ".env not found. Copy .env.example to .env and fill in VAPID key values."
fi

if [[ ! -f private_key.pem || ! -f public_key.pem ]]; then
  error "private_key.pem and public_key.pem must exist in the repository root."
fi

get_env_value() {
  local key="$1"
  grep -E "^[[:space:]]*${key}=" .env | tail -n 1 | cut -d'=' -f2-
}

VAPID_PUBLIC_KEY="$(get_env_value VAPID_PUBLIC_KEY)"
VAPID_PRIVATE_KEY="$(get_env_value VAPID_PRIVATE_KEY)"
VAPID_SUBJECT="$(get_env_value VAPID_SUBJECT)"

[[ -n "$VAPID_PUBLIC_KEY" && -n "$VAPID_PRIVATE_KEY" ]] || error "VAPID_PUBLIC_KEY and VAPID_PRIVATE_KEY must be set in .env."
[[ -n "$VAPID_SUBJECT" ]] || VAPID_SUBJECT="mailto:admin@example.com"

if [[ "${1:-}" == "--setup-kind" ]]; then
  bash "$ROOT_DIR/scripts/setup-kind.sh"
fi

if ! kubectl get ingressclass traefik >/dev/null 2>&1; then
  echo "Traefik not found. Installing..."
  bash "$ROOT_DIR/scripts/install-traefik.sh"
fi

echo "Building Docker images..."
docker build -t plataforma/ms-users:latest backend/ms-users
docker build -t plataforma/ms-reports:latest backend/ms-reports
docker build -t plataforma/ms-alerts:latest backend/ms-alerts
docker build -t plataforma/ms-bff:latest backend/bff
docker build \
  --build-arg NGINX_CONF=nginx.k8s.conf \
  --build-arg VITE_VAPID_PUBLIC_KEY="$VAPID_PUBLIC_KEY" \
  --build-arg VITE_API_BASE=/api \
  -t plataforma/mfe-mapeo:latest frontend/mfe-mapeo

CONTEXT="$(kubectl config current-context 2>/dev/null || true)"
if [[ "$CONTEXT" == kind-* ]]; then
  CLUSTER_NAME="${CONTEXT#kind-}"
  echo "Loading images into kind cluster '$CLUSTER_NAME'..."
  for image in "${IMAGES[@]}"; do
    kind load docker-image "$image" --name "$CLUSTER_NAME"
  done
fi

echo "Creating namespace and secrets..."
kubectl apply -f k8s/00-namespace.yaml

kubectl create secret generic ms-alerts-keys \
  --from-literal=VAPID_PUBLIC_KEY="$VAPID_PUBLIC_KEY" \
  --from-literal=VAPID_PRIVATE_KEY="$VAPID_PRIVATE_KEY" \
  --from-literal=VAPID_SUBJECT="$VAPID_SUBJECT" \
  -n "$NS" --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic ms-usuarios-keys \
  --from-file=private_key.pem=private_key.pem \
  --from-file=public_key.pem=public_key.pem \
  -n "$NS" --dry-run=client -o yaml | kubectl apply -f -

echo "Applying Kubernetes manifests..."
for manifest in "${MANIFESTS[@]}"; do
  kubectl apply -f "$manifest"
done

kubectl delete job minio-setup -n "$NS" --ignore-not-found
kubectl apply -f k8s/08-minio-setup-job.yaml

echo "Waiting for deployments..."
for deployment in "${DEPLOYMENTS[@]}"; do
  echo "  - deployment/$deployment"
  kubectl rollout status "deployment/$deployment" -n "$NS" --timeout=300s
done

kubectl wait --for=condition=complete job/minio-setup -n "$NS" --timeout=120s

echo
echo "Kubernetes deployment finished."
echo "Traffic flow: browser -> Traefik -> (frontend | Krakend -> BFF -> microservices)"
echo
echo "Access URLs:"
echo "  App (recommended):  http://plataforma.local"
echo "  App (localhost):    http://localhost"
echo "  Swagger UI:         http://plataforma.local/swagger-ui"
echo "  MinIO API:          http://localhost:30090"
echo "  MinIO Console:      http://localhost:30091"
echo
echo "Add to /etc/hosts if needed:"
echo "  127.0.0.1 plataforma.local"
