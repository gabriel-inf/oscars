#!/bin/bash

NB_PID_FILE=/tmp/nb_core.lock

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
CLASSPATH=$CLASSPATH:${CATALINA_HOME}/shared/classes:build/notifybroker-core.jar

export CLASSPATH=$CLASSPATH

# echo "CLASSPATH is: $CLASSPATH"


# Decide if we are being called as a daemon or from the command-line
CALLING_STYLE=${DAEMON_STYLE-0}

if [ $CALLING_STYLE -ne 1 ]; then
    case $1 in
    start)
        echo "starting NotifyBroker runner"
        echo $$ > $NB_PID_FILE 
        java -Dcatalina.home=${CATALINA_HOME} -Djava.endorsed.dirs=lib/endorsed -Djava.net.preferIPv4Stack=true net.es.oscars.notifybroker.NotifyBrokerRunner  $* ;;
    stop)
        # Note: this only kills the shell, not the NotifyBroker
        echo "stopping NofifyBroker runner"
        read pid < $NB_PID_FILE
        kill -n 9 $pid
        rm -f $NB_PID_FILE ;;
    *)
        echo "call with either start or stop" ;;
    esac
else
   case $1 in
    start)
        echo "starting NotifyBroker runner in background"
        nohup java -Dcatalina.home=${CATALINA_HOME} -Djava.endorsed.dirs=lib/endorsed -Djava.net.preferIPv4Stack=true net.es.oscars.notifybroker.NotifyBrokerRunner $* > /dev/null 2>&1 &
        echo $! > $NB_PID_FILE;;
    stop)
        echo "stopping NotifyBroker runner"
        read pid < $NB_PID_FILE
        kill $pid
        rm -f $NB_PID_FILE ;;
    *)
        echo "call with either start or stop" ;;
    esac
fi
exit 0
