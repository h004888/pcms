#!/bin/bash
# =====================================================
# PCMS - Stop all running services (started via run-local.sh)
# =====================================================

echo "Stopping all PCMS services..."
# Find java processes whose main class is in our package
pkill -f "com.pcms.*Application" 2>/dev/null && echo "  ✅ Stopped" || echo "  ℹ No running services"
