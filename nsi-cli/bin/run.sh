#!/bin/bash

debugFlags=""

javaFlags="-Xmx128m "
javaFlags="$javaFlags -Djava.net.preferIPv4Stack=true "
javaFlags="$javaFlags -Dlog4j.configuration=file:./config/log4j.properties "
javaFlags="$javaFlags -Dcom.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot=true "
javaFlags="$javaFlags -Dorg.apache.cxf.JDKBugHacks.defaultUsesCaches=true "
javaFlags="$javaFlags -jar target/nsi-cli-1.1.one-jar.jar "

params=""
verbose=""
while [ "$1" != "" ]; do

    if [ "$1" == "-v" ]; then
      verbose="true"
      shift
    fi

    if [ "$1" == "-d" ]; then
      echo "debugging turned on"
      debugFlags="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
    else
      params="$params $1"
    fi


    if [ "$1" == "--cmdfile" ]; then
      if [ -f $2 ]; then
        echo ""
      else
        echo "Command file [$2] does not exist" 1>&2
        exit 1
      fi
    fi

    shift
done

if [ $verbose ]; then
  echo "verbose mode on"
  echo "params: $params"
  java  $debugFlags $javaFlags $params
else
  java  $debugFlags $javaFlags $params 2> log/cli.err
fi


