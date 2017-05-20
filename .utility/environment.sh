#!/bin/bash

echo "<settings><servers><server><id>bintray</id><username>\${BINTRAY_USER}</username><password>\${BINTRAY_PASS}</password></server></servers></settings>" > ~/settings.xml

if [[ ! -z $TRAVIS_TAG ]]; then
  VERSION=`xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" pom.xml`  
  
  # Skipping snapshots
  if [[ ! $VERSION =~ "SNAPSHOT" ]]; then
    bash .utility/parse-dependencies.sh && bash .utility/deploy-pages.sh $VERSION
  fi
else
  mvn deploy --settings ~/settings.xml
fi