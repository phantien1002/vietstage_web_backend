$ErrorActionPreference = 'Stop'

$backend = Split-Path -Parent $MyInvocation.MyCommand.Path
$workspace = Split-Path -Parent $backend
$frontend = Join-Path $workspace 'vietstage_web'
$runtimeLogs = Join-Path $env:TEMP 'VietStage'

New-Item -ItemType Directory -Path $runtimeLogs -Force | Out-Null

function Test-Port {
    param([int]$Port)

    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $connection = $client.ConnectAsync('127.0.0.1', $Port)
        return $connection.Wait(500) -and $client.Connected
    }
    catch {
        return $false
    }
    finally {
        $client.Dispose()
    }
}

if (-not (Test-Port -Port 9191)) {
    $jdkCandidates = @(
        @(
            $env:JAVA_HOME,
            'C:\Program Files\Android\Android Studio\jbr'
        ) | Where-Object { $_ -and (Test-Path -LiteralPath (Join-Path $_ 'bin\java.exe')) }
    )

    if ($jdkCandidates.Count -eq 0) {
        throw 'JDK not found. Set JAVA_HOME or install Android Studio JBR.'
    }

    $env:JAVA_HOME = $jdkCandidates[0]
    $env:Path = "$(Join-Path $env:JAVA_HOME 'bin');$env:Path"

    $backendOut = Join-Path $runtimeLogs 'backend.log'
    $backendErr = Join-Path $runtimeLogs 'backend-error.log'
    Start-Process `
        -FilePath (Join-Path $backend 'mvnw.cmd') `
        -ArgumentList 'clean', 'spring-boot:run' `
        -WorkingDirectory $backend `
        -WindowStyle Hidden `
        -RedirectStandardOutput $backendOut `
        -RedirectStandardError $backendErr | Out-Null

    Write-Host 'Starting backend at http://localhost:9191 ...'
    $backendReady = $false
    for ($attempt = 0; $attempt -lt 120; $attempt++) {
        if (Test-Port -Port 9191) {
            $backendReady = $true
            break
        }
        Start-Sleep -Milliseconds 500
    }

    if (-not $backendReady) {
        throw "Backend failed to start. Check logs: $backendOut and $backendErr"
    }
}

if (-not (Test-Port -Port 5173)) {
    $frontendOut = Join-Path $runtimeLogs 'frontend.log'
    $frontendErr = Join-Path $runtimeLogs 'frontend-error.log'
    Start-Process `
        -FilePath 'npm.cmd' `
        -ArgumentList 'run', 'dev' `
        -WorkingDirectory $frontend `
        -WindowStyle Hidden `
        -RedirectStandardOutput $frontendOut `
        -RedirectStandardError $frontendErr | Out-Null
}

Write-Host 'VietStage is ready:'
Write-Host '  Frontend: http://localhost:5173'
Write-Host '  Backend:  http://localhost:9191'
Write-Host "  Log:      $runtimeLogs"
