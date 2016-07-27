#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

SOURCE_BRANCH="2.x"
TARGET_BRANCH="gh-pages"

# Pull requests, forks and commits to other branches shouldn't try to deploy, just build to verify
if [ "$TRAVIS_REPO_SLUG" != "AlternaCraft/PvpTitles" -o "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_BRANCH" != "$SOURCE_BRANCH" ]; then
    echo "Skipping deploy; just doing a build."
    exit 0
fi

# Save some useful information
URL=`git config remote.origin.url`
REPO=$(echo $URL | sed -e "s/https:\/\//https:\/\/${GH_TOKEN}@/g")
VERSION=`cat target/classes/project.properties`

echo "Publishing javadoc...\n"

cp -R target/site/apidocs $HOME/javadoc-latest

cd $HOME

# Get repository
git clone --quiet --branch=$TARGET_BRANCH $REPO $TARGET_BRANCH
git config --global user.name "$COMMIT_AUTHOR_NAME"
git config --global user.email "$COMMIT_AUTHOR_EMAIL"

cd gh-pages

# Save the latest javadoc
git rm -rf --ignore-unmatch ./javadoc/$VERSION
cp -Rf $HOME/javadoc-latest ./javadoc/$VERSION

# Add and commit new files
git add .
git commit -m "Latest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to $TARGET_BRANCH"

# Now that we're all set up, we can push.
git push origin $TARGET_BRANCH

echo "Published Javadoc to gh-pages."
