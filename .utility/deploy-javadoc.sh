#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

SOURCE_BRANCH="2.x"
TARGET_BRANCH="gh-pages"

# Pull requests and commits to other branches shouldn't try to deploy, just build to verify
if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_BRANCH" != "$SOURCE_BRANCH" ]; then
    echo "Skipping deploy; just doing a build."
    exit 0
fi

echo "Publishing javadoc...\n"

cp -R target/site/apidocs $HOME/javadoc-latest

cd $HOME

git config --global user.name "Travis CI"
git config --global user.email "$COMMIT_AUTHOR_EMAIL"
git clone --quiet --branch=$TARGET_BRANCH https://${GH_TOKEN}@github.com/AlternaCraft/Pvptitles $TARGET_BRANCH > /dev/null

cd gh-pages

git rm -rf ./javadoc
cp -Rf $HOME/javadoc-latest ./javadoc
git add -f .
git commit -m "Latest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to $TARGET_BRANCH"
git push -fq origin $TARGET_BRANCH > /dev/null

echo "Published Javadoc to gh-pages.\n"
