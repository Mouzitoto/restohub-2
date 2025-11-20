# Скрипт для последовательного запуска контейнеров с выводом логов
# Контейнеры запускаются по очереди, каждый в foreground режиме для просмотра логов

Write-Host "Запуск контейнеров RestoHub по очереди..." -ForegroundColor Green
Write-Host "Каждый контейнер будет запущен последовательно, чтобы видеть логи в консоли" -ForegroundColor Yellow

# Останавливаем существующие контейнеры, если они запущены
Write-Host "`nОстановка существующих контейнеров..." -ForegroundColor Yellow
docker compose down

# 1. Запускаем postgres в фоне и ждем его готовности
Write-Host "`n[1/5] Запуск PostgreSQL..." -ForegroundColor Cyan
docker compose up -d --build postgres
Write-Host "Ожидание готовности PostgreSQL..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$health = ""
do {
    Start-Sleep -Seconds 2
    $health = docker inspect --format='{{.State.Health.Status}}' resto-hub-postgres 2>$null
    $attempt++
    if ($health -eq "healthy") {
        Write-Host "`nPostgreSQL готов!" -ForegroundColor Green
        break
    }
    Write-Host "." -NoNewline
} while ($attempt -lt $maxAttempts)

if ($health -ne "healthy") {
    Write-Host "`nPostgreSQL не стал здоровым за отведенное время" -ForegroundColor Red
    exit 1
}

# Показываем логи postgres
Write-Host "`nЛоги PostgreSQL (нажмите Ctrl+C для перехода к следующему контейнеру):" -ForegroundColor Yellow
Write-Host ("=" * 80) -ForegroundColor Gray
docker compose logs -f postgres

# 2. Запускаем admin-api в фоне, затем показываем логи
Write-Host "`n[2/5] Запуск admin-api..." -ForegroundColor Cyan
docker compose up -d --build admin-api
Start-Sleep -Seconds 3
Write-Host "Логи admin-api (нажмите Ctrl+C для перехода к следующему контейнеру):" -ForegroundColor Yellow
Write-Host ("=" * 80) -ForegroundColor Gray
docker compose logs -f admin-api

# 3. Запускаем client-api в фоне, затем показываем логи
Write-Host "`n[3/5] Запуск client-api..." -ForegroundColor Cyan
docker compose up -d --build client-api
Start-Sleep -Seconds 3
Write-Host "Логи client-api (нажмите Ctrl+C для перехода к следующему контейнеру):" -ForegroundColor Yellow
Write-Host ("=" * 80) -ForegroundColor Gray
docker compose logs -f client-api

# 4. Запускаем client-web в фоне, затем показываем логи
Write-Host "`n[4/5] Запуск client-web..." -ForegroundColor Cyan
docker compose up -d --build client-web
Start-Sleep -Seconds 3
Write-Host "Логи client-web (нажмите Ctrl+C для перехода к следующему контейнеру):" -ForegroundColor Yellow
Write-Host ("=" * 80) -ForegroundColor Gray
docker compose logs -f client-web

# 5. Запускаем admin-web в фоне, затем показываем логи
Write-Host "`n[5/5] Запуск admin-web..." -ForegroundColor Cyan
docker compose up -d --build admin-web
Start-Sleep -Seconds 3
Write-Host "Логи admin-web (нажмите Ctrl+C для остановки всех контейнеров):" -ForegroundColor Yellow
Write-Host ("=" * 80) -ForegroundColor Gray
docker compose logs -f admin-web

Write-Host "`nОстановка всех контейнеров..." -ForegroundColor Yellow
docker compose down
Write-Host "Все контейнеры остановлены." -ForegroundColor Green

