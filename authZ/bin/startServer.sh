#!/bin/sh
# -Djavax.net.debug=all will dump all ssl information
vers=`cat $OSCARS_DIST/VERSION`
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
case $# in
0) context="DEVELOPMENT";;
1) context=$1;;
esac 
echo "Starting AuthZ with version:$vers context:$context"
java -jar $OSCARS_DIST/authZ/target/authZ-$vers.one-jar.jar -c $context &
echo $! > $DEFAULT_PID_DIR/authZ.pid

