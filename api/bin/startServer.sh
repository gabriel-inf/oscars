#!/bin/sh
#-Djavax.net.debug=ssl:handshake
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
case $# in
0) context="DEVELOPMENT";;
1) context=$1;;
esac 
java -ea -Xmx400m -jar target/api-0.0.1-SNAPSHOT.one-jar.jar -c $context &
echo $! > $DEFAULT_PID_DIR/api.pid
