#!/usr/bin/env bash

BASEDIR=$(dirname $(realpath $0))

if [ $# -gt 0 ]; then
  PROJECT_NAME=$1
else
  echo "Need \"PROJECT_NAME\""
  exit 1
fi


set -xv
COMPONENT_NAME=$(basename $(realpath $BASEDIR))
set +xv

${BASH_SOURCE%/*}/stop.sh $PROJECT_NAME $COMPONENT_NAME

${BASH_SOURCE%/*}/start.sh $PROJECT_NAME

