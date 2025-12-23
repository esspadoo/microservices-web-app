#!/bin/bash

source "compileProject.sh"
source "guardianCrawler.sh"
cd ..
sudo docker compose up --build
#source "insertTest.sh"