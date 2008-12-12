#!/bin/sh



cd ${OSCARS_BASE-.};

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


export CATALINA_HOME=/usr/local/tomcat/;

CLASSPATH=$AXIS2_CLASSPATH:$OSCARS_CLASSPATH
CLASSPATH=$CLASSPATH:build/OSCARS.jar:$CATALINA_HOME/shared/classes/

export CLASSPATH=$CLASSPATH
# echo "CLASSPATH is: $CLASSPATH"

nohup java -Djava.net.preferIPv4Stack=true net.es.oscars.oscars.OSCARSRunner $* > /dev/null 2&>1 &

echo $! > /tmp/oscars_core.pid

exit 0
