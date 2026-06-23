Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath
Set-Location ..

function ThrowIfMissingCommand($name) {
    if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
        throw "Required command not found: $name"
    }
}

ThrowIfMissingCommand docker
ThrowIfMissingCommand kubectl

Write-Host "✅ Docker and kubectl detected"

try {
    kubectl cluster-info | Out-Null
} catch {
    throw "kubectl cannot reach a Kubernetes cluster. Enable Docker Desktop Kubernetes or set KUBECONFIG to a valid cluster."
}

Write-Host "✅ Kubernetes cluster reachable"
Write-Host "➡ Building Docker images"

docker build -t plataforma/ms-users:latest backend/ms-users
docker build -t plataforma/ms-reports:latest backend/ms-reports
docker build -t plataforma/ms-alerts:latest backend/ms-alerts
docker build -t plataforma/ms-bff:latest backend/bff
docker build -t plataforma/mfe-mapeo:latest frontend/mfe-mapeo

Write-Host "➡ Creating namespace"
kubectl apply -f k8s/namespace.yaml

if (-not (Test-Path private_key.pem) -or -not (Test-Path public_key.pem)) {
    throw "private_key.pem and/or public_key.pem not found in repository root. Generate or copy them before running this script."
}

Write-Host "➡ Creating JWT key secret for ms-usuarios"
& kubectl create secret generic ms-usuarios-keys --from-file=private_key.pem=private_key.pem --from-file=public_key.pem=public_key.pem -n plataforma-incendios --dry-run=client -o yaml | kubectl apply -f -

if (-not (Test-Path .env)) {
    throw ".env not found. Copy .env.example to .env and fill in VAPID key values."
}

$envLines = Get-Content .env
function Get-EnvValue($key) {
    $line = $envLines | Where-Object { $_ -match "^\s*$key=" } | Select-Object -Last 1
    if (-not $line) { return $null }
    return $line -replace "^\s*$key=", ""
}

$vapidPublicKey = Get-EnvValue 'VAPID_PUBLIC_KEY'
$vapidPrivateKey = Get-EnvValue 'VAPID_PRIVATE_KEY'
$vapidSubject = Get-EnvValue 'VAPID_SUBJECT'

if (-not $vapidPublicKey -or -not $vapidPrivateKey) {
    throw "VAPID_PUBLIC_KEY and VAPID_PRIVATE_KEY must be set in .env to create ms-alerts secret."
}
if (-not $vapidSubject) {
    $vapidSubject = 'mailto:admin@example.com'
}

Write-Host "➡ Creating VAPID secret for ms-alerts"
& kubectl create secret generic ms-alerts-keys --from-literal="VAPID_PUBLIC_KEY=$vapidPublicKey" --from-literal="VAPID_PRIVATE_KEY=$vapidPrivateKey" --from-literal="VAPID_SUBJECT=$vapidSubject" -n plataforma-incendios --dry-run=client -o yaml | kubectl apply -f -

Write-Host "➡ Applying Kubernetes manifests"
$applyFiles = @(
    'k8s/postgres/configmap.yaml',
    'k8s/postgres/pvc.yaml',
    'k8s/postgres/deployment.yaml',
    'k8s/postgres/service.yaml',
    'k8s/minio/pvc.yaml',
    'k8s/minio/deployment.yaml',
    'k8s/minio/service.yaml',
    'k8s/krakend/configmap.yaml',
    'k8s/krakend/deployment.yaml',
    'k8s/krakend/service.yaml',
    'backend/ms-users/k8s/deployment.yaml',
    'backend/ms-users/k8s/service.yaml',
    'backend/ms-reports/k8s/deployment.yaml',
    'backend/ms-reports/k8s/service.yaml',
    'backend/ms-alerts/k8s/deployment.yaml',
    'backend/ms-alerts/k8s/service.yaml',
    'backend/bff/k8s/deployment.yaml',
    'backend/bff/k8s/service.yaml',
    'frontend/mfe-mapeo/k8s/deployment.yaml',
    'frontend/mfe-mapeo/k8s/service.yaml',
    'k8s/ingress/ingress.yaml'
)

foreach ($file in $applyFiles) {
    Write-Host "Applying $file"
    kubectl apply -f $file
}

Write-Host "➡ Waiting for deployments to become ready"
$deployments = @('postgres','minio','krakend','ms-usuarios','ms-reports','ms-alerts','ms-bff','mfe-mapeo')
foreach ($deployment in $deployments) {
    Write-Host "Waiting for deployment/$deployment..."
    try {
        kubectl rollout status deployment/$deployment -n plataforma-incendios --timeout=180s | Out-Null
    } catch {
        Write-Warning "Deployment $deployment did not reach ready state within timeout. Check pod logs with kubectl -n plataforma-incendios logs deployment/$deployment"
    }
}

Write-Host "✅ Kubernetes deployment finished"
Write-Host "Local access URLs:"
Write-Host "  Frontend: http://localhost:30080"
Write-Host "  BFF gateway: http://localhost:30081"
Write-Host "  MinIO API: http://localhost:30090"
Write-Host "  MinIO Console: http://localhost:30091"
Write-Host "  BFF swagger / openapi: http://localhost:30081/swagger-ui"
Write-Host "  Optional ingress path (if Traefik ingress and host routing are configured): http://localhost/api"
