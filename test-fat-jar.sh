#!/bin/bash

# Simple test script for the fat jar

FAT_JAR="app/build/fat-jar-dist/vpn-automator-fat.jar"

echo "Testing VPN Automator Fat Jar"
echo "============================="

# Check if fat jar exists
if [ ! -f "$FAT_JAR" ]; then
    echo "❌ Fat jar not found at $FAT_JAR"
    echo "Run './run-vpn-automator.sh fat-jar' first to create it"
    exit 1
fi

echo "✅ Fat jar found: $FAT_JAR"

# Check jar size
JAR_SIZE=$(du -h "$FAT_JAR" | cut -f1)
echo "📦 Jar size: $JAR_SIZE"

# Check main class in manifest
echo "🔍 Checking main class in manifest..."
MAIN_CLASS=$(unzip -p "$FAT_JAR" META-INF/MANIFEST.MF 2>/dev/null | grep "Main-Class:" | cut -d' ' -f2 | tr -d '\r')
if [ -n "$MAIN_CLASS" ]; then
    echo "✅ Main class: $MAIN_CLASS"
else
    echo "⚠️  No main class found in manifest"
fi

# Check if JavaFX dependencies are included
echo "🔍 Checking JavaFX dependencies..."
JAVAFX_COUNT=$(jar tf "$FAT_JAR" | grep -c "javafx/")
if [ "$JAVAFX_COUNT" -gt 0 ]; then
    echo "✅ JavaFX dependencies included ($JAVAFX_COUNT files)"
else
    echo "❌ JavaFX dependencies not found"
fi

# Check if application classes are included
echo "🔍 Checking application classes..."
APP_COUNT=$(jar tf "$FAT_JAR" | grep -c "vpn_automation/")
if [ "$APP_COUNT" -gt 0 ]; then
    echo "✅ Application classes included ($APP_COUNT files)"
else
    echo "❌ Application classes not found"
fi

echo ""
echo "Fat jar verification complete!"
echo ""
echo "To run the application:"
echo "  java --add-modules javafx.controls,javafx.fxml,javafx.web -jar $FAT_JAR"
echo ""
echo "Or use the provided script:"
echo "  ./app/build/fat-jar-dist/run-fat-jar.sh"
