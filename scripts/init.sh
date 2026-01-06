#!/bin/bash

source "compileProject.sh"
source "guardianCrawler.sh"

if [ ! -d "../all_data" ]; then
  mkdir "../all_data"
fi

if [ ! -d "../all_data/raw_data" ]; then
  mkdir "../all_data/raw_data"
fi

if [ ! -d "../all_data/owi_data" ]; then
  mkdir "../all_data/owi_data"
fi
