#!/bin/bash

echo "<settings><servers><server><id>bintray</id><username>\${BINTRAY_USER}</username><password>\${BINTRAY_PASS}</password></server></servers></settings>" > ~/settings.xml

if [[ ! -z $TRAVIS_TAG ]]; then
  ARTIFACT_ID=`echo -e 'setns x=http://maven.apache.org/POM/4.0.0\ncat /x:project/x:artifactId/text()' | xmllint --shell pom.xml | grep -v /`
  VERSION=`ls target/$ARTIFACT_ID-*.jar | sed 's/target\/$ARTIFACT_ID-//;s/.jar//;'`  
  
  # Skipping snapshots
  if [[ ! $VERSION =~ "SNAPSHOT" ]]; then
    bash .utility/parse-dependencies.sh && bash .utility/deploy-pages.sh $VERSION
  fi
else
  mvn deploy --settings ~/settings.xml
fi