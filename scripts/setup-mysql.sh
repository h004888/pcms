#!/bin/bash
# =====================================================
# PCMS - Setup MySQL user + 12 databases
# Run: ./scripts/setup-mysql.sh
# =====================================================

set -e
echo "======================================"
echo "  PCMS - MySQL Setup"
echo "======================================"
echo ""
echo "This script will create:"
echo "  - User: pcms_user (password: pcms_pass)"
echo "  - 12 databases: pcms_user, pcms_branch, ..., pcms_report"
echo ""
echo "It will prompt for MySQL root password."
echo ""

# Find script directory
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

# Use mysql client to run setup script
mysql -u root -p < "$SCRIPT_DIR/setup-mysql.sql"

echo ""
echo "✅ MySQL setup complete!"
echo ""
echo "Verify connection with new user:"
echo "  mysql -u pcms_user -ppcms_pass -e 'SHOW DATABASES LIKE \"pcms_%\";'"
