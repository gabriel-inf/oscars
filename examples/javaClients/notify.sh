#!/bin/sh
ant
. ./setclasspath.sh
java -Djava.net.preferIPv4Stack=true NotifyClient
