#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLUSTER_NAME="${KIND_CLUSTER_NAME:-plataforma}"

IMAGES=(
  "plataforma/ms-users:latest"
  "plataforma/ms-reports:latest"
  "plataforma/ms-alerts:latest"
  "plataforma/ms-bff:latest"
  "plataforma/mfe-mapeo:latest"
)

echo "Building images..."
docker build -t plataforma/ms-users:latest "$ROOT_DIR/backend/ms-users"
docker build -t plataforma/ms-reports:latest "$ROOT_DIR/backend/ms-reports"
docker build -t plataforma/ms-alerts:latest "$ROOT_DIR/backend/ms-alerts"
docker build -t plataforma/ms-bff:latest "$ROOT_DIR/backend/bff"

if [[ -f "$ROOT_DIR/.env" ]]; then
  VAPID_PUBLIC_KEY="$(grep -E '^[[:space:]]*VAPID_PUBLIC_KEY=' "$ROOT_DIR/.env" | tail -n 1 | cut -d'=' -f2-)"
else
  VAPID_PUBLIC_KEY=""
fi

docker build \
  --build-arg NGINX_CONF=nginx.k8s.conf \
  --build-arg VITE_VAPID_PUBLIC_KEY="${VAPID_PUBLIC_KEY}" \
  --build-arg VITE_API_BASE=/api \
  -t plataforma/mfe-mapeo:latest "$ROOT_DIR/frontend/mfe-mapeo"

if command -v kind >/dev/null 2>&1 && kind get clusters 2>/dev/null | grep -qx "$CLUSTER_NAME"; then
  echo "Loading images into kind cluster '$CLUSTER_NAME'..."
  for image in "${IMAGES[@]}"; do
    kind load docker-image "$image" --name "$CLUSTER_NAME"
  done
else
  echo "Kind cluster '$CLUSTER_NAME' not found. Skipping kind load (Docker Desktop uses local images directly)."
fi

echo "Done. Run ./scripts/deploy-k8s.sh to apply manifests."
