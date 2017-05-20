#!/bin/bash

# Save some useful information
TARGET_BRANCH="gh-pages"

REPO=`git config remote.origin.url`
#REPO=$(echo $URL | sed -e "s/https:\/\//https:\/\/${GH_TOKEN}@/g")
SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
VERSION=`ls target/PvpTitles-*.jar | sed 's/target\/PvpTitles-//;s/.jar//;'`

echo "Getting dependencies file..."

mkdir -p $HOME/dependencies-latest
cp .utility/dependencies.json $HOME/dependencies-latest

echo "Creating javadoc..."

mvn -q javadoc:javadoc
cp -R target/site/apidocs $HOME/javadoc-latest

cd $HOME

# Get repository
git clone --quiet --branch=$TARGET_BRANCH $REPO gh-pages

cd gh-pages

git config user.name "Travis CI"
git config user.email "$COMMIT_AUTHOR_EMAIL"

# Save dependencies json
git rm -rf --ignore-unmatch ./dependencies/$VERSION
mkdir -p dependencies/$VERSION
cp -a $HOME/dependencies-latest/. ./dependencies/$VERSION

# Save the latest javadoc
git rm -rf --ignore-unmatch ./javadoc/$VERSION
cp -a $HOME/javadoc-latest/. ./javadoc/$VERSION

# Add and commit new files
git add -A .
git commit -m "Latest components on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to $TARGET_BRANCH"

cd $HOME

# Get the deploy key by using Travis's stored variables to decrypt deploy_key.enc
ENCRYPTED_KEY_VAR="encrypted_${ENCRYPTION_LABEL}_key"
ENCRYPTED_IV_VAR="encrypted_${ENCRYPTION_LABEL}_iv"
ENCRYPTED_KEY=${!ENCRYPTED_KEY_VAR}
ENCRYPTED_IV=${!ENCRYPTED_IV_VAR}
openssl aes-256-cbc -K $ENCRYPTED_KEY -iv $ENCRYPTED_IV -in .utility/deploy_key.enc -out deploy_key -d
chmod 600 deploy_key
eval `ssh-agent -s`
ssh-add deploy_key

# Now that we're all set up, we can push.
git push $SSH_REPO $TARGET_BRANCH

echo "Published Javadoc and dependencies to gh-pages."