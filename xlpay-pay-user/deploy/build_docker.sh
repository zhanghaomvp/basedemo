#!/usr/bin/env bash

BASEDIR=$(dirname $(realpath $0))

set -xv
JAR_FILE_COUNT=$(ls $BASEDIR/../target | grep -E *.jar$ | wc -l)
if [ $JAR_FILE_COUNT -ne 1 ]; then
    echo "Only support 1 jar file"
    ls -al $BASEDIR/../target
    exit 1
fi

COMPONENT_DOCKER_TAG=$(basename $(pwd))

# prepare app
cp $BASEDIR/../target/*.jar $BASEDIR/../docker_image/app.jar

# add start.sh exec permission
chmod a+x $BASEDIR/../docker_image/*.sh

# prepare deploy folder
cd $BASEDIR/..
tar czf ./docker_image/deploy.tar.gz deploy

# build docker
cd $BASEDIR/../docker_image

docker build -t $COMPONENT_DOCKER_TAG .
RC=$?
if [ "$RC" != "0" ]; then
    exit 2
fi
set +xv

