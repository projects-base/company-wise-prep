# Start CompanyWisePrep at http://localhost:8090
#   .\run.ps1          build the UI if it has never been built, then start
#   .\run.ps1 -Build   rebuild the UI first (after changing frontend/)
param([switch]$Build)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$static = 'src/main/resources/static/index.html'
if ($Build -or -not (Test-Path $static)) {
    Push-Location frontend
    if (-not (Test-Path node_modules)) { npm install --no-audit --no-fund }
    npm run build
    Pop-Location
}

Write-Host 'Starting on http://localhost:8090 (Ctrl+C to stop)'
.\mvnw.cmd -q spring-boot:run
