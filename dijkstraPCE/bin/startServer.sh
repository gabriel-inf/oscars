#!/bin/sh 
#-Djavax.net.debug=ssl:handshake will dump all the ssl messages
vers=`cat $OSCARS_DIST/VERSION`
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
case $# in
0) context="DEVELOPMENT";;
1) context=$1;;
esac
echo "Starting DijkstraPCE version$vers context:$context"
java -Xmx500m -Djava.net.preferIPv4Stack=true -jar $OSCARS_DIST/dijkstraPCE/target/dijkstraPCE-$vers.one-jar.jar -c $context  &
echo $! > $DEFAULT_PID_DIR/dijPCE.pid
