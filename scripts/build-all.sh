#!/bin/bash
# =====================================================
# PCMS - Build all 15 services
# Run from project root: ./scripts/build-all.sh
# =====================================================

set -e
echo "======================================"
echo "  PCMS - Building all services..."
echo "======================================"

mvn clean install -DskipTests

echo ""
echo "✅ All services built successfully!"
echo "   - JAR files: */target/*.jar"
echo "   - Run with: ./scripts/run-local.sh OR docker-compose up"
