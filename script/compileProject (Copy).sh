#/bin/bash

cd ..

./inferer/mvnw -f inferer/pom.xml clean package 
./searcher/mvnw -f searcher/pom.xml clean package 
