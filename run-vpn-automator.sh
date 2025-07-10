#!/bin/bash

# VPN Automator Application Runner
# This script creates a standalone runner for your JavaFX application

# Configuration
APP_NAME="VPN Automator"
MAIN_CLASS="vpn_automation.Main"
JAVA_OPTS="-Xmx1024m -Xms512m"

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$SCRIPT_DIR/app"
GRADLEW="$SCRIPT_DIR/gradlew"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Java is available
check_java() {
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        print_error "Please install Java 21 or later"
        exit 1
    fi
    
    # Check Java version
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 21 ]; then
        print_error "Java version $JAVA_VERSION is too old. Please install Java 21 or later"
        exit 1
    fi
    
    print_status "Java version: $(java -version 2>&1 | head -n1)"
}

# Build the application if needed
build_app() {
    print_status "Building application..."
    cd "$APP_DIR"
    
    if [ ! -f "build.gradle.kts" ]; then
        print_error "build.gradle.kts not found in $APP_DIR"
        exit 1
    fi
    
    # Build the application
    "$GRADLEW" :app:build --no-daemon
    if [ $? -ne 0 ]; then
        print_error "Failed to build application"
        exit 1
    fi
    
    print_status "Application built successfully"
}

# Run the application
run_app() {
    print_status "Starting $APP_NAME..."
    cd "$APP_DIR"
    
    # Run using gradle
    "$GRADLEW" :app:runGui --no-daemon
    
    if [ $? -ne 0 ]; then
        print_error "Failed to start application"
        exit 1
    fi
}

# Create app image using jpackage
create_app_image() {
    print_status "Creating application image..."
    cd "$APP_DIR"
    
    # First build the application
    "$GRADLEW" :app:build --no-daemon
    if [ $? -ne 0 ]; then
        print_error "Failed to build application"
        return 1
    fi
    
    # Create the app image
    "$GRADLEW" :app:packageApp --no-daemon
    if [ $? -eq 0 ]; then
        print_status "Application image created successfully!"
        print_status "Location: $APP_DIR/build/app-image/"
        print_status "Executable: $APP_DIR/build/app-image/VPNAutomator/bin/VPNAutomator"
    else
        print_warning "Failed to create app image with jpackage"
        print_warning "Falling back to JAR-based distribution"
        create_jar_distribution
    fi
}

