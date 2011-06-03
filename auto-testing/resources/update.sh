#!/bin/bash

export MAVEN_OPTS='-Xmx512M -XX:MaxPermSize=512M'

OSCARS_DIST=/usr/local/oscars-0.6
OSCARS_HOME=/usr/local/oscars
MVN_HOME=/usr/local/apache-maven-2.2.1

cd $OSCARS_DIST
$OSCARS_DIST/bin/stopServers.sh ALL

mkdir -p $HOME/TopoBridgeService
cp -f $OSCARS_HOME/TopoBridgeService/conf/config.HTTP.yaml $HOME/TopoBridgeService
cp -f $OSCARS_HOME/TopoBridgeService/conf/config.SSL.yaml $HOME/TopoBridgeService

mkdir -p $HOME/VlanPCE
cp -f $OSCARS_HOME/VlanPCE/conf/config.HTTP.yaml $HOME/VlanPCE
cp -f $OSCARS_HOME/VlanPCE/conf/config.SSL.yaml $HOME/VlanPCE

mkdir -p $HOME/Utils
cp -f $OSCARS_HOME/Utils/conf/config.yaml $HOME/Utils

svn update
$MVN_HOME/bin/mvn clean
find . -name \*wsdl -exec touch {} \;

$MVN_HOME/bin/mvn install -DskipTests

$OSCARS_DIST/auto-testing/resources/do_export.sh

cp -f $HOME/TopoBridgeService/* $OSCARS_HOME/TopoBridgeService/conf
cp -f $HOME/VlanPCE/* $OSCARS_HOME/VlanPCE/conf
cp -f $HOME/Utils/* $OSCARS_HOME/Utils/conf

cp -f $OSCARS_DIST/auto-testing/test_main.pl $OSCARS_DIST/api/
cp -f $OSCARS_DIST/auto-testing/resources/testdomain* $OSCARS_HOME/TopoBridgeService/conf/
cp -rf $OSCARS_DIST/auto-testing/Lib $OSCARS_DIST/api/

$OSCARS_DIST/bin/startServers.sh DEVELOPMENT ALL

