#!/bin/sh 
#-Djavax.net.debug=ssl:handshake will dump all the ssl messages
vers=`cat $OSCARS_DIST/VERSION`
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
echo $*
case $# in
0) context="DEVELOPMENT";;
1) context=$1;;
esac 
echo "Starting openflowPSS version:$vers context:$context"
java   -Xmx256m -Djava.net.preferIPv4Stack=true -jar $OSCARS_DIST/openflowPSS/target/openflowPSS-$vers.one-jar.jar -c $context  &
echo $! > $DEFAULT_PID_DIR/openflowPSS.pid
