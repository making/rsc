#!/bin/bash
set -e
VERSION=$1

if [ "${VERSION}" = "" ];then
  echo "usage: $0 VERSION"
  exit 1
fi

set -x

VERSION=${VERSION}-SNAPSHOT
mvn versions:set -DnewVersion=${VERSION} -DallowSnapshots -DgenerateBackupPoms=false
git commit -m "Bump to ${VERSION}" pom.xml
set +x
echo "Run: git push origin master"