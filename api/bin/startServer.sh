#!/bin/sh
#-Djavax.net.debug=ssl:handshake
vers=`cat $OSCARS_DIST/VERSION`
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
case $# in
0) context="DEVELOPMENT";;
1) context=$1;;
esac
echo "Starting api version:$vers context:$context"
java -ea -Xmx400m -jar $OSCARS_DIST/api/target/api-$vers.one-jar.jar -c $context &
echo $! > $DEFAULT_PID_DIR/api.pid
