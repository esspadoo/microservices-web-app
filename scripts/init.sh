#!/bin/bash

#Script is intended to be IDEMPOTENT
# Exit immediately on:
# - any command failure (-e)
# - use of undefined variables (-u)
# - failure in any part of a pipeline (-o pipefail)

set -euo pipefail

# ------------------------
# Default flag values
# ------------------------
FORCE_GUARDIAN=false # Whether to force Guardian crawler execution
MODEL_UPDATE=false #  Whether to force ML model re-download
OWI_SAMPLE=false # Whether download an OWI dataset sample

# ------------------------
# Parse command-line arguments
# ------------------------
for arg in "$@"; do
  case "$arg" in
    --guardian-force)
      FORCE_GUARDIAN=true
      ;;
    --model-update)
      MODEL_UPDATE=true
      ;;
    --owi-sample)
      OWI_SAMPLE=true
      ;;
  esac
done

# ------------------------
# Resolve script directory (absolute path)
# ------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Path to the inferer ML model
MODEL_FILE="$SCRIPT_DIR/../inferer/src/main/resources/inferer/inferer.model"

# ------------------------
# Download or update ML model if required
# ------------------------
if [ "$MODEL_UPDATE" = true ] || [ ! -f "$MODEL_FILE" ]; then
  echo "Downloading model ..."

  # Download to a temporary file first (safe write)
  curl -L \
    https://huggingface.co/giancarlopadoan/inferer/resolve/main/inferer.model \
    -o ../inferer/src/main/resources/inferer/inferer.model.tmp && \

  # Atomically move into final location
  mv ../inferer/src/main/resources/inferer/inferer.model.tmp \
     ../inferer/src/main/resources/inferer/inferer.model
else
  echo "Model already present"
fi

# ------------------------
# Compile the project
# ------------------------
# Sourced so that environment variables persist
source "compileProject.sh"


# ------------------------
# Guardian crawler setup
# ------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GUARDIAN_FILE="$SCRIPT_DIR/../all_data/guardian.jsonl"

# Run crawler if forced or if dataset is missing
if [ "$FORCE_GUARDIAN" = true ] || [ ! -f "$GUARDIAN_FILE" ]; then
  echo "Running guardianCrawler ..."
  source "$SCRIPT_DIR/guardianCrawler.sh"
else
  echo "Skipping guardianCrawler.sh"
fi

# ------------------------
# Download a sample OWI dataset 
# ------------------------

#Run download if flag present
if [ "$OWI_SAMPLE" = true ]; then
  echo "Downloading OpenWebIndex sample dataset ..."

  # Download to a temporary file first (safe write)
  curl -L \
    https://huggingface.co/datasets/giancarlopadoan/owi_crawled/resolve/main/owi.json \
    -o ../all_data/owi.json.tmp && \

  # Atomically move into final location
  mv ../all_data/owi.json.tmp \
     ../all_data/owi.json
fi

# ------------------------
# Python environment initialization
# ------------------------
source "py_init.sh"


# ------------------------
# Start Docker services
# ------------------------
cd ..
sudo docker compose up -d --build
echo "Service running..."
echo "..."
echo "..."
echo "Starting to import data"

# ------------------------
# Import datasets
# ------------------------
cd ./all_data/

# Wait until importer service is ready
echo "Waiting for importer service..."
until curl -s http://localhost:8080/api/v1/importer/hello | grep -q "INDEXER UP"; do
  sleep 2
done
echo "Importer service is ready."

# ------------------------
# Import Guardian dataset
# ------------------------
echo "Importing... THE GUARDIAN"

JOB_ID=$(curl -s -D - -o /dev/null \
  -X POST http://localhost:8080/api/v1/importer/import \
  -F "file=@guardian.jsonl" \
  -F "indexName=guardian" \
  | grep -i '^X-Job-Id:' | awk '{print $2}' | tr -d '\r')


if [ -z "$JOB_ID" ]; then
  echo "Failed to extract JOB ID"
else

  # Wait until import completed, and in the meantime print the status
  echo "Job ID: $JOB_ID"

  # Wait until import completed, and in the meantime print the status
  while true; do
    STAT=$(curl -s http://localhost:8080/api/v1/importer/status/$JOB_ID)
    echo "$STAT"

    if echo "$STAT" | grep -q "COMPLETED"; then
      break
    fi
    sleep 1
  done
fi

# Wait again for importer readiness
until curl -s http://localhost:8080/api/v1/importer/hello | grep -q "INDEXER UP"; do
  sleep 2
done

# ------------------------
# Import OWI dataset
# ------------------------
if [ -f "owi.json" ]; then
  echo ""
  echo "Importing... OWI"
  echo ""


  JOB_ID_2=$(curl -s -D - -o /dev/null \
    -X POST http://localhost:8080/api/v1/importer/import \
    -F "file=@owi.json" \
    -F "indexName=owi" \
    | grep -i '^X-Job-Id:' | awk '{print $2}' | tr -d '\r')

  if [ -z "$JOB_ID_2" ]; then
    echo "Failed to extract JOB ID"
  else

    echo "Job ID: $JOB_ID_2"

    # Wait until import completed, and in the meantime print the status
    while true; do
      STAT_2=$(curl -s http://localhost:8080/api/v1/importer/status/$JOB_ID_2)
      echo "$STAT_2"

      if echo "$STAT_2" | grep -q "COMPLETED"; then
        break
      fi
      sleep 1
    done
  fi
fi
# ------------------------
# Completion message
# ------------------------
echo "The search app is now running..."
echo "Connect to http://localhost:8080/ to use it"
