#!/bin/bash

source "compileProject.sh"
source "guardianCrawler.sh"
cd ..
sudo docker compose up -d --build
#source "insertTest.sh"
