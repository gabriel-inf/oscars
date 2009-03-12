#!/bin/bash

###############################################################################
#oscars.sh
###############################################################################
printHelp ()
{
    echo "oscars.sh (start|stop) <options>"
    echo "Description: Starts/stops Tomcat, AAA, NotificationBroker and OSCARS core processes."
    echo "Options:"
    echo "-d           daemonize all processes"
    echo "-f           force existing tomcat, aaa, oscars and notifybroker core process to stop with a KILL signal"
    echo "-h           prints this help message"
    echo "-o <file>    output file for daemon STDOUT and STDERR. defaults to /dev/null"
}
###############################################################################
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
FORCE_KILL=0
#CATALINA_PID is a special variable defined by Tomcat
if [ -z "$CATALINA_PID" ]; then
    CATALINA_PID="$DEFAULT_PID_DIR/tomcat.pid"
    export CATALINA_PID;
fi

cd ${OSCARS_HOME-.};

#Read command-line options
ACTION=""
for opt in $*
do
    if [ "$opt" == "stop" ]; then
        ACTION="stop"
    elif [ "$opt" == "-f" ]; then
        FORCE_KILL=1
    elif [ "$opt" == "-p" ]; then
        echo "-p not allowed. All proceses cannot use the same PID file"
        exit 1
    elif [ "$opt" == "-h" ]; then
        printHelp
        exit 0
    fi
done

#Restart Tomcat
if [ $FORCE_KILL == 0 ]; then
    #supress any error about tomcat not running
    $CATALINA_HOME/bin/shutdown.sh > /dev/null 2>&1
    echo "Sleeping for 10 seconds while Tomcat stops..."
    sleep 10
fi
#Get the Tomcat PID
PID=""
if [ -f "$CATALINA_PID" ]; then
    PID=`cat $CATALINA_PID`
fi
#Make sure Tomcat is really dead
if [ -n "$PID" ]; then
    kill -9 $PID 2>/dev/null
    rm "$CATALINA_PID"
fi
#Start Tomcat or print it stopeed
if [ -z "$ACTION" ]; then
    $CATALINA_HOME/bin/startup.sh
else
    echo "Tomcat stopped."
fi

# Restart core processses
./aaa-core.sh -d $*
sleep 2
./notifybroker-core.sh -d $*
./oscars-core.sh -d $*

exit 0
