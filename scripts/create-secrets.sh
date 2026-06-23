#!/usr/bin/env bash
set -euo pipefail

NS=plataforma-incendios
kubectl create namespace ${NS} || true

# Create ms-usuarios keys from files (adjust paths)
if [ -f ./private_key.pem ] && [ -f ./public_key.pem ]; then
  kubectl -n ${NS} create secret generic ms-usuarios-keys --from-file=private_key.pem=./private_key.pem --from-file=public_key.pem=./public_key.pem || true
  echo "Created secret ms-usuarios-keys"
else
  echo "Warning: private_key.pem/public_key.pem not found in project root. Create ms-usuarios-keys secret manually."
fi

# Create ms-alerts secret using environment variables or manual replacement
read -p "VAPID_PUBLIC_KEY (or leave empty to skip): " VAPID_PUBLIC_KEY
read -p "VAPID_PRIVATE_KEY (or leave empty to skip): " VAPID_PRIVATE_KEY
read -p "VAPID_SUBJECT (or leave empty to skip): " VAPID_SUBJECT

if [ -n "${VAPID_PUBLIC_KEY}" ] && [ -n "${VAPID_PRIVATE_KEY}" ]; then
  kubectl -n ${NS} create secret generic ms-alerts-keys --from-literal=VAPID_PUBLIC_KEY="${VAPID_PUBLIC_KEY}" --from-literal=VAPID_PRIVATE_KEY="${VAPID_PRIVATE_KEY}" --from-literal=VAPID_SUBJECT="${VAPID_SUBJECT}" || true
  echo "Created secret ms-alerts-keys"
else
  echo "Skipped creating ms-alerts-keys. You can apply k8s/ms-alerts-secret.yaml after replacing placeholders."
fi

