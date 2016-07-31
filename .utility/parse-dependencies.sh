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
	     echo "{'repo': '$repo:$artifact', 'version': '$version'},"
         # Dependency of dependency
#	 elif [[ $repo =~ ^(\||( )+).*$ ]]; then
#            echo "Child -> "$repo":"$artifact" on "$version
	 fi
    fi
done`

LEN=${#JSON_DEPS}
JSON_DEPS=`echo ${JSON_DEPS:0:LEN-1}`
JSON_DEPS=$JSON_DEPS'],'

echo "Available dependencies parsed to JSON succesfully"

# Transforming new versions to JSON
NEW=`cat $FILE_NEWS`
NEW=`echo "$NEW" | sed 's/The following dependencies in Dependencies have newer versions://'`
NEW=`echo "$NEW" | sed 's/No dependencies in Dependencies have newer versions.//'`
NEW=`echo "$NEW" | sed 's/  //;s/................................ //;s/ ->//;'`

JSON_NEW='['
JSON_NEW=$JSON_NEW`echo "$NEW" | while IFS=" " read repo prev last
do
    if [ -n "$repo" -a -n "$prev" -a -n "$last" ]; then
        echo "{'repo': '$repo', 'prev': '$prev', 'last': '$last'},"
    fi
done`

LEN=${#JSON_NEW}
if [ $LEN -gt 1 ]; then
    JSON_NEW=`echo ${JSON_NEW:0:LEN-1}`
fi

JSON_NEW=$JSON_NEW']'

echo "New versions parsed to JSON succesfully"

# Build the final JSON
echo $JSON_DEPS$JSON_NEW > dependencies.json
