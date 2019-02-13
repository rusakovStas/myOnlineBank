#!/bin/bash
source env.config
sed -i '' -e "s/var_port/$port/g" Jenkinsfile
sed -i '' -e  "s/var_test_port/$test_port/g" Jenkinsfile
sed -i '' -e  "s/var_host/$host/g" Jenkinsfile
sed -i '' -e  "s/var_db/$db_name/g" Jenkinsfile

sed -i '' -e  "s/var_port/$port/g" ./client/package.json
sed -i '' -e  "s/var_host/$host/g" ./client/package.json
sed -i '' -e  "s/var_test_port/$test_port/g" ./client/package.json

sed -i '' -e  "s/var_test_port/$test_port/g" ./server/src/test/resources/application-test.properties
sed -i '' -e  "s/var_host/$host/g" ./server/src/test/resources/application-test.properties

sed -i '' -e  "s/var_port/$port/g" ./server/src/main/resources/application-dev.properties
sed -i '' -e  "s/var_host/$host/g" ./server/src/main/resources/application-dev.properties

sed -i '' -e  "s/var_port/$port/g" ./server/src/main/resources/application-firstStart.properties
sed -i '' -e  "s/var_db/$db_name/g" ./server/src/main/resources/application-firstStart.properties

sed -i '' -e  "s/var_port/$port/g" ./server/src/main/resources/application.properties
sed -i '' -e  "s/var_db/$db_name/g" ./server/src/main/resources/application.properties

createdb $db_name
cd client
yarn build-dev
cd ../server
./gradlew clean copyFrontBuildToPublic
cd ..
rm env.config
rm -- "$0"

