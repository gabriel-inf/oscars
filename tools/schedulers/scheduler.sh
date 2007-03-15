#!/bin/sh

# Jason Lee, David Robertson
cwd=`pwd`
echo $cwd
progname="$0"
curdir=`dirname "$progname"`

# update classpath
OSCARS_CLASSPATH=""
# TODO:  better solution
for f in ../../lib/*.jar
do
    OSCARS_CLASSPATH="$OSCARS_CLASSPATH":$f
done

AXIS2_CLASSPATH=""
for f in ../../lib/axis2/*.jar
do
    AXIS2_CLASSPATH="$AXIS2_CLASSPATH":$f
done

CLASSPATH=$AXIS2_CLASSPATH:$OSCARS_CLASSPATH
CLASSPATH=$CLASSPATH:../../build/WEB-INF/classes
CLASSPATH=$CLASSPATH:.

export CLASSPATH=$CLASSPATH
echo CLASSPATH is $CLASSPATH

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

mv -f ${CATALINA_HOME}/logs/scheduler.log ${CATALINA_HOME}/logs/scheduler.log.bak > /dev/null 2>&1
# start up logging to file
javac `pwd`/LSPScheduler.java
java -Djava.net.preferIPv4Stack=true LSPScheduler $* > ${CATALINA_HOME}/logs/scheduler.log 2>&1

exit 1
