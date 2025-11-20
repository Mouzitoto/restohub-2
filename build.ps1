# Build script for RestoHub project
# This script builds all projects locally before Docker deployment

Write-Host "Building RestoHub projects..." -ForegroundColor Green

# Build client-api
Write-Host "Building client-api..." -ForegroundColor Yellow
Set-Location client-api
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build client-api" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Build admin-api
Write-Host "Building admin-api..." -ForegroundColor Yellow
Set-Location admin-api
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build admin-api" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Build client-web
Write-Host "Building client-web..." -ForegroundColor Yellow
Set-Location client-web
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to install client-web dependencies" -ForegroundColor Red
    Set-Location ..
    exit 1
}
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build client-web" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Build admin-web
Write-Host "Building admin-web..." -ForegroundColor Yellow
Set-Location admin-web
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to install admin-web dependencies" -ForegroundColor Red
    Set-Location ..
    exit 1
}
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build admin-web" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

Write-Host "All projects built successfully!" -ForegroundColor Green

