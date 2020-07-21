#!/usr/bin/env bash

BASEDIR=${BASH_SOURCE%/*}

if [ $# -gt 0 ]; then
  PROJECT_NAME=$1
else
  echo "Need \"PROJECT_NAME\""
  exit 1
fi


set -xv

docker-compose -p $PROJECT_NAME -f $BASEDIR/docker-compose-prod.yml up -d

set +xv

