#!/bin/bash
set -e #exit if any commands exit with non zero status

GUARDIAN_API_URL="https://content.guardianapis.com/search"
API_KEY="5eaa4909-873b-4eac-b07c-9ef331376ff8"
PAGES=100
PAGE_SIZE=50
OUTPUT_FILE="../all_data/guardian.jsonl"

#check if curl and jq are available
type curl >/dev/null 2>&1 || { echo >&2 "Required curl but it's not installed. Aborting."; exit 1; }
type jq > /dev/null 2>&1 || { echo >&2 "Required jq but it's not installed. Aborting."; exit 1; }

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

#performing API request to the guardian api
echo "Retrieving articles from The Guardian"
for (( page = 1; page <= PAGES; page++ )); do
    curl -s "$GUARDIAN_API_URL?page-size=$PAGE_SIZE&api-key=$API_KEY&show-fields=bodyText&page=$page" | \
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
