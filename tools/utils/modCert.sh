#!/bin/sh

if [ -z $CATALINA_HOME ]; then
   export CATALINA_HOME=/usr/local/jakarta-tomcat5.5
   echo "setting CATALINA_HOME to $CATALINA_HOME"
fi
# set classpath
OSCARS_CLASSPATH=""
# TODO:  better solution
for f in ../../lib/*.jar
do
    OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

for f in ../../lib/axis2/*.jar
do
   OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

CLASSPATH=$OSCARS_CLASSPATH:../../build/WEB-INF/classes
CLASSPATH=$CLASSPATH:./classes

export CLASSPATH=$CLASSPATH
#echo CLASSPATH is $CLASSPATH

java ModifyCertificate $* 

exit 1
