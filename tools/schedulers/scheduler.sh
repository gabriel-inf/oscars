#!/bin/sh

# Jason Lee, David Robertson
cwd=`pwd`
progname="$0"
curdir=`dirname "$progname"`

#Set environment variables
CATALINA_HOME=/usr/local/tomcat
AXIS2_HOME=/usr/local/tomcat/webapps/axis2/WEB-INF

# update classpath
OSCARS_CLASSPATH=""
# TODO:  better solution
for f in ../../lib/*.jar
do
    OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

# TODO:  better solution
for f in $AXIS2_HOME/lib/*.jar
do
    OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

CLASSPATH=$OSCARS_CLASSPATH:../../build/WEB-INF/classes
CLASSPATH=$CLASSPATH:.

export CLASSPATH=$CLASSPATH
#echo CLASSPATH is $CLASSPATH

javac `pwd`/PathScheduler.java
#java -Dlog4j.debug=true -Djava.net.preferIPv4Stack=true PathScheduler $*
java -Dlog4j.configuration=file://$CATALINA_HOME/webapps/OSCARS/WEB-INF/classes/log4j.properties -Djava.net.preferIPv4Stack=true -Dcatalina.home=$CATALINA_HOME PathScheduler $*

exit 1
