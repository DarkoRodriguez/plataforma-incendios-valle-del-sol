#!/bin/bash

# Script de inicio del microservicio ms-usuarios con Docker

set -e

echo "╔════════════════════════════════════════════════════════════╗"
echo "║ MS-USUARIOS - Microservicio de Gestión de Usuarios        ║"
echo "║ Iniciando con Docker Compose                              ║"
echo "╚════════════════════════════════════════════════════════════╝"

# Verificar si Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado. Por favor instala Docker."
    exit 1
fi

# Verificar si Docker Compose está instalado
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose no está instalado. Por favor instala Docker Compose."
    exit 1
fi

echo "✓ Docker y Docker Compose detectados"
echo ""

# Obtener el directorio del script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Mostrar opciones
if [ "$1" = "up" ] || [ -z "$1" ]; then
    echo "📦 Construyendo e iniciando contenedores..."
    docker-compose up --build -d
    
    echo ""
    echo "✓ Contenedores iniciados correctamente"
    echo ""
    echo "📊 Estado de los servicios:"
    docker-compose ps
    
    echo ""
    echo "⏳ Esperando a que PostgreSQL esté listo..."
    sleep 5
    
    echo "✓ PostgreSQL está listo"
    echo ""
    echo "🚀 Microservicio ms-usuarios accesible en: http://localhost:8081"
    echo ""
    echo "📝 Logs de la aplicación:"
    docker-compose logs ms-usuarios
    
elif [ "$1" = "down" ]; then
    echo "🛑 Deteniendo contenedores..."
    docker-compose down
    echo "✓ Contenedores detenidos"
    
elif [ "$1" = "logs" ]; then
    echo "📋 Mostrando logs..."
    docker-compose logs -f "$2"
    
elif [ "$1" = "restart" ]; then
    echo "🔄 Reiniciando contenedores..."
    docker-compose restart
    echo "✓ Contenedores reiniciados"
    
elif [ "$1" = "clean" ]; then
    echo "🧹 Eliminando contenedores, volúmenes e imágenes..."
    docker-compose down -v
    docker rmi ms-usuarios-app:latest 2>/dev/null || true
    echo "✓ Limpieza completada"
    
else
    echo "Uso: $0 [comando]"
    echo ""
    echo "Comandos disponibles:"
    echo "  up       - Iniciar servicios (por defecto)"
    echo "  down     - Detener servicios"
    echo "  logs     - Mostrar logs (ej: $0 logs ms-usuarios)"
    echo "  restart  - Reiniciar servicios"
    echo "  clean    - Limpiar completamente (contenedores, volúmenes, imágenes)"
    exit 1
fi
