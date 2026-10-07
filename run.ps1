# Start CompanyWisePrep at http://localhost:8090
#   .\run.ps1          build the UI if it has never been built, then start
#   .\run.ps1 -Build   rebuild the UI first (after changing frontend/)
#
# Database: if .env exists, its DB_URL / DB_USERNAME / DB_PASSWORD are used (Neon Postgres);
# without it the app uses a local H2 file in .local/. See .env.example.
param([switch]$Build)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

if (Test-Path .env) {
    foreach ($line in Get-Content .env) {
        if ($line -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*?)\s*$' -and -not $line.TrimStart().StartsWith('#')) {
            $value = $Matches[2].Trim('"').Trim("'")
            if ($value) { [Environment]::SetEnvironmentVariable($Matches[1], $value, 'Process') }
        }
    }
    if ($env:DB_URL) { Write-Host "Database: $(($env:DB_URL -split '\?')[0])" }
} else {
    Write-Host 'Database: local H2 (.local/) — create .env from .env.example to use Neon'
}

$static = 'src/main/resources/static/index.html'
if ($Build -or -not (Test-Path $static)) {
    Push-Location frontend
    if (-not (Test-Path node_modules)) { npm install --no-audit --no-fund }
    npm run build
    Pop-Location
}

Write-Host 'Starting on http://localhost:8090 (Ctrl+C to stop)'
.\mvnw.cmd -q spring-boot:run
