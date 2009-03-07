#!/bin/bash

###############################################################################
#oscars-core.sh
###############################################################################
printHelp ()
{
    echo "oscars-core.sh (start|stop) <options>"
    echo "Description: Starts/stops OSCARS Interdomain Controller(IDC) service"
    echo "Options:"
    echo "-d           daemonize this process"
    echo "-f           force an existing process to stop with a KILL signal"
    echo "-h           prints this help message"
    echo "-o <file>    output file for daemon STDOUT and STDERR. defaults to /dev/null"
    echo "-p <file>    the name of the pid file"
    echo "Environment Variables:"
    echo "OSCARS_OPTS     variable used to pass java additional command-line options"
}
###############################################################################
APP_NAME="OSCARS core"
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
DEFAULT_PID_FILE="$DEFAULT_PID_DIR/oscars-core.pid"

# Try to cd to the application base directory if the variable is set
cd ${OSCARS_HOME-.};

#Read command-line options
KILL_CMD="kill"
FORCE_KILL_CMD="kill -9"
READ_PID=0
READ_OUT_FILE=0
ACTION=""
DAEMON_STYLE=0
PID_FILE="$DEFAULT_PID_FILE"
OUT_FILE="/dev/null"
for opt in $*
do
    if [ $READ_PID == 1 ]; then
        PID_FILE="$opt"
        READ_PID=0
    elif [ $READ_OUT_FILE == 1 ]; then
        OUT_FILE="$opt"
        READ_OUT_FILE=0
    elif [ "$opt" == "start" ] && [ -z "$ACTION" ]; then
        ACTION="start"
    elif [ "$opt" == "stop" ] && [ -z "$ACTION" ]; then
        ACTION="stop"
    elif [ "$opt" == "-d" ]; then
        DAEMON_STYLE=1
    elif [ "$opt" == "-o" ]; then
        READ_OUT_FILE=1
    elif [ "$opt" == "-f" ]; then
        KILL_CMD="$FORCE_KILL_CMD"
    elif [ "$opt" == "-p" ]; then
        READ_PID=1
    elif [ "$opt" == "-h" ]; then
        printHelp
        exit 0
    else
        echo "Invalid option '$opt'"
        exit 0
    fi
done

#default to start if ACTION is empty
if [ -z "$ACTION" ]; then
    ACTION="start"
fi

#Kill current process
PID=""
if [ -f "$PID_FILE" ]; then
    PID=`cat $PID_FILE`
fi
KILL_STATUS=0
if [ -n "$PID" ]; then
    $KILL_CMD $PID
    KILL_STATUS=$?
fi

#print error if kill failed during 'stop' otherwise forge ahead
# with start because likely error caused by process already being killed
if [ $KILL_STATUS != 0 ] && [ "$ACTION" == "stop" ]; then
    echo "Unable to stop $APP_NAME. Process may have already been killed."
    exit 1
elif [ -n "$PID" ] && [ $KILL_STATUS == 0 ] && [ "$KILL_CMD" != "$FORCE_KILL_CMD" ]; then
    echo "Waiting 5 seconds for old $APP_NAME processes to close..."
    sleep 5
    #make sure its really gone after giving it 5 seconds to die nicely
    `$FORCE_KILL_CMD $PID 2>/dev/null`
fi
if [ -f "$PID_FILE" ]; then
    rm $PID_FILE
fi

#If actions is stop then we are done
if [ "$ACTION" == "stop" ]; then
    echo "$APP_NAME stopped."
    exit 0
fi

#if action is start then continue....
#Create a directory for pids if none exists and using default
if [ "$DEFAULT_PID_FILE" == "$PID_FILE" ] && [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi

# Create the classpath:
OSCARS_CLASSPATH=""
for f in ${OSCARS_HOME-.}/lib/*.jar
do
    OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done
AXIS2_CLASSPATH=""
for f in ${OSCARS_HOME-.}/lib/axis2/*.jar
do
    AXIS2_CLASSPATH="$AXIS2_CLASSPATH":$f
done
CLASSPATH=$AXIS2_CLASSPATH:$OSCARS_CLASSPATH
CLASSPATH=$CLASSPATH:${OSCARS_HOME-.}/build/oscars-core.jar
export CLASSPATH=$CLASSPATH

# Start java process
if [ $DAEMON_STYLE -eq 1 ]; then
    nohup java $OSCARS_OPTS -Dcatalina.home=${CATALINA_HOME} -Djava.endorsed.dirs=lib/endorsed -Djava.net.preferIPv4Stack=true net.es.oscars.bss.OSCARSRunner > "$OUT_FILE" 2>&1 &
    echo $! > $PID_FILE
    echo "$APP_NAME started.";
else
    echo "$APP_NAME starting...";
    java $OSCARS_OPTS -Dcatalina.home=${CATALINA_HOME} -Djava.endorsed.dirs=lib/endorsed -Djava.net.preferIPv4Stack=true net.es.oscars.bss.OSCARSRunner
fi

exit 0