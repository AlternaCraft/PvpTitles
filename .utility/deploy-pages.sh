#!/bin/bash

# Save some useful information
TARGET_BRANCH="gh-pages"

URL=`git config remote.origin.url`
REPO=$(echo $URL | sed -e "s/https:\/\//https:\/\/${GH_TOKEN}@/g")
VERSION=`ls target/PvpTitles-*.jar | sed 's/target\/PvpTitles-//;s/.jar//;'`

COMMIT_AUTHOR_NAME="Travis CI"
COMMIT_AUTHOR_EMAIL="esejuli94@gmail.com"

echo "Getting dependencies file..."

mkdir -p $HOME/dependencies-latest
cp .utility/dependencies.json $HOME/dependencies-latest

echo "Creating javadoc..."

mvn -q javadoc:javadoc
cp -R target/site/apidocs $HOME/javadoc-latest

cd $HOME

# Get repository
git clone --quiet --branch=$TARGET_BRANCH $REPO gh-pages
git config --global user.name "$COMMIT_AUTHOR_NAME"
git config --global user.email "$COMMIT_AUTHOR_EMAIL"

cd gh-pages

# Save dependencies json
git rm -rf --ignore-unmatch ./dependencies/$VERSION
mkdir -p dependencies/$VERSION
cp $HOME/dependencies-latest/* ./dependencies/$VERSION

# Save the latest javadoc
git rm -rf --ignore-unmatch ./javadoc/$VERSION
cp -Rf $HOME/javadoc-latest ./javadoc/$VERSION

# Add and commit new files
git add .
git commit -m "Latest components on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to $TARGET_BRANCH"

# Now that we're all set up, we can push.
git push origin $TARGET_BRANCH

echo "Published Javadoc and dependencies to gh-pages."