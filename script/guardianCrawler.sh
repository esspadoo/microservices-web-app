#!/bin/bash
set -e #exit if any commands exit with non zero status

GUARDIAN_API_URL="https://content.guardianapis.com/search"
API_KEY="5eaa4909-873b-4eac-b07c-9ef331376ff8"
PAGES=10
PAGE_SIZE=50
OUTPUT_FILE="../all_data/guardian.jsonl"

#check if curl and jq are available
type curl >/dev/null 2>&1 || { echo >&2 "Required curl but it's not installed. Aborting."; exit 1; }
type jq > /dev/null 2>&1 || { echo >&2 "Required jq but it's not installed. Aborting."; exit 1; }

#creating directory if doesn't exists
mkdir -p "$(dirname "$OUTPUT_FILE")"

#clearing file if already exists
echo "" > "$OUTPUT_FILE"

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
done

echo "Success! Processed full bodies for $(wc -l < "$OUTPUT_FILE") articles."