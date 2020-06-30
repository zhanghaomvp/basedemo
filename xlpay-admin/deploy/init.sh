#!/usr/bin/env bash

BASEDIR=$(dirname $(realpath $0))

if [ $# -gt 0 ]; then
  PROJECT_NAME=$1
else
  PROJECT_NAME="default"
fi

if [ $# -gt 1 ]; then
  RESET_MYSQL=$2
else
  RESET_MYSQL="false"
fi

if [ $# -gt 2 ]; then
  MYSQL_PASSWORD=$3
else
  MYSQL_PASSWORD="root001"
fi


set -xv
COMPONENT_NAME=$(basename $(realpath $BASEDIR))
set +xv

$BASEDIR/stop.sh $PROJECT_NAME $COMPONENT_NAME

$BASEDIR/setup.sh $PROJECT_NAME $COMPONENT_NAME $RESET_MYSQL $MYSQL_PASSWORD

$BASEDIR/start.sh $PROJECT_NAME
