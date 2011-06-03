#!/bin/sh 
#-Djavax.net.debug=ssl:handshake will dump all the ssl messages
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
case $# in
0) context="DEVELOPMENT";;
1) context=$1;;
esac 
echo "Starting AuthN with context set to $context"
java  -jar target/authN-0.0.1-SNAPSHOT.one-jar.jar -c $context  &
echo $! > $DEFAULT_PID_DIR/authN.pid
