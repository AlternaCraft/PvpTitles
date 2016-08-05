#!/bin/bash

echo "<settings><servers><server><id>bintray</id><username>\${BINTRAY_USER}</username><password>\${BINTRAY_PASS}</password></server></servers></settings>" > ~/settings.xml

[[ ! -z $TRAVIS_TAG ]] && bash .utility/parse-dependencies.sh && bash .utility/deploy-pages.sh || mvn deploy --settings ~/settings.xml