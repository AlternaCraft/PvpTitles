#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

SOURCE_BRANCH="2.x"
TARGET_BRANCH="gh-pages"

# Pull requests and commits to other branches shouldn't try to deploy, just build to verify
if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_BRANCH" != "$SOURCE_BRANCH" ]; then
    echo "Skipping deploy; just doing a build."
    exit 0
fi

# Save some useful information
REPO=`git config remote.origin.url`
SSH_REPO=$(echo $REPO | sed -e "s/https:\/\/github.com\//git@github.com:/g")
SHA=`git rev-parse --verify HEAD`

echo "Publishing javadoc...\n"

cp -R target/site/apidocs $HOME/javadoc-latest
cp .utility/deploy_key.enc $HOME/deploy_key.enc

cd $HOME

git clone --quiet --branch=$TARGET_BRANCH $REPO $TARGET_BRANCH

git config --global user.name "Travis CI"
git config --global user.email "$COMMIT_AUTHOR_EMAIL"

cd gh-pages

# Save the latest javadoc
git rm -rf --ignore-unmatch ./javadoc
cp -Rf $HOME/javadoc-latest ./javadoc

# Add and commit new files
git add .
git commit -m "Latest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to $TARGET_BRANCH"

# Get the deploy key by using Travis's stored variables to decrypt deploy_key.enc
ENCRYPTED_KEY_VAR="encrypted_${ENCRYPTION_LABEL}_key"
ENCRYPTED_IV_VAR="encrypted_${ENCRYPTION_LABEL}_iv"
ENCRYPTED_KEY=${!ENCRYPTED_KEY_VAR}
ENCRYPTED_IV=${!ENCRYPTED_IV_VAR}
openssl aes-256-cbc -K $ENCRYPTED_KEY -iv $ENCRYPTED_IV -in $HOME/deploy_key.enc -out deploy_key -d
chmod 600 deploy_key
eval "$(ssh-agent -s)"
ssh-add deploy_key

# Now that we're all set up, we can push.
git push $SSH_REPO $TARGET_BRANCH

echo "Published Javadoc to gh-pages."
