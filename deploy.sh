#!/usr/bin/env bash

mvn clean package -DskipTests

mvn install:install-file -Dfile=target/maven-builder-plugin-1.0-SNAPSHOT.jar -Dpackaging=jar -DgroupId=com.lanxing.plugin -DartifactId=maven-builder-plugin -Dversion=1.0-SNAPSHOT