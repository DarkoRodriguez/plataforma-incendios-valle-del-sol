#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLUSTER_NAME="${KIND_CLUSTER_NAME:-plataforma}"

if ! command -v kind >/dev/null 2>&1; then
  echo "kind is not installed. See https://kind.sigs.k8s.io/docs/user/quick-start/#installation"
  exit 1
fi

if kind get clusters 2>/dev/null | grep -qx "$CLUSTER_NAME"; then
  echo "Kind cluster '$CLUSTER_NAME' already exists."
  kubectl cluster-info --context "kind-${CLUSTER_NAME}"
  exit 0
fi

echo "Creating kind cluster '$CLUSTER_NAME'..."
kind create cluster --name "$CLUSTER_NAME" --config "$ROOT_DIR/kind-config.yaml"

echo "Kind cluster ready. Context: kind-${CLUSTER_NAME}"
