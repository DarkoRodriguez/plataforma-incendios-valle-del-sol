Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Get-Command helm -ErrorAction SilentlyContinue)) {
    throw 'helm is not installed. Install Helm 3 before running this script.'
}

$ingressClass = kubectl get ingressclass traefik 2>$null
if ($ingressClass) {
    Write-Host 'Traefik IngressClass already installed.'
    exit 0
}

$context = kubectl config current-context
$isKind = $context -like 'kind-*'

helm repo add traefik https://traefik.github.io/charts 2>$null
helm repo update traefik | Out-Null

if ($isKind) {
    Write-Host 'Installing Traefik for kind (hostPort 80/443)...'
    helm upgrade --install traefik traefik/traefik `
        --namespace traefik --create-namespace `
        --set providers.kubernetesIngress.enabled=true `
        --set providers.kubernetesIngress.allowEmptyServices=true `
        --set ingressClass.enabled=true `
        --set ingressClass.isDefaultClass=true `
        --set ingressClass.name=traefik `
        --set service.type=ClusterIP `
        --set ports.web.hostPort=80 `
        --set ports.websecure.hostPort=443
} else {
    Write-Host 'Installing Traefik for local Kubernetes (LoadBalancer)...'
    helm upgrade --install traefik traefik/traefik `
        --namespace traefik --create-namespace `
        --set providers.kubernetesIngress.enabled=true `
        --set providers.kubernetesIngress.allowEmptyServices=true `
        --set ingressClass.enabled=true `
        --set ingressClass.isDefaultClass=true `
        --set ingressClass.name=traefik `
        --set service.type=LoadBalancer
}

kubectl -n traefik rollout status deployment/traefik --timeout=180s
Write-Host "Traefik installed in namespace 'traefik'."
