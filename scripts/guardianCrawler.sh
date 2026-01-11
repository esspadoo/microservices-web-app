#!/bin/bash
set -e #exit if any commands exit with non zero status

GUARDIAN_API_URL="https://content.guardianapis.com/search?tag="
API_KEY="5eaa4909-873b-4eac-b07c-9ef331376ff8"
PAGES=100
PAGE_SIZE=50
OUTPUT_FILE="../all_data/guardian.jsonl"

GUARDIAN_TAGS=(
    "science/science"
    "technology/technology"
    "advertising/research"
    "technology/computing"
    "technology/artificialintelligence"
    "technology/software"
    "technology/games"
    "technology/internet"
    "technology/data-security"
    "technology/hacking"
    "technology/data-protection"
    "artanddesign/graphic-design"
    "artanddesign/digital-art"
)

check_installed() {
    local PACKAGE=$1

    if command -v "$PACKAGE" >/dev/null 2>&1; then
        echo "$PACKAGE is already installed."
    else
        echo "$PACKAGE not found. Attempting to install..."
        install_package "$PACKAGE"
    fi
}

# Helper function to handle the OS-specific logic
install_package() {
    local PKG=$1

    case "$OSTYPE" in
        darwin*)
            if command -v brew >/dev/null 2>&1; then
                brew install "$PKG"
            else
                echo "❌ Homebrew not found. Install it at https://brew.sh/"
                return 1
            fi
            ;;
        linux-gnu*)
            if [ -f /etc/debian_version ]; then
                sudo apt-get update && sudo apt-get install -y "$PKG"
            elif [ -f /etc/arch-release ]; then
                sudo pacman -Syu --noconfirm "$PKG"
            else
                echo "Unsupported Linux distro."
                return 1
            fi
            ;;
        *)
            echo "OS $OSTYPE not supported."
            return 1
            ;;
    esac
}

#check if curl and jq are available
check_installed "curl"
check_installed "jq"

#creating directory if doesn't exists
mkdir -p "$(dirname "$OUTPUT_FILE")"

#clearing file if already exists
echo "" > "$OUTPUT_FILE"

#progress bar setup
if [ -t 1 ]; then
    TERM_WIDTH=${COLUMNS:-80}
    RESERVED_WIDTH=35
    BAR_WIDTH=$(( TERM_WIDTH - RESERVED_WIDTH ))
    (( BAR_WIDTH < 10 )) && BAR_WIDTH=10
    (( BAR_WIDTH > 60 )) && BAR_WIDTH=60
else
    BAR_WIDTH=0
fi

#adding tags
JOINED_TAGS=$(IFS="|"; echo "${GUARDIAN_TAGS[*]}")
TAGS_URL="${GUARDIAN_API_URL}${JOINED_TAGS}"

#performing API request to the guardian api
echo "Retrieving articles from The Guardian"
for (( page = 1; page <= PAGES; page++ )); do
    curl -s "$TAGS_URL&page-size=$PAGE_SIZE&api-key=$API_KEY&show-fields=bodyText&page=$page" | \
    jq -c '.response.results[] | {
      id: .id,
      title: .webTitle,
      url: .webUrl,
      main_content: (.fields.bodyText // "")
    }' >> "$OUTPUT_FILE"

    if (( BAR_WIDTH > 0 )); then
            percent=$(( page * 100 / PAGES ))
            filled=$(( page * BAR_WIDTH / PAGES ))
            empty=$(( BAR_WIDTH - filled ))

            bar="$(printf '%*s' "$filled" '' | tr ' ' '#')"
            bar+=$(printf '%*s' "$empty" '')

            printf "\rProgress: [%s] %3d%% (%d/%d pages)" \
                "$bar" "$percent" "$page" "$PAGES"
        fi
done

# Final newline to avoid prompt overlap
[ -t 1 ] && echo

echo "Success! Processed full bodies for $(wc -l < "$OUTPUT_FILE") articles."
