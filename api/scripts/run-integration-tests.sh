#!/bin/bash

# Integration Test Runner Script
# This script runs integration tests with Docker containers via Testcontainers

set -e

echo "======================================"
echo "Running Integration Tests"
echo "======================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker first."
    exit 1
fi

echo "Docker is running."
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH."
    exit 1
fi

echo "Maven version:"
mvn -version
echo ""

# Change to project root
cd "$(dirname "$0")/.."

echo "Project directory: $(pwd)"
echo ""

# Clean and compile
echo "======================================"
echo "Step 1: Clean and Compile"
echo "======================================"
mvn clean compile

# Run integration tests
echo ""
echo "======================================"
echo "Step 2: Run Integration Tests"
echo "======================================"
mvn test -Dtest="asdf" -Dspring.profiles.active=test

echo ""
echo "======================================"
echo "Integration Tests Completed"
echo "======================================"
