# Integration Test Runner Script for Windows PowerShell
# This script runs integration tests with Docker containers via Testcontainers

$ErrorActionPreference = "Stop"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Running Integration Tests" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

# Check if Docker is running
try {
    $dockerInfo = docker info 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Docker is not running. Please start Docker first." -ForegroundColor Red
        exit 1
    }
    Write-Host "Docker is running." -ForegroundColor Green
} catch {
    Write-Host "Error: Docker is not running or not installed. Please start Docker first." -ForegroundColor Red
    exit 1
}

Write-Host ""

# Check if Maven is installed
try {
    $mvnVersion = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "Maven version: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "Error: Maven is not installed or not in PATH." -ForegroundColor Red
    exit 1
}

Write-Host ""

# Change to project root
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Join-Path $scriptDir ".."
Set-Location $projectRoot

Write-Host "Project directory: $(Get-Location)" -ForegroundColor Yellow
Write-Host ""

# Clean and compile
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Step 1: Clean and Compile" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

# Run integration tests
Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Step 2: Run Integration Tests" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=test

if ($LASTEXITCODE -ne 0) {
    Write-Host "Tests failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Green
Write-Host "Integration Tests Completed" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Green
