#!/bin/bash

debugFlags=""
javaFlags="-Xmx256m "
javaFlags="$javaFlags -Djava.net.preferIPv4Stack=true "
javaFlags="$javaFlags -Dlog4j.configuration=file:./config/log4j.properties "
javaFlags="$javaFlags -Dcom.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot=true "
javaFlags="$javaFlags -Dorg.apache.cxf.JDKBugHacks.defaultUsesCaches=true "

javaFlags="$javaFlags -Dnsibridge.manifest=file:./config/manifest.yaml "
javaFlags="$javaFlags -Dnsibridge.beans=file:./config/beans.xml "

debugSSL="-Djavax.net.debug=ssl,handshake,data,all"


javaFlags="$javaFlags $debugSSL -jar target/nsibridge-1.0-one-jar.jar"
if [ -n "$1" ]; then
  if [ "$1" == "-d" ]; then 
    debugFlags="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=4000"
    argOk="ok"
  else 
    if [ "$1" == "-h" ]; then 
      echo "run.sh [-d : debug mode]" 
      exit 1;
    else 
      echo "invalid option $1"
      exit 1;
    fi
  fi
fi

java $debugFlags $javaFlags 

