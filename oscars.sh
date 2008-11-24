#!/bin/sh

cwd=`pwd`
progname="$0"
curdir=`dirname "$progname"`

# update classpath
OSCARS_CLASSPATH=""
for f in ./lib/*.jar
do
    OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

AXIS2_CLASSPATH=""
for f in ./lib/axis2/*.jar
do
    AXIS2_CLASSPATH="$AXIS2_CLASSPATH":$f
done


CLASSPATH=$AXIS2_CLASSPATH:$OSCARS_CLASSPATH
CLASSPATH=$CLASSPATH:build/OSCARS.jar

export CATALINA_HOME=/usr/local/tomcat/;


export CLASSPATH=$CLASSPATH
# echo CLASSPATH is $CLASSPATH

java -Djava.net.preferIPv4Stack=true net.es.oscars.oscars.OSCARSRunner $*

exit 1
