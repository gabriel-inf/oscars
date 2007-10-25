#!/bin/sh

# Jason Lee, David Robertson
cwd=`pwd`
progname="$0"
curdir=`dirname "$progname"`

# update classpath
OSCARS_CLASSPATH=""
# TODO:  better solution
for f in ../../lib/*.jar
do
    OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

AXIS2_CLASSPATH=""
for f in ../../lib/axis2/*.jar
do
    AXIS2_CLASSPATH="$AXIS2_CLASSPATH":$f
done

CLASSPATH=$AXIS2_CLASSPATH:$OSCARS_CLASSPATH
CLASSPATH=$CLASSPATH:../../build/WEB-INF/classes
CLASSPATH=$CLASSPATH:.

export CLASSPATH=$CLASSPATH
#echo CLASSPATH is $CLASSPATH

# start up logging to file
javac `pwd`/DBPFClient.java
java -Djava.net.preferIPv4Stack=true DBPFClient $*

exit 1
