#!/bin/bash
set -e

# ------------------------------
# Step 1: Detect OS / Distro
# ------------------------------
OS="$(uname -s)"

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
# Step 2: Locate Java 21 (do NOT touch system Java)
# ------------------------------
JAVA21_HOME=""

if [ "$OS" = "Darwin" ]; then
    JAVA21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || true)

elif [ "$OS" = "Linux" ]; then
    if command -v java >/dev/null 2>&1; then
        JAVA_PATH=$(command -v java)
        if command -v readlink >/dev/null 2>&1; then
            CANDIDATE_HOME=$(dirname "$(dirname "$(readlink -f "$JAVA_PATH")")")
            if "$CANDIDATE_HOME/bin/java" -version 2>&1 | grep -q '"21'; then
                JAVA21_HOME="$CANDIDATE_HOME"
            fi
        fi
    fi
fi

# ------------------------------
# Step 3: Install Java 21 if missing
# ------------------------------
if [ -z "$JAVA21_HOME" ]; then
    echo "Java 21 not found. Installing locally..."

    if [ "$OS" = "Darwin" ]; then
        if ! command -v brew >/dev/null 2>&1; then
            echo "Homebrew not found. Please install Homebrew."
            exit 1
        fi
        brew install --cask temurin@21
        JAVA21_HOME=$(/usr/libexec/java_home -v 21)

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
                JAVA21_HOME="/opt/temurin21"
                ;;
            arch)
                sudo pacman -Sy --noconfirm jdk21-temurin
                JAVA21_HOME="/usr/lib/jvm/java-21-temurin"
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

# ------------------------------
# Step 4: Use Java 21 ONLY for this script
# ------------------------------
export JAVA_HOME="$JAVA21_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using Java for build:"
"$JAVA_HOME/bin/java" -version

# ------------------------------
# Step 5: Resolve project root
# ------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# ------------------------------
# Step 6: Build modules using mvnw
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

    # Normalize CRLF line endings (macOS-safe)
    sed -i.bak 's/\r$//' "$MVNW" 2>/dev/null || true

    echo "Building module: $module"
    chmod +x "$MVNW"
    "$MVNW" -f "$MODULE_DIR/pom.xml" clean package
done

echo "All modules compiled successfully with Java 21!"
