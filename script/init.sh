#!/bin/bash

source "compileProject.sh"
source "guardianCrawler.sh"
source "py_init.sh"
cd ..
sudo docker compose up -d --build
#source "insertTest.sh"
