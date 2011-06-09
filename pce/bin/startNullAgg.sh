#!/bin/bash 
# usage startNullAgg c context
#  -Djavax.net.debug=all will dump the ssl messages
vers=`cat $OSCARS_DIST/VERSION`
DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
if [ ! -d "$DEFAULT_PID_DIR" ]; then
    mkdir "$DEFAULT_PID_DIR"
fi
test   ! -d target/tmp  -o \( target/tmp -ot target/api-$vers.one-jar.jar \) && . bin/expandOneJar.sh
. bin/setclasspath.sh
case $# in
0) context="DEVELOPMENT";;
1) context=$1;;
esac
echo "Starting the Null Aggregator version:$vers context:$context"
java net.es.oscars.pce.nullagg.NullAgg -c $context &
echo $! > $DEFAULT_PID_DIR/nullagg.pid
