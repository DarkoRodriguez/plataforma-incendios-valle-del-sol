Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RootDir = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $RootDir

$Namespace = 'plataforma-incendios'
$Images = @(
    'plataforma/ms-users:latest',
    'plataforma/ms-reports:latest',
    'plataforma/ms-alerts:latest',
    'plataforma/ms-bff:latest',
    'plataforma/mfe-mapeo:latest'
)

$Manifests = @(
    'k8s/00-namespace.yaml',
    'k8s/01-postgres-configmap.yaml',
    'k8s/02-postgres-pvc.yaml',
    'k8s/03-postgres-deployment.yaml',
    'k8s/04-postgres-service.yaml',
    'k8s/05-minio-pvc.yaml',
    'k8s/06-minio-deployment.yaml',
    'k8s/07-minio-service.yaml',
    'k8s/09-ms-users-deployment.yaml',
    'k8s/10-ms-users-service.yaml',
    'k8s/11-ms-reports-deployment.yaml',
    'k8s/12-ms-reports-service.yaml',
    'k8s/13-ms-alerts-deployment.yaml',
    'k8s/14-ms-alerts-service.yaml',
    'k8s/15-ms-bff-deployment.yaml',
    'k8s/16-ms-bff-service.yaml',
    'k8s/17-krakend-configmap.yaml',
    'k8s/18-krakend-deployment.yaml',
    'k8s/19-krakend-service.yaml',
    'k8s/20-mfe-mapeo-deployment.yaml',
    'k8s/21-mfe-mapeo-service.yaml',
    'k8s/22-ingress.yaml'
)

$Deployments = @(
    'postgres', 'minio', 'ms-users', 'ms-reports', 'ms-alerts', 'ms-bff', 'krakend', 'mfe-mapeo'
)

function Require-Command($Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command not found: $Name"
    }
}

function Get-EnvValue($Key) {
    if (-not (Test-Path .env)) { return $null }
    $line = Get-Content .env | Where-Object { $_ -match "^\s*$Key=" } | Select-Object -Last 1
    if (-not $line) { return $null }
    return ($line -replace "^\s*$Key=", '')
}

Require-Command docker
Require-Command kubectl

kubectl cluster-info | Out-Null

if (-not (Test-Path .env)) {
    throw '.env not found. Copy .env.example to .env and fill in VAPID key values.'
}
if (-not (Test-Path private_key.pem) -or -not (Test-Path public_key.pem)) {
    throw 'private_key.pem and public_key.pem must exist in the repository root.'
}

$vapidPublicKey = Get-EnvValue 'VAPID_PUBLIC_KEY'
$vapidPrivateKey = Get-EnvValue 'VAPID_PRIVATE_KEY'
$vapidSubject = Get-EnvValue 'VAPID_SUBJECT'

if (-not $vapidPublicKey -or -not $vapidPrivateKey) {
    throw 'VAPID_PUBLIC_KEY and VAPID_PRIVATE_KEY must be set in .env.'
}
if (-not $vapidSubject) {
    $vapidSubject = 'mailto:admin@example.com'
}

$ingressClass = kubectl get ingressclass traefik 2>$null
if (-not $ingressClass) {
    Write-Host 'Traefik not found. Installing...'
    if (Get-Command helm -ErrorAction SilentlyContinue) {
        & "$RootDir/scripts/install-traefik.ps1"
    } else {
        throw 'Traefik is not installed and Helm is unavailable. Install Helm 3 or run scripts/install-traefik.ps1 manually.'
    }
}

Write-Host 'Building Docker images...'
docker build -t plataforma/ms-users:latest backend/ms-users
docker build -t plataforma/ms-reports:latest backend/ms-reports
docker build -t plataforma/ms-alerts:latest backend/ms-alerts
docker build -t plataforma/ms-bff:latest backend/bff
docker build `
    --build-arg NGINX_CONF=nginx.k8s.conf `
    --build-arg VITE_VAPID_PUBLIC_KEY=$vapidPublicKey `
    --build-arg VITE_API_BASE=/api `
    -t plataforma/mfe-mapeo:latest frontend/mfe-mapeo

$context = kubectl config current-context
if ($context -like 'kind-*') {
    $clusterName = $context.Substring(5)
    Write-Host "Loading images into kind cluster '$clusterName'..."
    foreach ($image in $Images) {
        kind load docker-image $image --name $clusterName
    }
}

Write-Host 'Creating namespace and secrets...'
kubectl apply -f k8s/00-namespace.yaml

kubectl create secret generic ms-alerts-keys `
    --from-literal="VAPID_PUBLIC_KEY=$vapidPublicKey" `
    --from-literal="VAPID_PRIVATE_KEY=$vapidPrivateKey" `
    --from-literal="VAPID_SUBJECT=$vapidSubject" `
    -n $Namespace --dry-run=client -o yaml | kubectl apply -f -

kubectl create secret generic ms-usuarios-keys `
    --from-file=private_key.pem=private_key.pem `
    --from-file=public_key.pem=public_key.pem `
    -n $Namespace --dry-run=client -o yaml | kubectl apply -f -

Write-Host 'Applying Kubernetes manifests...'
foreach ($manifest in $Manifests) {
    Write-Host "  Applying $manifest"
    kubectl apply -f $manifest
}

kubectl delete job minio-setup -n $Namespace --ignore-not-found
kubectl apply -f k8s/08-minio-setup-job.yaml

Write-Host 'Waiting for deployments...'
foreach ($deployment in $Deployments) {
    Write-Host "  - deployment/$deployment"
    kubectl rollout status "deployment/$deployment" -n $Namespace --timeout=300s
}

kubectl wait --for=condition=complete job/minio-setup -n $Namespace --timeout=120s

Write-Host ''
Write-Host 'Kubernetes deployment finished.'
Write-Host 'Traffic flow: browser -> Traefik -> (frontend | Krakend -> BFF -> microservices)'
Write-Host ''
Write-Host 'Access URLs:'
Write-Host '  App (recommended):  http://plataforma.local'
Write-Host '  App (localhost):    http://localhost'
Write-Host '  Swagger UI:         http://plataforma.local/swagger-ui'
Write-Host '  MinIO API:          http://localhost:30090'
Write-Host '  MinIO Console:      http://localhost:30091'
Write-Host ''
Write-Host 'Add to C:\Windows\System32\drivers\etc\hosts if needed:'
Write-Host '  127.0.0.1 plataforma.local'
