#!/bin/bash
set -e

echo "========================================"
echo "  Exam System Docker Deployment"
echo "========================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! command -v docker &> /dev/null; then
    echo "[ERROR] Docker not found! Please install Docker first."
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "[ERROR] Docker daemon is not running! Please start Docker."
    exit 1
fi

echo "[1/6] Cleaning up old containers and volumes..."
docker compose down -v --remove-orphans 2>/dev/null || true
echo ""

echo "[2/6] Validating docker-compose.yml..."
if ! docker compose config -q; then
    echo "[ERROR] docker-compose.yml validation failed!"
    exit 1
fi
echo "    Validation passed."
echo ""

echo "[3/6] Building backend image..."
if ! docker compose build --no-cache backend; then
    echo "[ERROR] Backend image build failed!"
    echo ""
    echo "Possible solutions:"
    echo "1. Check network connection"
    echo "2. Configure Docker mirror source in /etc/docker/daemon.json"
    exit 1
fi
echo "    Backend build complete."
echo ""

echo "[4/6] Building frontend image..."
if ! docker compose build --no-cache frontend; then
    echo "[ERROR] Frontend image build failed!"
    exit 1
fi
echo "    Frontend build complete."
echo ""

echo "[5/6] Starting all containers..."
if ! docker compose up -d; then
    echo "[ERROR] Failed to start containers!"
    exit 1
fi
echo "    Containers started."
echo ""

echo "[6/6] Waiting for services to be ready..."
MAX_WAIT=180
WAIT_INTERVAL=10
ELAPSED=0
BACKEND_READY=0
FRONTEND_READY=0

while [ $ELAPSED -lt $MAX_WAIT ]; do
    sleep $WAIT_INTERVAL
    ELAPSED=$((ELAPSED + WAIT_INTERVAL))

    if [ $BACKEND_READY -eq 0 ]; then
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:6080/api/dashboard/stats 2>/dev/null || echo "000")
        if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
            BACKEND_READY=1
            echo "    [OK] Backend is ready (took ${ELAPSED}s)"
        else
            echo "    Waiting for backend... (${ELAPSED}/${MAX_WAIT}s)"
        fi
    fi

    if [ $FRONTEND_READY -eq 0 ]; then
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:6070/ 2>/dev/null || echo "000")
        if [ "$HTTP_CODE" = "200" ]; then
            FRONTEND_READY=1
            echo "    [OK] Frontend is ready (took ${ELAPSED}s)"
        fi
    fi

    if [ $BACKEND_READY -eq 1 ] && [ $FRONTEND_READY -eq 1 ]; then
        break
    fi
done

echo ""
echo "========================================"
echo "  Deployment Complete!"
echo "========================================"
echo ""
echo "Service URLs:"
echo "  Frontend:    http://localhost:6070"
echo "  Backend API: http://localhost:6080/api"
echo "  MySQL:       localhost:3306 (root / root123)"
echo "  Redis:       localhost:6379"
echo ""
echo "Default Accounts:"
echo "  Admin:   admin / admin123"
echo "  Teacher: teacher1~teacher8 / teacher123"
echo "  Student: student1~student30 / student123"
echo ""
echo "Useful Commands:"
echo "  View logs:       docker compose logs -f [backend|frontend|mysql|redis]"
echo "  Stop services:   docker compose stop"
echo "  Start services:  docker compose start"
echo "  Restart:         docker compose restart"
echo "  Remove all:      docker compose down -v"
echo ""
echo "Container Status:"
docker compose ps
