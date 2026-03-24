param(
    [int]$Port = 55432,
    [string]$User = "postgres",
    [string]$Password = "postgres",
    [string]$Database = "tradelab"
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$dataDir = Join-Path $root ".pgdata"
$logFile = Join-Path $root ".pglog"
$passwordFile = Join-Path $root ".pgpassfile"

$pgBin = $null
$initdb = Get-Command initdb.exe -ErrorAction SilentlyContinue
if ($initdb) {
    $pgBin = Split-Path -Parent $initdb.Source
} elseif (Test-Path "C:\Program Files\PostgreSQL\18\bin\initdb.exe") {
    $pgBin = "C:\Program Files\PostgreSQL\18\bin"
} else {
    throw "PostgreSQL binaries not found. Install PostgreSQL or add its bin directory to PATH."
}

$initdbExe = Join-Path $pgBin "initdb.exe"
$pgCtlExe = Join-Path $pgBin "pg_ctl.exe"
$psqlExe = Join-Path $pgBin "psql.exe"
$pgIsReadyExe = Join-Path $pgBin "pg_isready.exe"

if (-not (Test-Path $dataDir)) {
    Set-Content -Path $passwordFile -Value $Password -NoNewline
    & $initdbExe -D $dataDir -U $User -A scram-sha-256 --pwfile=$passwordFile | Out-Null
}

$alreadyRunning = $false
& $pgIsReadyExe -h localhost -p $Port -U $User | Out-Null
if ($LASTEXITCODE -eq 0) {
    $alreadyRunning = $true
}

if (-not $alreadyRunning) {
    & $pgCtlExe -D $dataDir -l $logFile -o "`"-p $Port`"" start | Out-Null
}

$ready = $false
for ($i = 0; $i -lt 20; $i++) {
    & $pgIsReadyExe -h localhost -p $Port -U $User | Out-Null
    if ($LASTEXITCODE -eq 0) {
        $ready = $true
        break
    }
    Start-Sleep -Milliseconds 500
}

if (-not $ready) {
    throw "Local PostgreSQL failed to start on port $Port."
}

$env:PGPASSWORD = $Password
& $psqlExe -h localhost -p $Port -U $User -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$Database'" | ForEach-Object {
    if (-not $_.Trim()) {
        & $psqlExe -h localhost -p $Port -U $User -d postgres -c "CREATE DATABASE $Database;" | Out-Null
    }
}

Write-Output "Local PostgreSQL is running on localhost:$Port"
Write-Output "Database: $Database"
Write-Output "User: $User"
