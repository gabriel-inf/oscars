#!/bin/sh
#ant
. ./setclasspath.sh
java -Djava.net.preferIPv4Stack=true -Djavax.net.ssl.keyStore=repo/sec-client.jks -Djavax.net.ssl.keyStorePassword=password  NotifyEchoHandler $*
