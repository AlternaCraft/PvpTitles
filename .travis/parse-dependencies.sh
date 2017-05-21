#!/bin/bash
FILE_DEPS='.travis/dependencies.txt'

# Transforming dependencies to JSON
DEPS=`cat $FILE_DEPS`

JSON_DEPS='['
JSON_DEPS=$JSON_DEPS`echo "$DEPS" | while IFS=":" read repo artifact type version scope
do
    if [ -n "$repo" -a -n "$artifact" -a -n "$version" ]; then
	 # Main dependency
	 if [[ $repo =~ ^(\+-|\\-).*$ ]]; then
	     # Remove begining
	     repo=\`echo "$repo" | sed 's/+- //' | sed 's/\- //'\`
	     echo "{\"groupId\": \"$repo\", \"artifactId\": \"$artifact\", \"type\": \"$type\", \"version\": \"$version\", \"scope\": \"$scope\"},"
	 fi
    fi
done`

# Cleaning last comma
JSON_DEPS=`echo ${JSON_DEPS:0:${#JSON_DEPS}-1}`"]"

# Build the final JSON
echo $JSON_DEPS > .travis/dependencies.json