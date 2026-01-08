#!/bin/bash
set -e

# ------------------------------
# Step 1: Detect OS
# ------------------------------
OS="$(uname -s)"

# ------------------------------
# Step 2: Detect distro (Linux only)
# ------------------------------
if [ "$OS" = "Linux" ]; then
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        DISTRO="$ID"
    else
        echo "Cannot detect Linux distribution."
        exit 1
    fi
fi

# ------------------------------
# Step 3: Detect or install Java 21
# ------------------------------
if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME not set. Checking for Java 21..."

    if command -v java >/dev/null 2>&1; then
        if [ "$OS" = "Darwin" ]; then
            JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || true)
        else
            JAVA_PATH=$(command -v java)
            if command -v readlink >/dev/null 2>&1; then
                JAVA_HOME=$(dirname "$(dirname "$(readlink -f "$JAVA_PATH")")")
            fi
        fi
    fi

    if [ -z "$JAVA_HOME" ]; then
        echo "Java 21 not found. Installing..."

        if [ "$OS" = "Darwin" ]; then
            if ! command -v brew >/dev/null 2>&1; then
                echo "Homebrew not found. Please install Homebrew."
                exit 1
            fi
            brew install --cask temurin@21
            JAVA_HOME=$(/usr/libexec/java_home -v 21)

        elif [ "$OS" = "Linux" ]; then
            case "$DISTRO" in
                ubuntu|debian)
                    sudo apt update
                    sudo apt install -y wget tar
                    TEMURIN_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.9+10/OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz"
                    TEMPDIR=$(mktemp -d)
                    wget -O "$TEMPDIR/temurin21.tar.gz" "$TEMURIN_URL"
                    sudo mkdir -p /opt/temurin21
                    sudo tar -xzf "$TEMPDIR/temurin21.tar.gz" -C /opt/temurin21 --strip-components=1
                    JAVA_HOME="/opt/temurin21"
                    ;;
                arch)
                    sudo pacman -Sy --noconfirm jdk21-temurin
                    JAVA_HOME="/usr/lib/jvm/java-21-temurin"
                    ;;
                *)
                    echo "Unsupported Linux distribution: $DISTRO"
                    exit 1
                    ;;
            esac
        else
            echo "Unsupported OS: $OS"
            exit 1
        fi
    fi

    export JAVA_HOME
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "JAVA_HOME set to $JAVA_HOME"
fi

# ------------------------------
# Step 4: Enforce Java 21
# ------------------------------
JAVA_VERSION=$(java -version 2>&1 | awk -F[\".] '/version/ {print $2}')
if [ "$JAVA_VERSION" -ne 21 ]; then
    echo "Java 21 is required. Detected Java $JAVA_VERSION."
    exit 1
fi

java -version

# ------------------------------
# Step 5: Resolve project root
# ------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# ------------------------------
# Step 6: Build modules
# ------------------------------
for module in inferer searcher importer; do
    MODULE_DIR="$PROJECT_ROOT/$module"
    MVNW="$MODULE_DIR/mvnw"

    if [ ! -d "$MODULE_DIR" ]; then
        echo "Module directory not found: $module"
        exit 1
    fi

    if [ ! -f "$MVNW" ]; then
        echo "Maven wrapper not found in module $module"
        exit 1
    fi

    # Normalize line endings (macOS-safe)
    sed -i.bak 's/\r$//' "$MVNW" 2>/dev/null || true

    echo "Building module: $module"
    chmod +x "$MVNW"
    "$MVNW" -f "$MODULE_DIR/pom.xml" clean package
done

echo "All modules compiled successfully!"