# Create a fat jar (uber jar)
create_fat_jar() {
    print_status "Creating fat jar..."
    cd "$APP_DIR"
    
    # Build the shadow jar
    "$GRADLEW" :app:shadowJar --no-daemon
    if [ $? -ne 0 ]; then
        print_error "Failed to create fat jar"
        return 1
    fi
    
    # Create distribution directory
    DIST_DIR="$APP_DIR/build/fat-jar-dist"
    mkdir -p "$DIST_DIR"
    
    # Find and copy the fat jar (shadowJar creates app-fat.jar)
    FAT_JAR_SOURCE=$(find build/libs -name "app-fat.jar" -o -name "*-all.jar" -o -name "app.jar" | head -n1)
    if [ -z "$FAT_JAR_SOURCE" ]; then
        # Fallback to any jar file
        FAT_JAR_SOURCE=$(ls build/libs/*.jar | head -n1)
    fi
    
    if [ -f "$FAT_JAR_SOURCE" ]; then
        cp "$FAT_JAR_SOURCE" "$DIST_DIR/vpn-automator-fat.jar"
    else
        print_error "No jar file found in build/libs/"
        return 1
    fi
    
    # Create a simple run script
    cat > "$DIST_DIR/run-fat-jar.sh" << 'EOF'
#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FAT_JAR="$SCRIPT_DIR/vpn-automator-fat.jar"

if [ ! -f "$FAT_JAR" ]; then
    echo "Error: Fat jar not found at $FAT_JAR"
    exit 1
fi

java --add-modules javafx.controls,javafx.fxml,javafx.web \
     -Xmx1024m -Xms512m \
     -jar "$FAT_JAR" "$@"
EOF
    
    chmod +x "$DIST_DIR/run-fat-jar.sh"
    
    print_status "Fat jar created successfully!"
    print_status "Location: $DIST_DIR/vpn-automator-fat.jar"
    print_status "Run with: java -jar $DIST_DIR/vpn-automator-fat.jar"
    print_status "Or use the script: $DIST_DIR/run-fat-jar.sh"
}

# Create a JAR-based distribution
create_jar_distribution() {
    print_status "Creating JAR-based distribution..."
    cd "$APP_DIR"
    
    # Build the application
    ./gradlew build --no-daemon
    if [ $? -ne 0 ]; then
        print_error "Failed to build application"
        return 1
    fi
    
    # Create distribution directory
    DIST_DIR="$APP_DIR/build/distribution"
    mkdir -p "$DIST_DIR"
    
    # Copy JAR file
    cp build/libs/*.jar "$DIST_DIR/"
    
    # Copy dependencies
    ./gradlew copyDependencies --no-daemon 2>/dev/null || {
        # If copyDependencies task doesn't exist, create it
        print_status "Creating dependencies copy..."
        mkdir -p "$DIST_DIR/lib"
        
        # Use gradle to list and copy dependencies
        ./gradlew dependencies --configuration runtimeClasspath --no-daemon > /dev/null
        
        # Get runtime classpath
        CLASSPATH=$(./gradlew printClasspath --no-daemon --quiet 2>/dev/null || echo "")
        
        if [ -n "$CLASSPATH" ]; then
            # Copy each dependency
            echo "$CLASSPATH" | tr ':' '\n' | while read -r jar; do
                if [ -f "$jar" ] && [[ "$jar" != */build/classes/* ]]; then
                    cp "$jar" "$DIST_DIR/lib/" 2>/dev/null || true
                fi
            done
        fi
    }
    
    # Create run script
    cat > "$DIST_DIR/run-vpn-automator.sh" << 'EOF'
#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE=$(ls "$SCRIPT_DIR"/*.jar | head -n1)
CLASSPATH="$JAR_FILE:$SCRIPT_DIR/lib/*"

java -cp "$CLASSPATH" \
     --module-path "$SCRIPT_DIR/lib" \
     --add-modules javafx.controls,javafx.fxml,javafx.web \
     -Xmx1024m -Xms512m \
     vpn_automation.Main "$@"
EOF
    
    chmod +x "$DIST_DIR/run-vpn-automator.sh"
    
    print_status "JAR distribution created successfully!"
    print_status "Location: $DIST_DIR"
    print_status "Run with: $DIST_DIR/run-vpn-automator.sh"
}

# Create installer
create_installer() {
    print_status "Creating installer..."
    cd "$APP_DIR"
    
    ./gradlew createInstaller --no-daemon
    if [ $? -eq 0 ]; then
        print_status "Installer created successfully!"
        print_status "Location: $APP_DIR/build/installer/"
    else
        print_error "Failed to create installer"
        return 1
    fi
}

# Main script logic
main() {
    print_status "VPN Automator Application Runner"
    print_status "================================"
    
    # Check prerequisites
    check_java
    
    case "${1:-run}" in
        "run")
            build_app
            run_app
            ;;
        "build")
            build_app
            print_status "Build completed successfully"
            ;;
        "package")
            create_app_image
            ;;
        "jar")
            create_jar_distribution
            ;;
        "fat-jar")
            create_fat_jar
            ;;
        "installer")
            create_installer
            ;;
        "help"|"-h"|"--help")
            echo "Usage: $0 [command]"
            echo ""
            echo "Commands:"
            echo "  run       - Build and run the application (default)"
            echo "  build     - Build the application only"
            echo "  package   - Create a native app image"
            echo "  jar       - Create a JAR-based distribution"
            echo "  fat-jar   - Create a fat jar (includes all dependencies)"
            echo "  installer - Create a platform-specific installer"
            echo "  help      - Show this help message"
            ;;
        *)
            print_error "Unknown command: $1"
            print_error "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
