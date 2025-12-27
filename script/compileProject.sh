#!/bin/bash
set -e  # Exit immediately if a command fails

# ------------------------------
# Step 1: Detect Java
# ------------------------------
if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME not set. Checking for Java installation..."

    JAVA_PATH=$(which java || true)

    if [ -z "$JAVA_PATH" ]; then
        echo "Java not found. Installing Temurin 21..."
        
        # Detect OS and install accordingly
        if [[ "$OSTYPE" == "linux-gnu"* ]]; then
		sudo apt update
		sudo apt install -y wget tar
		TEMURIN_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.9+10/OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz"
		TEMPDIR=$(mktemp -d)
		wget -O "$TEMPDIR/temurin21.tar.gz" "$TEMURIN_URL"
		sudo mkdir -p /opt/temurin21
		sudo tar -xzf "$TEMPDIR/temurin21.tar.gz" -C /opt/temurin21 --strip-components=1
		JAVA_HOME="/opt/temurin21"
	elif [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS - use brew
            if ! command -v brew &> /dev/null; then
                echo "Homebrew not found. Please install Homebrew first."
                exit 1
            fi
            brew install temurin21
            JAVA_HOME=$(/usr/libexec/java_home -v 21)
        else
            echo "Unsupported OS: $OSTYPE. Please install Java manually."
            exit 1
        fi

        export JAVA_HOME
        export PATH=$JAVA_HOME/bin:$PATH
        echo "Java installed and JAVA_HOME set to $JAVA_HOME"
    else
        # Java found, set JAVA_HOME automatically
        JAVA_HOME=$(dirname $(dirname "$JAVA_PATH"))
        export JAVA_HOME
        export PATH=$JAVA_HOME/bin:$PATH
        echo "JAVA_HOME set to $JAVA_HOME"
    fi
fi

# Verify Java
java -version

# ------------------------------
# Step 2: Build modules using module-specific mvnw
# ------------------------------
SCRIPT_DIR=$(dirname "$0")
PROJECT_ROOT=$(realpath "$SCRIPT_DIR/..")

for module in inferer searcher importer; do
    MODULE_DIR="$PROJECT_ROOT/$module"
    MVNW="$MODULE_DIR/mvnw"

    if [ -d "$MODULE_DIR" ]; then
        if [ -f "$MVNW" ]; then
            echo "Building module: $module using its mvnw"
            chmod +x "$MVNW"  # ensure executable
            "$MVNW" -f "$MODULE_DIR/pom.xml" clean package
        else
            echo "Maven wrapper not found in module $module"
            exit 1
        fi
    else
        echo "Module directory not found: $module"
        exit 1
    fi
done

echo "All modules compiled successfully!"

