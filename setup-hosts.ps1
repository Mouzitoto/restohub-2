# Script for setting up local domains in Windows
# Requires administrator privileges

$hostsPath = "$env:SystemRoot\System32\drivers\etc\hosts"
$domains = @(
    "127.0.0.1 restohub.local",
    "127.0.0.1 partner.restohub.local",
    "127.0.0.1 api.restohub.local"
)

Write-Host "Setting up local domains for resto-hub..." -ForegroundColor Cyan

# Check administrator privileges
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "ERROR: Script must be run as administrator!" -ForegroundColor Red
    Write-Host "Right-click on file -> Run as administrator" -ForegroundColor Yellow
    exit 1
}

# Read current hosts file
$hostsContent = Get-Content $hostsPath -ErrorAction Stop

# Check which domains are already added
$domainsToAdd = @()
foreach ($domain in $domains) {
    $domainName = ($domain -split '\s+')[1]
    $exists = $hostsContent | Where-Object { $_ -match [regex]::Escape($domainName) }
    
    if ($exists) {
        Write-Host "Domain $domainName is already configured" -ForegroundColor Green
    } else {
        $domainsToAdd += $domain
        Write-Host "Adding domain: $domainName" -ForegroundColor Yellow
    }
}

# Add new domains
if ($domainsToAdd.Count -gt 0) {
    $maxRetries = 5
    $retryCount = 0
    $success = $false
    
    while ($retryCount -lt $maxRetries -and -not $success) {
        try {
            # Create backup
            $backupPath = "$hostsPath.backup.$(Get-Date -Format 'yyyyMMdd-HHmmss')"
            Copy-Item $hostsPath $backupPath -Force
            Write-Host "Backup created: $backupPath" -ForegroundColor Gray
            
            # Read current content
            $currentContent = Get-Content $hostsPath -Raw -ErrorAction Stop
            
            # Prepare new content
            $newContent = $currentContent.TrimEnd()
            if (-not $newContent.EndsWith("`n") -and -not $newContent.EndsWith("`r`n")) {
                $newContent += "`r`n"
            }
            $newContent += "`r`n# Resto-Hub local domains`r`n"
            foreach ($domain in $domainsToAdd) {
                $newContent += "$domain`r`n"
            }
            
            # Write using .NET method for better file handling
            [System.IO.File]::WriteAllText($hostsPath, $newContent, [System.Text.Encoding]::UTF8)
            
            $success = $true
            Write-Host ""
            Write-Host "Domains successfully added!" -ForegroundColor Green
            Write-Host "You can now use:" -ForegroundColor Cyan
            Write-Host "  - http://restohub.local" -ForegroundColor White
            Write-Host "  - http://partner.restohub.local" -ForegroundColor White
            Write-Host "  - http://api.restohub.local" -ForegroundColor White
        } catch {
            $retryCount++
            if ($retryCount -lt $maxRetries) {
                Write-Host "File is locked, retrying in 2 seconds... (attempt $retryCount/$maxRetries)" -ForegroundColor Yellow
                Start-Sleep -Seconds 2
            } else {
                Write-Host "ERROR writing to hosts file: $_" -ForegroundColor Red
                Write-Host "The file may be locked by another process (antivirus, editor, etc.)" -ForegroundColor Yellow
                Write-Host "Try:" -ForegroundColor Yellow
                Write-Host "  1. Close any editors that might have the file open" -ForegroundColor White
                Write-Host "  2. Temporarily disable antivirus" -ForegroundColor White
                Write-Host "  3. Edit the file manually: $hostsPath" -ForegroundColor White
                Write-Host "     Add these lines:" -ForegroundColor White
                foreach ($domain in $domainsToAdd) {
                    Write-Host "       $domain" -ForegroundColor Gray
                }
                exit 1
            }
        }
    }
} else {
    Write-Host ""
    Write-Host "All domains are already configured!" -ForegroundColor Green
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
