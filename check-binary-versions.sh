#!/bin/bash

./target/classes/rsc-osx-x86_64 -v | jq .
docker run --rm \
   -v "$PWD":/usr/src \
   -v "$HOME/.m2":/root/.m2 \
   -w /usr/src \
   oracle/graalvm-ce:19.2.1 \
   ./target/classes/rsc-linux-x86_64 -v | jq .
java -jar target/rsc-*.jar -v | jq .