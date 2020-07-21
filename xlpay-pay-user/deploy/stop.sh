#!/usr/bin/env bash

BASEDIR=$(dirname $(realpath $0))

if [ $# -gt 0 ]; then
  PROJECT_NAME=$1
else
  echo "Need \"PROJECT_NAME\" \"COMPONENT_NAME\""
  exit 1
fi

if [ $# -gt 1 ]; then
  COMPONENT_NAME=$2
else
  echo "Need \"PROJECT_NAME\" \"COMPONENT_NAME\""
  exit 2
fi

set +e
docker-compose -p $PROJECT_NAME -f $BASEDIR/docker-compose-prod.yml down

docker stop $COMPONENT_NAME
docker rm $COMPONENT_NAME
set -e



