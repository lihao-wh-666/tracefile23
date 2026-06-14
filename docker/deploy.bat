@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   Exam System Docker Deployment
echo ========================================
echo.

set "DOCKER_CMD=docker"
set "COMPOSE_CMD=docker compose"

where docker >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker not found! Please install Docker Desktop first.
    echo Download: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker daemon is not running! Please start Docker Desktop.
    pause
    exit /b 1
)

cd /d "%~dp0"

echo [1/6] Cleaning up old containers and volumes...
%COMPOSE_CMD% down -v --remove-orphans 2>nul
echo.

echo [2/6] Validating docker-compose.yml...
%COMPOSE_CMD% config -q
if %errorlevel% neq 0 (
    echo [ERROR] docker-compose.yml validation failed!
    pause
    exit /b 1
)
echo     Validation passed.
echo.

echo [3/6] Building backend image...
%COMPOSE_CMD% build --no-cache backend
if %errorlevel% neq 0 (
    echo [ERROR] Backend image build failed!
    echo.
    echo Possible solutions:
    echo 1. Check network connection
    echo 2. Configure Docker mirror source:
    echo    Go to Docker Desktop Settings ^> Docker Engine
    echo    Add registry mirrors configuration
    pause
    exit /b 1
)
echo     Backend build complete.
echo.

echo [4/6] Building frontend image...
%COMPOSE_CMD% build --no-cache frontend
if %errorlevel% neq 0 (
    echo [ERROR] Frontend image build failed!
    pause
    exit /b 1
)
echo     Frontend build complete.
echo.

echo [5/6] Starting all containers...
%COMPOSE_CMD% up -d
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start containers!
    pause
    exit /b 1
)
echo     Containers started.
echo.

echo [6/6] Waiting for services to be ready...
set MAX_WAIT=180
set WAIT_INTERVAL=10
set ELAPSED=0
set BACKEND_READY=0
set FRONTEND_READY=0

:wait_loop
if %ELAPSED% geq %MAX_WAIT% (
    echo.
    echo [WARNING] Timeout waiting for services to be ready.
    echo Please check container logs manually:
    echo   docker compose logs -f backend
    echo   docker compose logs -f frontend
    goto show_result
)

timeout /t %WAIT_INTERVAL% /nobreak >nul
set /a ELAPSED+=WAIT_INTERVAL

if %BACKEND_READY% equ 0 (
    curl -s -o nul -w "%%{http_code}" http://localhost:6080/api/dashboard/stats 2>nul | findstr /r "^200$ ^401$ ^403$" >nul
    if !errorlevel! equ 0 (
        set BACKEND_READY=1
        echo     [OK] Backend is ready (took %ELAPSED%s)
    ) else (
        echo     Waiting for backend... (%ELAPSED%/%MAX_WAIT%s)
    )
)

if %FRONTEND_READY% equ 0 (
    curl -s -o nul -w "%%{http_code}" http://localhost:6070/ 2>nul | findstr "^200$" >nul
    if !errorlevel! equ 0 (
        set FRONTEND_READY=1
        echo     [OK] Frontend is ready (took %ELAPSED%s)
    )
)

if %BACKEND_READY% equ 1 if %FRONTEND_READY% equ 1 goto show_result
goto wait_loop

:show_result
echo.
echo ========================================
echo   Deployment Complete!
echo ========================================
echo.
echo Service URLs:
echo   Frontend: http://localhost:6070
echo   Backend API: http://localhost:6080/api
echo   MySQL: localhost:3306 (root / root123)
echo   Redis: localhost:6379
echo.
echo Default Accounts:
echo   Admin: admin / admin123
echo   Teacher: teacher1~teacher8 / teacher123
echo   Student: student1~student30 / student123
echo.
echo Useful Commands:
echo   View logs:       docker compose logs -f [backend^|frontend^|mysql^|redis]
echo   Stop services:   docker compose stop
echo   Start services:  docker compose start
echo   Restart:         docker compose restart
echo   Remove all:      docker compose down -v
echo.
echo Container Status:
%COMPOSE_CMD% ps
echo.
pause
