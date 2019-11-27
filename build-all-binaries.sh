#!/bin/bash

./mvnw clean package -DskipTests -Pgraal
docker run --rm \
   -v "$PWD":/usr/src \
   -v "$HOME/.m2":/root/.m2 \
   -w /usr/src \
   oracle/graalvm-ce:19.2.1 \
   bash -c 'gu install native-image && ./mvnw package -Dversion.generate.skip=true -Pgraal -DskipTests'
./mvnw package -Dversion.generate.skip=true -DskipTests