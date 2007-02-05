#!/bin/sh
# Jason Lee
cwd=`pwd`
progname="$0"
curdir=`dirname "$progname"`

# Only set AXIS2_HOME if not already set
if [ -z "$AXIS2_HOME" ] ; then 
  cd "$curdir"/..
  export AXIS2_HOME=`pwd` 
  cd "${cwd}"
fi

# OSCARS_HOME (for finding conf files)
if [ -z "$OSCARS_HOME" ] ; then 
    cd "$curdir"/..
    export OSCARS_HOME=`pwd`
    cd "${cwd}"
fi

# update classpath
OSCARS_CLASSPATH=""
for f in "$AXIS2_HOME"/lib/*.jar
do
 OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done
AXIS2_CLASSPATH=""
for f in "$AXIS2_HOME"/lib/axis2/*.jar
do
 AXIS2_CLASSPATH="$AXIS2_CLASSPATH":$f
done
for f in "$AXIS2_HOME"/lib/rampart/*.jar
do
 AXIS2_CLASSPATH="$AXIS2_CLASSPATH":$f
done

CLASSPATH=$AXIS2_CLASSPATH:$OSCARS_CLASSPATH

export AXIS2_HOME=$AXIS2_HOME
export AXIS2_CLASSPATH=$AXIS2_CLASSPATH
export OSCARS_CLASSPATH=$OSCARS_CLASSPATH
export CLASSPATH=$CLASSPATH

#echo CLASSPATH is $CLASSPATH
export CLASSPATH=$CLASSPATH:${OSCARS_HOME}/build/WEB-INF/classes

unset scheduler_debug

# check for debug flag
for arg in $* ; do 
   case "${arg}" in
   -d) echo "debug on" 
       scheduler_debug=1
      ;;
   *) echo "Error: unknown arg to shell"
      ;;
   esac
done

# start program from root of OSCARS
cd "${OSCARS_HOME}"

if ! [ -z "${scheduler_debug}" ];  then 
    # back up log file
    mv -f /tmp/scheduler.log /tmp/scheduler.log.bak > /dev/null 2>&1
    # start up logging to file in /tmp
    java -Djava.net.preferIPv4Stack=true net.es.oscars.bss.LSPScheduler $* > /tmp/jscheduler.log 2>&1
else
    # start normally
    java -Djava.net.preferIPv4Stack=true net.es.oscars.bss.LSPScheduler $* > /dev/null 2>&1 
fi

exit 1
