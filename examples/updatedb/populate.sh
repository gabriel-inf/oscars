#!/bin/sh
. setclasspath.sh
export CLASSPATH=$CLASSPATH:../../api.build/WEB-INF/classes/net/es/oscars/bss/topology/
java  -Djava.net.preferIPv4Stack=true PopulateDB $*

