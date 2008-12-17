#!/bin/sh


# Try to cd to the application base directory if the variable is set
cd ${OSCARS_BASE-.};


# Create the classpath:
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

export CLASSPATH=$CLASSPATH

# echo "CLASSPATH is: $CLASSPATH"


# Decide if we are being called as a daemon or from the command-line
CALLING_STYLE=${DAEMON_STYLE-0}

if [ $CALLING_STYLE -ne 1 ]; then
    touch /tmp/oscars_core.lock
    java -Djava.net.preferIPv4Stack=true net.es.oscars.oscars.OSCARSRunner $*
    rm -f /tmp/oscars_core.lock
else
    nohup java -Djava.net.preferIPv4Stack=true net.es.oscars.oscars.OSCARSRunner $* > /dev/null 2&>1 &
    echo $! > $CORE_PID_FILE
fi


exit 0
