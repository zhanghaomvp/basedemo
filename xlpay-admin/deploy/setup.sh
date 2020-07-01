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
  MYSQL_PASSWORD=$3
else
  MYSQL_PASSWORD='root001'
fi

set -xv
if [ "$RESET_MYSQL" = "true" ]; then
  docker exec -i mysql bash -c 'mysql -uroot -p'"$MYSQL_PASSWORD"'' < $BASEDIR/sql/schema.sql
fi
set +xv

set -xv

#sed -i -E "s#^(\s*)command:.*\$#\1command: /start.sh '"$INVOKE_ADDRESS"' '"$QUERY_ADDRESS"' '"$CHAIN_CODE_NAME"' '"$APP_KEY"' '"$APP_SECRET"' '"$SHOW_PATH"' '"$MYSQL_PASSWORD"'#" $BASEDIR/docker-compose-prod.yml

# clear logs
rm -rf /var/log/$PROJECT_NAME/$COMPONENT_NAME

set +xv
