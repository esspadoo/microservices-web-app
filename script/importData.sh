#!/bin/bash

curl -s -X POST "localhost:8882/api/v1/importer/import" -F "file=@../all_data/guardian.jsonl" -F "indexName=guardian"