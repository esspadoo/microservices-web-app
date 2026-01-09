#!/bin/bash

set -euo pipefail
FORCE_GUARDIAN=false
MODEL_UPDATE=false

for arg in "$@"; do
  case "$arg" in
    --guardian-force)
      FORCE_GUARDIAN=true
      ;;
    --model-update)
      MODEL_UPDATE=true
      ;;
  esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODEL_FILE="$SCRIPT_DIR/../inferer/src/main/resources/inferer/inferer.model"

if [ "$MODEL_UPDATE" = true ] || [ ! -f "$MODEL_FILE" ]; then
  echo "Downloading model ..."
  curl -L \
    https://huggingface.co/giancarlopadoan/inferer/resolve/main/inferer.model \
    -o ../inferer/src/main/resources/inferer/inferer.model.tmp && \
  mv ../inferer/src/main/resources/inferer/inferer.model.tmp \
     ../inferer/src/main/resources/inferer/inferer.model
else
  echo "Model already present"
fi

source "compileProject.sh"


SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GUARDIAN_FILE="$SCRIPT_DIR/../all_data/guardian.jsonl"

if [ "$FORCE_GUARDIAN" = true ] || [ ! -f "$GUARDIAN_FILE" ]; then
  echo "Running guardianCrawler ..."
  source "$SCRIPT_DIR/guardianCrawler.sh"
else
  echo "Skipping guardianCrawler.sh"
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
until curl -s http://localhost:8080/api/v1/importer/hello | grep -q "INDEXER UP"; do
  sleep 2
done
echo "Importer service is ready."


echo "Importing... THE GUARDIAN"
curl -X POST http://localhost:8080/api/v1/importer/import \
  -F "file=@guardian.jsonl" \
  -F "indexName=guardian"
  
sleep 5
until curl -s http://localhost:8080/api/v1/importer/hello | grep -q "INDEXER UP"; do
  sleep 2
done

echo ""
echo "Importing... OWI"
echo ""
curl -X POST http://localhost:8080/api/v1/importer/import \
  -F "file=@owi.json" \
  -F "indexName=owi"
sleep 5
echo "Wait for finishing import..."
sleep 20
echo "The search app is now running..."
echo "Connect to http://localhost:8080/ to use it"
