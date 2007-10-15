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

CLASSPATH=../../lib/axis2/jaxen-1.1.1.jar:../../lib/axis2/commons-logging-1.1.jar:../../lib/axis2/log4j-1.2.14.jar:../../lib/axis2/mail-1.4.jar:$OSCARS_CLASSPATH
CLASSPATH=$CLASSPATH:../../build/WEB-INF/classes
CLASSPATH=$CLASSPATH:.

export CLASSPATH=$CLASSPATH
#echo CLASSPATH is $CLASSPATH

javac `pwd`/PathScheduler.java
#java -Dlog4j.debug=true -Djava.net.preferIPv4Stack=true PathScheduler $*
java -Djava.net.preferIPv4Stack=true PathScheduler $*

exit 1
