# Deploys the API to Google Cloud Run from this folder (Cloud Build builds the Dockerfile).
# One-time setup and the secrets are in docs/DEPLOY.md ("Google Cloud Run"). Secrets (DB_*,
# APP_PASSWORD, APP_TOKEN_SECRET, CORS_ORIGINS, API keys) are set once on the service and kept
# across deploys; this script never touches them.
#
#   .\deploy-gcp.ps1                 # deploy main as it is on disk
#   .\deploy-gcp.ps1 -Region asia-south1
param(
    [string]$Region = 'asia-southeast1',   # Singapore, next to the Neon database
    [string]$Service = 'company-wise-prep-api'
)
$ErrorActionPreference = 'Stop'

# gcloud is on PATH after a new terminal; until then, use the default per-user install.
$gcloud = (Get-Command gcloud -ErrorAction SilentlyContinue).Source
if (-not $gcloud) { $gcloud = Join-Path $env:LOCALAPPDATA 'Google\Cloud SDK\google-cloud-sdk\bin\gcloud.cmd' }
if (-not (Test-Path $gcloud)) { throw 'gcloud not found: install the Google Cloud CLI (docs/DEPLOY.md)' }
# gcloud prints progress on stderr, which Windows PowerShell turns into errors under 'Stop':
# judge it by its exit code instead.
$ErrorActionPreference = 'Continue'

# 1 GiB fits the API plus one judge JVM, so Run/Submit work on the hosted app too.
# max-instances 1: one user, and it caps the bill. min-instances 0: sleeps when idle (free).
$javaOpts = '-Xmx400m -Xss512k -XX:+UseSerialGC -XX:MaxMetaspaceSize=160m -XX:TieredStopAtLevel=1'
& $gcloud run deploy $Service `
    --source . `
    --region $Region `
    --memory 1Gi --cpu 1 `
    --min-instances 0 --max-instances 1 `
    --concurrency 20 --timeout 300 `
    --cpu-boost `
    --allow-unauthenticated --quiet `
    --update-env-vars "AUTH_ENABLED=true,CODE_RUNNER_ENABLED=true,JUDGE_HEAP=256m,JUDGE_PARALLEL=1,JAVA_OPTS=$javaOpts"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$url = & $gcloud run services describe $Service --region $Region --format 'value(status.url)'
Write-Host "API: $url  (check $url/api/auth/status)"
