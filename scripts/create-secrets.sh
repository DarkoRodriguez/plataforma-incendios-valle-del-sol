#!/usr/bin/env bash
set -euo pipefail

NS=plataforma-incendios

echo "This script is optional. Prefer ./scripts/deploy-k8s.sh which creates secrets automatically."
echo "Creating namespace ${NS}..."
kubectl apply -f k8s/00-namespace.yaml

if [[ -f ./private_key.pem && -f ./public_key.pem ]]; then
  kubectl create secret generic ms-usuarios-keys \
    --from-file=private_key.pem=./private_key.pem \
    --from-file=public_key.pem=./public_key.pem \
    -n "${NS}" --dry-run=client -o yaml | kubectl apply -f -
  echo "Applied secret ms-usuarios-keys"
else
  echo "Warning: private_key.pem/public_key.pem not found in project root."
fi

if [[ -f ./.env ]]; then
  VAPID_PUBLIC_KEY="$(grep -E '^[[:space:]]*VAPID_PUBLIC_KEY=' .env | tail -n 1 | cut -d'=' -f2-)"
  VAPID_PRIVATE_KEY="$(grep -E '^[[:space:]]*VAPID_PRIVATE_KEY=' .env | tail -n 1 | cut -d'=' -f2-)"
  VAPID_SUBJECT="$(grep -E '^[[:space:]]*VAPID_SUBJECT=' .env | tail -n 1 | cut -d'=' -f2-)"
  VAPID_SUBJECT="${VAPID_SUBJECT:-mailto:admin@example.com}"

  if [[ -n "${VAPID_PUBLIC_KEY}" && -n "${VAPID_PRIVATE_KEY}" ]]; then
    kubectl create secret generic ms-alerts-keys \
      --from-literal=VAPID_PUBLIC_KEY="${VAPID_PUBLIC_KEY}" \
      --from-literal=VAPID_PRIVATE_KEY="${VAPID_PRIVATE_KEY}" \
      --from-literal=VAPID_SUBJECT="${VAPID_SUBJECT}" \
      -n "${NS}" --dry-run=client -o yaml | kubectl apply -f -
    echo "Applied secret ms-alerts-keys"
  else
    echo "Skipped ms-alerts-keys: set VAPID keys in .env"
  fi
else
  echo "Skipped ms-alerts-keys: .env not found"
fi
