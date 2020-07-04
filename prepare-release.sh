#!/bin/bash
set -e
VERSION=$1

if [ "${VERSION}" = "" ];then
  echo "usage: $0 VERSION"
  exit 1
fi

set -x

mvn versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false
git commit -m "Release ${VERSION}" pom.xml
git tag ${VERSION}
#./build-all-binaries.sh
set +x
echo "Run: git push origin ${VERSION} && git push origin master"