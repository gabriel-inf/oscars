#!/bin/sh
. ./setclasspath.sh
java -Djava.net.preferIPv4Stack=true RenewClient $*
