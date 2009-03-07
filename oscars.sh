#!/bin/bash

###############################################################################
#aaa-core.sh
###############################################################################
printHelp ()
{
    echo "oscars.sh (start|stop) <options>"
    echo "Description: Starts/stops Tomcat, AAA, NotificationBroker and OSCARS core processes."
    echo "Options:"
    echo "-d           daemonize all processes"
    echo "-f           force existing aaa, oscars, and notifybroker core process to stop with a KILL signal"
    echo "-h           prints this help message"
    echo "-o <file>    output file for daemon STDOUT and STDERR. defaults to /dev/null"
}
###############################################################################

cd ${OSCARS_HOME-.};

#Read command-line options
ACTION=""
for opt in $*
do
    if [ "$opt" == "stop" ]; then
        ACTION="stop"
    elif [ "$opt" == "-p" ]; then
        echo "-p not allowed. All core proceses cannot use the same PID file"
        exit 1
    elif [ "$opt" == "-h" ]; then
        printHelp
        exit 0
    fi
done


#Stop tomcat but suppress any complaints about it not already running
$CATALINA_HOME/bin/shutdown.sh > /dev/null 2>&1
echo "Sleeping for 10 seconds while Tomcat stops..."
sleep 10
if [ -z "$ACTION" ]; then
    $CATALINA_HOME/bin/startup.sh
else
    echo "Tomcat stopped."
fi

# Restart core processses
./aaa-core.sh -d $*
./notifybroker-core.sh -d $*
./oscars-core.sh -d $*



exit 0