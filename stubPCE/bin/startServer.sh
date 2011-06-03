#!/bin/sh 
#-Djavax.net.debug=ssl:handshake will dump all the ssl messages
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
java  -Djava.net.preferIPv4Stack=true -jar target/stubPCE-0.0.1-SNAPSHOT.one-jar.jar  &
echo $! > $DEFAULT_PID_DIR/stubPCE.pid
