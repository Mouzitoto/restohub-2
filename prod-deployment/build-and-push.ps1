# Скрипт для сборки и пуша Docker образов в Docker Hub (PowerShell)
# Использование: .\build-and-push.ps1 [version]
# Пример: .\build-and-push.ps1 1.0.0

param(
    [string]$Version = "latest"
)

# Установка кодировки UTF-8 для корректного отображения
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

$ErrorActionPreference = "Stop"

# Установка переменных окружения по умолчанию (если не заданы)
if (-not $env:DOCKER_HUB_USERNAME) {
    $env:DOCKER_HUB_USERNAME = "mouzitoto"
}
if (-not $env:DOCKER_HUB_REPO) {
    $env:DOCKER_HUB_REPO = "resto-hub-repo"
}
if (-not $env:VITE_API_BASE_URL) {
    $env:VITE_API_BASE_URL = "https://api.restohub.kz"
}
if (-not $env:VITE_PARTNER_DOMAIN) {
    $env:VITE_PARTNER_DOMAIN = "https://partner.restohub.kz"
}

# Проверка переменных окружения
if (-not $env:DOCKER_HUB_USERNAME) {
    Write-Host "Ошибка: Установите переменную DOCKER_HUB_USERNAME" -ForegroundColor Red
    Write-Host "Пример: `$env:DOCKER_HUB_USERNAME='your-username'" -ForegroundColor Yellow
    exit 1
}

if (-not $env:DOCKER_HUB_REPO) {
    Write-Host "Ошибка: Установите переменную DOCKER_HUB_REPO" -ForegroundColor Red
    Write-Host "Пример: `$env:DOCKER_HUB_REPO='resto-hub-repo'" -ForegroundColor Yellow
    exit 1
}

$ImageBase = "$env:DOCKER_HUB_USERNAME/$env:DOCKER_HUB_REPO"

Write-Host "=== Сборка и публикация образов Resto-Hub ===" -ForegroundColor Green
Write-Host "Docker Hub пользователь: $env:DOCKER_HUB_USERNAME" -ForegroundColor Yellow
Write-Host "Docker Hub репозиторий: $env:DOCKER_HUB_REPO" -ForegroundColor Yellow
Write-Host "Версия: $Version" -ForegroundColor Yellow
Write-Host ""

# Переход в корень проекта
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

# Проверка авторизации в Docker Hub
Write-Host "Проверка авторизации в Docker Hub..." -ForegroundColor Green
try {
    $null = docker info 2>&1 | Select-String "Username"
} catch {
    Write-Host "Требуется авторизация в Docker Hub" -ForegroundColor Yellow
    docker login
}

# Сборка и публикация admin-api
Write-Host ""
Write-Host "[1/5] Сборка admin-api..." -ForegroundColor Green
docker build -t "${ImageBase}:admin-api-${Version}" -t "${ImageBase}:admin-api-latest" ./admin-api
if ($LASTEXITCODE -ne 0) { exit 1 }
Write-Host "[1/5] Публикация admin-api..." -ForegroundColor Green
docker push "${ImageBase}:admin-api-${Version}"
if ($LASTEXITCODE -ne 0) { exit 1 }
docker push "${ImageBase}:admin-api-latest"
if ($LASTEXITCODE -ne 0) { exit 1 }

# Сборка и публикация client-api
Write-Host ""
Write-Host "[2/5] Сборка client-api..." -ForegroundColor Green
docker build -t "${ImageBase}:client-api-${Version}" -t "${ImageBase}:client-api-latest" ./client-api
if ($LASTEXITCODE -ne 0) { exit 1 }
Write-Host "[2/5] Публикация client-api..." -ForegroundColor Green
docker push "${ImageBase}:client-api-${Version}"
if ($LASTEXITCODE -ne 0) { exit 1 }
docker push "${ImageBase}:client-api-latest"
if ($LASTEXITCODE -ne 0) { exit 1 }

# Сборка и публикация client-web
Write-Host ""
Write-Host "[3/5] Сборка client-web..." -ForegroundColor Green
$ViteApiBaseUrl = if ($env:VITE_API_BASE_URL) { $env:VITE_API_BASE_URL } else { "https://api.restohub.kz" }
$VitePartnerDomain = if ($env:VITE_PARTNER_DOMAIN) { $env:VITE_PARTNER_DOMAIN } else { "https://partner.restohub.kz" }
docker build `
    --build-arg VITE_API_BASE_URL=$ViteApiBaseUrl `
    --build-arg VITE_PARTNER_DOMAIN=$VitePartnerDomain `
    -t "${ImageBase}:client-web-${Version}" `
    -t "${ImageBase}:client-web-latest" `
    ./client-web
if ($LASTEXITCODE -ne 0) { exit 1 }
Write-Host "[3/5] Публикация client-web..." -ForegroundColor Green
docker push "${ImageBase}:client-web-${Version}"
if ($LASTEXITCODE -ne 0) { exit 1 }
docker push "${ImageBase}:client-web-latest"
if ($LASTEXITCODE -ne 0) { exit 1 }

# Сборка и публикация admin-web
Write-Host ""
Write-Host "[4/5] Сборка admin-web..." -ForegroundColor Green
docker build `
    --build-arg VITE_API_BASE_URL=$ViteApiBaseUrl `
    -t "${ImageBase}:admin-web-${Version}" `
    -t "${ImageBase}:admin-web-latest" `
    ./admin-web
if ($LASTEXITCODE -ne 0) { exit 1 }
Write-Host "[4/5] Публикация admin-web..." -ForegroundColor Green
docker push "${ImageBase}:admin-web-${Version}"
if ($LASTEXITCODE -ne 0) { exit 1 }
docker push "${ImageBase}:admin-web-latest"
if ($LASTEXITCODE -ne 0) { exit 1 }

# Сборка и публикация nginx
Write-Host ""
Write-Host "[5/5] Сборка nginx..." -ForegroundColor Green
docker build -t "${ImageBase}:nginx-${Version}" -t "${ImageBase}:nginx-latest" ./nginx
if ($LASTEXITCODE -ne 0) { exit 1 }
Write-Host "[5/5] Публикация nginx..." -ForegroundColor Green
docker push "${ImageBase}:nginx-${Version}"
if ($LASTEXITCODE -ne 0) { exit 1 }
docker push "${ImageBase}:nginx-latest"
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host ""
Write-Host "=== Все образы успешно собраны и опубликованы! ===" -ForegroundColor Green
Write-Host "Версия: $Version" -ForegroundColor Yellow
Write-Host "Docker Hub: https://hub.docker.com/r/$ImageBase" -ForegroundColor Yellow
