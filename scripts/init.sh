#!/bin/bash

source "compileProject.sh"
source "guardianCrawler.sh"
cd ..
sudo docker compose up -d --build
echo "Service running..."
echo "..."
echo "..."
echo "Starting to import data"
cd ./all_data/
echo "Importing... THE GUARDIAN"
curl -X POST http://localhost:8882/api/v1/importer/import \
  -F "file=@guardian.jsonl" \
  -F "indexName=guardian"
echo "Importing... OWI"
source "py_init.sh"
curl -X POST http://localhost:8882/api/v1/importer/import \
  -F "file=@owi.json" \
  -F "indexName=owi"
echo "The search app is now running..."
echo "Connect to http://localhost:8080/ to use it"
