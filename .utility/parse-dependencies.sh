#!/bin/bash
FILE_DEPS='.utility/dependencies.txt'
FILE_NEWS='.utility/dependencies_new.txt'

# Transforming dependencies to JSON
DEPS=`cat $FILE_DEPS`

JSON_DEPS='['
JSON_DEPS=$JSON_DEPS`echo "$DEPS" | while IFS=":" read repo artifact type version scope
do
    if [ -n "$repo" -a -n "$artifact" -a -n "$version" ]; then
	 # Main dependency
	 if [[ $repo =~ ^(\+-|\\-).*$ ]]; then
	     repo=\`echo "$repo" | sed 's/+- //' | sed 's/\- //'\`
	     echo "{\"groupId\": \"$repo\", \"artifactId\": \"$artifact\", \"type\": \"$type\", \"version\": \"$version\", \"scope\": \"$scope\"},"
   # Dependency of dependency
   # elif [[ $repo =~ ^(\||( )+).*$ ]]; then
	 fi
    fi
done`

LEN=${#JSON_DEPS}
JSON_DEPS=`echo ${JSON_DEPS:0:LEN-1}`
JSON_DEPS=$JSON_DEPS']'

# Build the final JSON
echo $JSON_DEPS > .utility/dependencies.json

echo "Available dependencies parsed to JSON succesfully"

# Transforming new versions to JSON
NEW=`cat $FILE_NEWS`
NEW=`echo "$NEW" | sed 's/The following dependencies in Dependencies have newer versions://'`
NEW=`echo "$NEW" | sed 's/No dependencies in Dependencies have newer versions.//'`
NEW=`echo "$NEW" | sed 's/  //;s/................................ //;s/ ->//;'`

JSON_NEW='['
JSON_NEW=$JSON_NEW`echo "$NEW" | while IFS=" " read repo actual new
do
    if [ -n "$repo" -a -n "$actual" -a -n "$new" ]; then
        echo "{'repository': '$repo', 'actualv': '$actual', 'newv': '$new'},"
    fi
done`

LEN=${#JSON_NEW}
if [ $LEN -gt 1 ]; then
    JSON_NEW=`echo ${JSON_NEW:0:LEN-1}`
fi

JSON_NEW=$JSON_NEW']'

# Build the final JSON
echo $JSON_NEW > .utility/changes.json

echo "New versions parsed to JSON succesfully"
