#/bin/bash

cd ..

mvn -f inferer/pom.xml clean package 
mvn -f searcher/pom.xml clean package 
mvn -f trainer/pom.xml clean package
