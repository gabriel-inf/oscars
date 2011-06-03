#!/bin/sh
# -Djavax.net.debug=all will dump all ssl information
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
case $# in
0) context="DEVELOPMENT";;
1) context=$1;;
esac 
java -Djava.net.preferIPv4Stack=true  -jar target/lookup-0.0.1-SNAPSHOT.one-jar.jar  -c $context &
echo $! > $DEFAULT_PID_DIR/lookup.pid

