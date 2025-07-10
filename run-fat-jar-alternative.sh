#!/bin/bash

# Alternative Fat Jar Runner for VPN Automator
# This script runs the fat jar using the JavaFX libraries from gradle cache

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FAT_JAR="$SCRIPT_DIR/app/build/fat-jar-dist/vpn-automation-fat.jar"

echo "VPN Automator Fat Jar Alternative Runner"
echo "========================================"

# Check if fat jar exists
if [ ! -f "$FAT_JAR" ]; then
    echo "Error: Fat jar not found at $FAT_JAR"
    echo "Run './run-vpn-automator.sh fat-jar' first to create it"
    exit 1
fi

echo "✅ Fat jar found: $FAT_JAR"

# Get JavaFX module path from gradle cache
GRADLE_CACHE="$HOME/.gradle/caches/modules-2/files-2.1"
JAVAFX_MODULES=""

# Find JavaFX modules in gradle cache
for module in javafx-base javafx-controls javafx-graphics javafx-fxml javafx-web javafx-media; do
    MODULE_PATH=$(find "$GRADLE_CACHE" -path "*org.openjfx/$module/21/*linux.jar" 2>/dev/null | head -n1)
    if [ -n "$MODULE_PATH" ]; then
        if [ -z "$JAVAFX_MODULES" ]; then
            JAVAFX_MODULES="$MODULE_PATH"
        else
            JAVAFX_MODULES="$JAVAFX_MODULES:$MODULE_PATH"
        fi
        echo "Found: $module -> $MODULE_PATH"
    fi
done

if [ -z "$JAVAFX_MODULES" ]; then
    echo "❌ JavaFX modules not found in gradle cache"
    echo "Try running: ./run-vpn-automator.sh run"
    echo "This will download JavaFX dependencies to the gradle cache"
    exit 1
fi

echo ""
echo "🚀 Starting VPN Automator with JavaFX modules..."
echo "Module path: $JAVAFX_MODULES"
echo ""

# Run the fat jar with proper JavaFX setup
java -Xmx1024m -Xms512m \
     --module-path "$JAVAFX_MODULES" \
     --add-modules javafx.controls,javafx.fxml,javafx.web \
     -Djava.awt.headless=false \
     -jar "$FAT_JAR" "$@"

echo ""
echo "Application finished."
