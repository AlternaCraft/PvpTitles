#!/bin/bash

# A version has been released
ORIGIN_BRANCH="^(v[0-9](.[0-9])+-[a-zA-Z]+)$"
if [[ $TRAVIS_BRANCH =~ $ORIGIN_BRANCH ]]; then
    # Save some useful information
    TARGET_BRANCH="gh-pages"
    
    URL=`git config remote.origin.url`
    REPO=$(echo $URL | sed -e "s/https:\/\//https:\/\/${GH_TOKEN}@/g")
    VERSION=`ls target/PvpTitles-*.jar | sed 's/target\/PvpTitles-//;s/.jar//;'`
    
    COMMIT_AUTHOR_NAME="Travis CI"
    COMMIT_AUTHOR_EMAIL="esejuli94@gmail.com"

    echo "Creating javadoc...\n"

    mvn javadoc:javadoc

    cp -R target/site/apidocs $HOME/javadoc-latest

    cd $HOME

    # Get repository
    git clone --quiet --branch=$TARGET_BRANCH $REPO gh-pages
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
else
    echo "Bad repository; Just deploy for releases"
fi
