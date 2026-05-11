#!/bin/bash

# Script maestro para gestionar todos los servicios de la plataforma
# Ubicación: plataforma-incendios-valle-del-sol/

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Variables
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="docker-compose.yml"

# Función para imprimir con color
print_header() {
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║ PLATAFORMA INCENDIOS - VALLE DEL SOL                       ║${NC}"
    echo -e "${BLUE}║ $1${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Función para verificar requisitos
check_requirements() {
    echo ""
    print_info "Verificando requisitos..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker no está instalado"
        exit 1
    fi
    print_success "Docker instalado"
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose no está instalado"
        exit 1
    fi
    print_success "Docker Compose instalado"
}

# Función: Iniciar servicios
start_services() {
    print_header "Iniciando Servicios"
    
    check_requirements
    
    echo ""
    print_info "Construyendo e iniciando contenedores..."
    docker-compose -f "$COMPOSE_FILE" up --build -d
    
    print_success "Contenedores iniciados"
    echo ""
    
    print_info "Estado de servicios:"
    docker-compose -f "$COMPOSE_FILE" ps
    
    echo ""
    print_info "Esperando a que PostgreSQL esté listo..."
    sleep 5
    
    print_success "PostgreSQL está listo"
    echo ""
    
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}🚀 SERVICIOS INICIADOS CORRECTAMENTE${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "${YELLOW}📍 URLS DE LOS SERVICIOS:${NC}"
    echo -e "  • MS-USUARIOS:  ${BLUE}http://localhost:8081${NC}"
    echo -e "  • MS-MAPEO:     ${BLUE}http://localhost:8082${NC}"
    echo -e "  • DEMO:         ${BLUE}http://localhost:8083${NC}"
    echo -e "  • PgAdmin:      ${BLUE}http://localhost:5050${NC}"
    echo -e "  • PostgreSQL:   ${BLUE}localhost:5432${NC}"
    echo ""
    echo -e "${YELLOW}📊 CREDENCIALES BD:${NC}"
    echo -e "  • Usuario: ${BLUE}postgres${NC}"
    echo -e "  • Contraseña: ${BLUE}postgres${NC}"
    echo ""
    echo -e "${YELLOW}📊 CREDENCIALES PGADMIN:${NC}"
    echo -e "  • Email: ${BLUE}admin@example.com${NC}"
    echo -e "  • Contraseña: ${BLUE}admin${NC}"
    echo ""
}

# Función: Detener servicios
stop_services() {
    print_header "Deteniendo Servicios"
    echo ""
    print_info "Deteniendo contenedores..."
    docker-compose -f "$COMPOSE_FILE" down
    print_success "Servicios detenidos"
}

# Función: Reiniciar servicios
restart_services() {
    print_header "Reiniciando Servicios"
    echo ""
    print_info "Reiniciando contenedores..."
    docker-compose -f "$COMPOSE_FILE" restart
    print_success "Servicios reiniciados"
    echo ""
    print_info "Estado de servicios:"
    docker-compose -f "$COMPOSE_FILE" ps
}

# Función: Ver logs
show_logs() {
    local service=$1
    if [ -z "$service" ]; then
        echo -e "${YELLOW}Servicio no especificado. Opciones:${NC}"
        echo "  • postgres"
        echo "  • ms-usuarios"
        echo "  • ms-mapeo"
        echo "  • demo"
        echo "  • pgadmin"
        echo ""
        echo "Uso: $0 logs [servicio]"
        return 1
    fi
    
    print_header "Logs: $service"
    docker-compose -f "$COMPOSE_FILE" logs -f "$service"
}

# Función: Estado de servicios
status_services() {
    print_header "Estado de Servicios"
    echo ""
    docker-compose -f "$COMPOSE_FILE" ps
}

# Función: Limpiar
clean_services() {
    print_header "Limpieza Completa"
    echo ""
    print_info "Esto eliminará contenedores, volúmenes e imágenes..."
    read -p "¿Estás seguro? (s/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        print_info "Deteniendo servicios..."
        docker-compose -f "$COMPOSE_FILE" down -v
        print_info "Eliminando imágenes..."
        docker rmi ms-usuarios:latest ms-mapeo:latest demo:latest 2>/dev/null || true
        print_success "Limpieza completada"
    else
        print_info "Operación cancelada"
    fi
}

# Función: Acceder a BD
access_db() {
    local db=${1:-ms_usuarios}
    print_header "Accediendo a Base de Datos: $db"
    echo ""
    docker-compose -f "$COMPOSE_FILE" exec postgres psql -U postgres -d "$db"
}

# Función: Ver ayuda
show_help() {
    cat << EOF
${BLUE}PLATAFORMA INCENDIOS - VALLE DEL SOL${NC}

${YELLOW}Uso: $0 [comando]${NC}

${YELLOW}Comandos:${NC}
  up          - Iniciar todos los servicios (por defecto)
  down        - Detener todos los servicios
  restart     - Reiniciar servicios
  status      - Ver estado de los servicios
  logs        - Ver logs de un servicio (ej: $0 logs ms-usuarios)
  db [nombre] - Acceder a PostgreSQL (ej: $0 db ms_usuarios)
  clean       - Limpiar todo (contenedores, volúmenes, imágenes)
  help        - Mostrar esta ayuda

${YELLOW}Servicios Disponibles:${NC}
  • ms-usuarios   - Microservicio de gestión de usuarios (puerto 8081)
  • ms-mapeo      - Microservicio de mapeo (puerto 8082)
  • demo          - Servicio de demostración (puerto 8083)
  • pgadmin       - Administrador de PostgreSQL (puerto 5050)
  • postgres      - Base de datos PostgreSQL (puerto 5432)

${YELLOW}Ejemplos:${NC}
  $0 up                    # Iniciar servicios
  $0 logs ms-usuarios      # Ver logs de ms-usuarios
  $0 db ms_usuarios        # Acceder a la BD ms_usuarios
  $0 status                # Ver estado
  $0 clean                 # Limpieza completa

EOF
}

# Función principal
main() {
    cd "$SCRIPT_DIR"
    
    case "${1:-up}" in
        up)
            start_services
            ;;
        down)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        status)
            status_services
            ;;
        logs)
            show_logs "$2"
            ;;
        db)
            access_db "$2"
            ;;
        clean)
            clean_services
            ;;
        help|-h|--help)
            show_help
            ;;
        *)
            print_error "Comando desconocido: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Ejecutar función principal
main "$@"
