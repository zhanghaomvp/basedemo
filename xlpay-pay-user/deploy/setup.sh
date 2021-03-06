#!/usr/bin/env bash

BASEDIR=${BASH_SOURCE%/*}

if [ $# -gt 0 ]; then
  PROJECT_NAME=$1
fi

if [ $# -gt 1 ]; then
  COMPONENT_NAME=$2
fi

if [ $# -gt 2 ]; then
  RESET_MYSQL=$3
fi

if [ $# -gt 3 ]; then
  MYSQL_PASSWORD=$4
else
  MYSQL_PASSWORD='root001'
fi

set -xv

sed -i -E "s#^(\s*)command:.*\$#\1command: java -Xms512m -Xmx1024m -jar /app.jar --spring.profiles.active=prod --spring.datasource.password='"$MYSQL_PASSWORD"'#" $BASEDIR/docker-compose-prod.yml

# clear logs
rm -rf /var/log/$PROJECT_NAME/$COMPONENT_NAME

set +xv
