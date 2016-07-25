#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

SOURCE_BRANCH="2.x"
TARGET_BRANCH="gh-pages"

# Pull requests and commits to other branches shouldn't try to deploy, just build to verify
if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_BRANCH" != "$SOURCE_BRANCH" ]; then
    echo "Skipping deploy; just doing a build."
    exit 0
fi

echo -e "Publishing javadoc...\n"

cp -R build/docs/javadoc $HOME/javadoc-latest

# Save some useful information
REPO=`git config remote.origin.url`
SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
SHA=`git rev-parse --verify HEAD`

# Clone the existing gh-pages for this repo into out/
# Create a new empty branch if gh-pages doesn't exist yet (should only happen on first deply)
git clone --quiet --branch=$TARGET_BRANCH $REPO gh-pages

# Now let's go have some fun with the cloned repo
cd gh-pages

git config user.name "Travis CI"
git config user.email "$COMMIT_AUTHOR_EMAIL"

git rm -rf ./javadoc
cp -Rf $HOME/javadoc-latest ./javadoc
git add -f .
git commit -m "Lastest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages"

# Get the deploy key by using Travis's stored variables to decrypt deploy_key.enc
ENCRYPTED_KEY_VAR="encrypted_${ENCRYPTION_LABEL}_key"
ENCRYPTED_IV_VAR="encrypted_${ENCRYPTION_LABEL}_iv"
ENCRYPTED_KEY=${!ENCRYPTED_KEY_VAR}
ENCRYPTED_IV=${!ENCRYPTED_IV_VAR}

echo "$ENCRYPTED_KEY - $ENCRYPTED_IV"

openssl aes-256-cbc -K $ENCRYPTED_KEY -iv $ENCRYPTED_IV -in deploy_key.enc -out deploy_key -d

<<COMMENT1
chmod 600 deploy_key
eval `ssh-agent -s`
ssh-add deploy_key

cd gh-pages

# Now that we're all set up, we can push.
git push $SSH_REPO $TARGET_BRANCH
COMMENT1

echo -e "Published Javadoc to gh-pages.\n"
