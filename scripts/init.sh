#!/bin/bash

echo "Downloading model ..."

curl -L \
  https://huggingface.co/username/repo/resolve/main/model.model \
  -o ../inferer/src/main/resources/inferer/inferer.model.tmp && \
mv ../inferer/src/main/resources/inferer/inferer.model

source "compileProject.sh"

set -euo pipefail
FORCE_GUARDIAN=false

for arg in "$@"; do
  case "$arg" in
    --guardian-force)
      FORCE_GUARDIAN=true
      ;;
  esac
done

GUARDIAN_FILE="../all_data/guardian.jsonl"

if [ "$FORCE_GUARDIAN" = true ]; then
  source "guardianCrawler.sh"
else
  if [ ! -f "$GUARDIAN_FILE" ]; then
    source "guardianCrawler.sh"
  fi
fi
#------------------------
source "py_init.sh"
#-------------------
cd ..
sudo docker compose up -d --build
echo "Service running..."
echo "..."
echo "..."
echo "Starting to import data"
cd ./all_data/


echo "Waiting for importer service..."
until curl -s http://localhost:8882/api/v1/importer/hello | grep -q "INDEXER UP"; do
  sleep 2
done
echo "Importer service is ready."


echo "Importing... THE GUARDIAN"
curl -X POST http://localhost:8882/api/v1/importer/import \
  -F "file=@guardian.jsonl" \
  -F "indexName=guardian"
  
sleep 5
until curl -s http://localhost:8882/api/v1/importer/hello | grep -q "INDEXER UP"; do
  sleep 2
done

echo ""
echo "Importing... OWI"
echo ""
curl -X POST http://localhost:8882/api/v1/importer/import \
  -F "file=@owi.json" \
  -F "indexName=owi"
sleep 15
echo "The search app is now running..."
echo "Connect to http://localhost:8080/ to use it"
