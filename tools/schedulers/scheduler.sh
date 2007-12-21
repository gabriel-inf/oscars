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



myname=`basename $0`
LOCKFILE=/tmp/lock.$myname
# Loop until we get a lock:
until (umask 222; echo $$ >$LOCKFILE) 2>/dev/null   # test & set
do
   # Optional message - show lockfile owner and creation time:
   set x `ls -l $LOCKFILE`
   echo "Already running scheduler! Lockfile: [$LOCKFILE]. Ctrl-C to exit."

   sleep 5
done

# Do whatever we need exclusive access to do...

java -Dlog4j.configuration=file://$CATALINA_HOME/webapps/OSCARS/WEB-INF/classes/log4j.properties -Djava.net.preferIPv4Stack=true -Dcatalina.home=$CATALINA_HOME PathScheduler $*

rm -f $LOCKFILE            # unlock

exit 1
