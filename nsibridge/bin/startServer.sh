#!/bin/sh

pidfile=$1
jarfile=$2
shortname=nsibridge

#set context 
if [ -z "$context" ]; then
    context="DEVELOPMENT"
fi

if [ -z "$pidfile" ]; then
    DEFAULT_PID_DIR="${OSCARS_HOME-.}/run"
    if [ ! -d "$DEFAULT_PID_DIR" ]; then
        mkdir "$DEFAULT_PID_DIR"
    fi
    pidfile=$DEFAULT_PID_DIR/${shortname}.pid
fi

if [ -z "$jarfile" ]; then
    vers=`cat $OSCARS_DIST/VERSION`
    jarfile=$OSCARS_DIST/${shortname}/target/${shortname}-$vers-one-jar.jar
    echo "Starting ${shortname} with version:$vers context:$context"
fi

confDir="/etc/oscars/NsiBridgeService/conf"

javaFlags="-Xmx512m "
javaFlags="$javaFlags -Djava.net.preferIPv4Stack=true "
javaFlags="$javaFlags -Dlog4j.configuration=file:$confDir/log4j.properties "
javaFlags="$javaFlags -Dcom.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot=true "
javaFlags="$javaFlags -Dorg.apache.cxf.JDKBugHacks.defaultUsesCaches=true "

javaFlags="$javaFlags -Dnsibridge.manifest=file:$confDir/manifest.yaml "
javaFlags="$javaFlags -Dnsibridge.beans=file:$confDir/beans.xml "

# debugFlags="-Djavax.net.debug=all "
# debugFlags="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n "

java $debugFlags $javaFlags -jar $jarfile &

echo $! > $pidfile

