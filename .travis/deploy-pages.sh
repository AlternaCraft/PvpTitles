#!/bin/bash

# Save some useful information
TARGET_BRANCH="gh-pages"

# GIT
REPO=`git config remote.origin.url`
SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
VERSION=$1

# DIRS
DEPENDENCIES="dependencies-latest"
KEYS="deploy-keys"
JAVADOC="javadoc-latest"

# GATHERING THE DATA
echo "Getting dependencies file..."
mkdir -p $HOME/$DEPENDENCIES
cp .travis/dependencies.json $HOME/$DEPENDENCIES

echo "Getting deploy key..."
mkdir -p $HOME/$KEYS
cp .travis/deploy_key.enc $HOME/$KEYS

echo "Creating javadoc..."
mvn -q javadoc:javadoc
cp -R target/site/apidocs $HOME/$JAVADOC

# CLONING REPO
cd $HOME

# Get repository
git clone --quiet --branch=$TARGET_BRANCH $REPO $TARGET_BRANCH

# MOVING DATA AND COMMITING
cd $TARGET_BRANCH

git config user.name "Travis CI"
git config user.email "$COMMIT_AUTHOR_EMAIL"

# Save dependencies json
git rm -rf --ignore-unmatch ./dependencies/$VERSION
mkdir -p dependencies/$VERSION
cp -a $HOME/$DEPENDENCIES/. ./dependencies/$VERSION

# Save the latest javadoc
git rm -rf --ignore-unmatch ./javadoc/$VERSION
cp -a $HOME/$JAVADOC/. ./javadoc/$VERSION

# Add and commit new files
git add -A .
git commit -m "Latest components on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to $TARGET_BRANCH"

cd $HOME

# Get the deploy key by using Travis's stored variables to decrypt deploy_key.enc
ENCRYPTED_KEY_VAR="encrypted_${ENCRYPTION_LABEL}_key"
ENCRYPTED_IV_VAR="encrypted_${ENCRYPTION_LABEL}_iv"
ENCRYPTED_KEY=${!ENCRYPTED_KEY_VAR}
ENCRYPTED_IV=${!ENCRYPTED_IV_VAR}

openssl aes-256-cbc -K $ENCRYPTED_KEY -iv $ENCRYPTED_IV -in $KEYS/deploy_key.enc -out deploy_key -d
chmod 600 deploy_key
eval `ssh-agent -s`
ssh-add deploy_key

cd $TARGET_BRANCH

# Now that we're all set up, we can push.
git push $SSH_REPO $TARGET_BRANCH

echo "Published Javadoc and dependencies to $TARGET_BRANCH."